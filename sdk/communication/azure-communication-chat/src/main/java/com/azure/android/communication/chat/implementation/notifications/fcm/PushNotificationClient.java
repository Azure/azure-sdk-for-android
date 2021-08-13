// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.fcm;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatPushNotification;
import com.azure.android.communication.chat.models.PushNotificationCallback;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.logging.ClientLogger;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * The registrar client interface
 */
public class PushNotificationClient {
    private final ClientLogger logger = new ClientLogger(PushNotificationClient.class);
    private final CommunicationTokenCredential communicationTokenCredential;
    private final RegistrarClient registrarClient;
    private final Map<ChatEventType, Set<PushNotificationCallback>> pushNotificationListeners;
    private boolean isPushNotificationsStarted;

    public PushNotificationClient(CommunicationTokenCredential communicationTokenCredential) {
        this.communicationTokenCredential = communicationTokenCredential;
        this.pushNotificationListeners = new HashMap<>();
        this.isPushNotificationsStarted = false;
        this.registrarClient = new RegistrarClient();
    }

    /**
     * flag to indicate if push notification has started
     * @return boolean if push notification has started
     */
    public boolean hasStarted() {
        return this.isPushNotificationsStarted;
    }

    /**
     * Register the current device for receiving incoming push notifications via FCM.
     * @param deviceRegistrationToken Device registration token obtained from the FCM SDK.
     * @throws RuntimeException if push notifications failed to start.
     */
    public void startPushNotifications(String deviceRegistrationToken) {
        if (this.isPushNotificationsStarted) {
            return;
        }

        String skypeUserToken;
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(
                new RuntimeException("Get skype token failed for push notification: " + e.getMessage()));
        } catch (ExecutionException e) {
            throw logger.logExceptionAsError(
                new RuntimeException("Get skype token failed for push notification: " + e.getMessage()));
        }

        if (!this.registrarClient.register(skypeUserToken, deviceRegistrationToken)) {
            throw logger.logExceptionAsWarning(new RuntimeException("Start push notification failed!"));
        }

        this.isPushNotificationsStarted = true;
        this.pushNotificationListeners.clear();
        this.logger.info(" Registered push notification successfully!");
    }

    /**
     * Unregister the current device from receiving incoming push notifications.
     * All registered handlers would be removed.
     * @throws RuntimeException if push notifications failed to stop.
     */
    public void stopPushNotifications() {
        if (!this.isPushNotificationsStarted) {
            return;
        }

        String skypeUserToken;
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(
                new RuntimeException("Get skype token failed for push notification: " + e.getMessage()));
        } catch (ExecutionException e) {
            throw logger.logExceptionAsError(
                new RuntimeException("Get skype token failed for push notification: " + e.getMessage()));
        }

        if (!this.registrarClient.unregister(skypeUserToken)) {
            throw logger.logExceptionAsWarning(new RuntimeException("Stop push notification failed!"));
        }

        this.isPushNotificationsStarted = false;
        this.pushNotificationListeners.clear();
        this.logger.info(" Unregistered push notification successfully!");
    }

    /**
     * Handle incoming push notification.
     * Invoke corresponding chat event handle if registered.
     * @param pushNotification Incoming push notification payload from the FCM SDK.
     *
     * @return True if there's handler(s) for incoming push notification; otherwise, false.
     */
    public boolean handlePushNotification(ChatPushNotification pushNotification) {
        this.logger.info(" Receive handle push notification request.");

        ChatEventType chatEventType = parsePushNotificationEventType(pushNotification);
        this.logger.info(" " + chatEventType + " received.");

        this.parsePushNotificationEvent(chatEventType, pushNotification);

        if (this.pushNotificationListeners.containsKey(chatEventType)) {
            ChatEvent event = this.parsePushNotificationEvent(chatEventType, pushNotification);
            Set<PushNotificationCallback> callbacks = this.pushNotificationListeners.get(chatEventType);
            for (PushNotificationCallback callback: callbacks) {
                this.logger.info(" invoke callback " + callback + " for " + chatEventType);
                callback.onChatEvent(event);
            }

            return true;
        }

        return false;
    }

    /**
     * Add handler for a chat event for push notifications.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     */
    public void addPushNotificationHandler(ChatEventType chatEventType, PushNotificationCallback listener) {
        this.logger.info(" Add push notification handler.");
        Set<PushNotificationCallback> callbacks;
        if (this.pushNotificationListeners.containsKey(chatEventType)) {
            callbacks = this.pushNotificationListeners.get(chatEventType);
        } else {
            callbacks = new HashSet<>();
        }

        callbacks.add(listener);
        this.pushNotificationListeners.put(chatEventType, callbacks);
    }

    /**
     * Remove handler from a chat event for push notifications.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     */
    public void removePushNotificationHandler(ChatEventType chatEventType, PushNotificationCallback listener) {
        if (this.pushNotificationListeners.containsKey(chatEventType)) {
            Set<PushNotificationCallback> callbacks = this.pushNotificationListeners.get(chatEventType);
            callbacks.remove(listener);
            if (callbacks.isEmpty()) {
                this.pushNotificationListeners.remove(chatEventType);
            } else {
                this.pushNotificationListeners.put(chatEventType, callbacks);
            }
        }
    }

    private ChatEventType parsePushNotificationEventType(ChatPushNotification pushNotification) {
        if (pushNotification.getPayload().containsKey("eventId")) {
            int eventId = Integer.parseInt(pushNotification.getPayload().get("eventId"));
            if (NotificationUtils.isValidEventId(eventId)) {
                return NotificationUtils.getChatEventTypeByEventId(eventId);
            }
        }

        throw logger.logExceptionAsError(new RuntimeException("Invalid push notification payload."));
    }

    private ChatEvent parsePushNotificationEvent(ChatEventType chatEventType, ChatPushNotification pushNotification) {
        this.logger.info(" Try Jsonlize input.");
        JSONObject obj = new JSONObject(pushNotification.getPayload());
        String jsonString = obj.toString();
        this.logger.info(" Result: " + jsonString);
        return NotificationUtils.toEventPayload(chatEventType, jsonString);
    }
}
