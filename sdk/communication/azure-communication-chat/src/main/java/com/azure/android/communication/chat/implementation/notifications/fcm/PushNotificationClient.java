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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import java9.util.function.Consumer;

import static com.azure.android.communication.chat.BuildConfig.PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL;

/**
 * The registrar client interface
 */
public class PushNotificationClient {
    private final ClientLogger logger = new ClientLogger(PushNotificationClient.class);
    private final CommunicationTokenCredential communicationTokenCredential;
    private final RegistrarClient registrarClient;
    private final Map<ChatEventType, Set<PushNotificationCallback>> pushNotificationListeners;
    private boolean isPushNotificationsStarted;
    private String deviceRegistrationToken;
    private Consumer<Throwable> registrationErrorHandler;
    private Timer registrationRenewScheduleTimer;

    public PushNotificationClient(CommunicationTokenCredential communicationTokenCredential) {
        this.communicationTokenCredential = communicationTokenCredential;
        this.pushNotificationListeners = new HashMap<>();
        this.isPushNotificationsStarted = false;
        this.registrarClient = new RegistrarClient();
        this.deviceRegistrationToken = null;
        this.registrationErrorHandler = null;
        this.registrationRenewScheduleTimer = null;
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
     * @param errorHandler error handler callback for registration failures
     * @throws RuntimeException if push notifications failed to start.
     */
    public void startPushNotifications(String deviceRegistrationToken, Consumer<Throwable> errorHandler) {
        this.deviceRegistrationToken = deviceRegistrationToken;
        this.registrationErrorHandler = errorHandler;

        if (this.isPushNotificationsStarted) {
            return;
        }

        String skypeUserToken;
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
        } catch (ExecutionException | InterruptedException e) {
            throw logger.logExceptionAsError(
                new RuntimeException("Get skype user token failed for push notification: " + e.getMessage()));
        }

        try {
            this.registrarClient.register(skypeUserToken, deviceRegistrationToken);
        } catch (Throwable throwable) {
            this.logger.error("Start push notification failed!");
            errorHandler.accept(throwable);
            return;
        }

        this.isPushNotificationsStarted = true;
        this.pushNotificationListeners.clear();
        this.logger.info("Start push notification successfully!");

        long delayInMS = 1000L * (long) (Integer.parseInt(PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL) - 30);
        this.scheduleRegistrationRenew(delayInMS, 0);
    }

    /**
     * Unregister the current device from receiving incoming push notifications.
     * All registered handlers will be removed.
     */
    public void stopPushNotifications() {
        if (!this.isPushNotificationsStarted) {
            return;
        }

        String skypeUserToken;
        try {
            try {
                skypeUserToken = communicationTokenCredential.getToken().get().getToken();
            } catch (ExecutionException | InterruptedException e) {
                throw logger.logExceptionAsError(
                    new RuntimeException("Get skype user token failed for push notification: " + e.getMessage()));
            }

            try {
                this.registrarClient.unregister(skypeUserToken);
                this.logger.info("Stop push notification successfully!");
            } catch (Throwable throwable) {
                throw logger.logExceptionAsError(new RuntimeException(throwable));
            }
        } catch (RuntimeException e) {
            this.logger.warning("Unregistered push notification with error: "
                + e.getMessage()
                + ". Would just clear local push notification listeners.");
        }

        this.isPushNotificationsStarted = false;
        this.pushNotificationListeners.clear();
        if (this.registrationRenewScheduleTimer != null) {
            this.registrationRenewScheduleTimer.cancel();
            this.registrationRenewScheduleTimer = null;
        }
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

    private void scheduleRegistrationRenew(final long delayMs, final int tryCount) {
        if (!this.isPushNotificationsStarted) {
            this.logger.info("Push notification has already been stopped! No need to renew registration.");
            return;
        }

        if (this.deviceRegistrationToken == null) {
            stopPushNotifications();
            Throwable throwable = new Throwable("No device registration token stored!");
            this.logger.logThrowableAsError(throwable);
            if (this.registrationErrorHandler != null) {
                this.registrationErrorHandler.accept(throwable);
            }
            return;
        }

        if (tryCount > NotificationUtils.MAX_REGISTRATION_RETRY_COUNT) {
            stopPushNotifications();
            Throwable throwable = new Throwable(
                "Registration renew request failed after "
                + NotificationUtils.MAX_REGISTRATION_RETRY_COUNT
                + "retries.");
            this.logger.logThrowableAsError(throwable);
            if (this.registrationErrorHandler != null) {
                this.registrationErrorHandler.accept(throwable);
            }
            return;
        }

        this.logger.info("Scheduling Registrar registration automatically renew in " + delayMs + "ms");

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                boolean retry = false;

                PushNotificationClient.this.logger.info("Renew Registrar registration attempt #" + tryCount);

                String skypeUserToken;
                try {
                    skypeUserToken = communicationTokenCredential.getToken().get().getToken();
                    PushNotificationClient.this.registrarClient.register(skypeUserToken, deviceRegistrationToken);
                } catch (ExecutionException | InterruptedException e) {
                    PushNotificationClient.this.logger.logExceptionAsError(
                        new RuntimeException("Get skype user token failed for push notification: " + e.getMessage()));
                    retry = true;
                } catch (Throwable throwable) {
                    PushNotificationClient.this.logger.logThrowableAsError(throwable);
                    retry = true;
                }

                if (retry) {
                    long delayInMS = 1000L * (long) Math.min(
                        (int) Math.pow(2.0D, (double) tryCount), NotificationUtils.MAX_REGISTRATION_RETRY_DELAY_S);
                    PushNotificationClient.this.logger.info("Registration renew failed, will retry in " + delayInMS + "ms");
                    PushNotificationClient.this.scheduleRegistrationRenew(delayInMS, tryCount + 1);
                } else {
                    long delayInMS = 1000L * (long) (Integer.parseInt(PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL)
                        - NotificationUtils.REGISTRATION_RENEW_IN_ADVANCE_S);
                    PushNotificationClient.this.logger.info("Registration successfully renewed!");
                    PushNotificationClient.this.scheduleRegistrationRenew(delayInMS, 0);
                }
            }
        };

        if (this.registrationRenewScheduleTimer != null) {
            this.registrationRenewScheduleTimer.cancel();
        }

        this.registrationRenewScheduleTimer = new Timer("PushNotificationRegistrarRenewTimer");
        this.registrationRenewScheduleTimer.schedule(task, delayMs);
    }
}
