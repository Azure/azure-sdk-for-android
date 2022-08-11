// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.util.Pair;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatPushNotification;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import java9.util.function.Consumer;

/**
 * The registrar client interface
 */
public class PushNotificationClient {
    private final ClientLogger logger = new ClientLogger(PushNotificationClient.class);
    private final CommunicationTokenCredential communicationTokenCredential;
    private final RegistrarClient registrarClient;
    private final Map<ChatEventType, Set<Consumer<ChatEvent>>> pushNotificationListeners;
    private boolean isPushNotificationsStarted;
    private String deviceRegistrationToken;
    private Timer registrationRenewScheduleTimer;
    private WorkManager workManager;
    private RegistrationKeyManager registrationKeyManager;

    public PushNotificationClient(CommunicationTokenCredential communicationTokenCredential) {
        this.communicationTokenCredential = communicationTokenCredential;
        this.pushNotificationListeners = new HashMap<>();
        this.isPushNotificationsStarted = false;
        this.registrarClient = new RegistrarClient();
        this.deviceRegistrationToken = null;
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
        logger.verbose("device_id: " + deviceRegistrationToken);
        if (this.isPushNotificationsStarted) {
            return;
        }
        this.workManager = WorkManager.getInstance();
        this.registrationKeyManager = RegistrationKeyManager.instance();

        this.isPushNotificationsStarted = true;
        this.pushNotificationListeners.clear();
        this.logger.info("Successfully started push notifications!");
        // 15 minutes is the minimum interval worker manager allows
        long interval = 15;
        this.startRegistrationRenewalWorker(interval, errorHandler);
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
                this.logger.info("Successfully stopped push notification!");
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
        if (this.workManager != null) {
            this.workManager.cancelAllWork();
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

        ChatEventType chatEventType = this.parsePushNotificationEventType(pushNotification);
        this.logger.info(" " + chatEventType + " received.");

        if (this.pushNotificationListeners.containsKey(chatEventType)) {
            ChatEvent event = this.parsePushNotificationEvent(chatEventType, pushNotification);
            Set<Consumer<ChatEvent>> callbacks = this.pushNotificationListeners.get(chatEventType);
            for (Consumer<ChatEvent> callback: callbacks) {
                this.logger.info(" invoke callback " + callback + " for " + chatEventType);
                callback.accept(event);
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
    public void addPushNotificationHandler(ChatEventType chatEventType, Consumer<ChatEvent> listener) {
        this.logger.info(" Add push notification handler.");
        Set<Consumer<ChatEvent>> callbacks;
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
    public void removePushNotificationHandler(ChatEventType chatEventType, Consumer<ChatEvent> listener) {
        if (this.pushNotificationListeners.containsKey(chatEventType)) {
            Set<Consumer<ChatEvent>> callbacks = this.pushNotificationListeners.get(chatEventType);
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
        this.logger.verbose(" Try Jsonlize input.");
        JSONObject obj = new JSONObject(pushNotification.getPayload());
        String decrypted;
        try {
            String encrypted = obj.getString("e");
            this.logger.verbose(" Encrypted payload: " + encrypted);

            decrypted = decryptPayload(encrypted);
            this.logger.verbose(" Decrypted payload: " + decrypted);
        } catch (Throwable e) {
            throw logger.logExceptionAsError(new RuntimeException("Failed to parse push notification payload: " + e));
        }

        return NotificationUtils.toEventPayload(chatEventType, decrypted);
    }

    private void startRegistrationRenewalWorker(long intervalInMinutes, Consumer<Throwable> errorHandler) {
        this.logger.info("Initialize RegistrationRenewalWorker in background");

        Data inputData = new Data.Builder().putString("deviceRegistrationToken", deviceRegistrationToken).build();

        PeriodicWorkRequest renewTokenRequest =
            new PeriodicWorkRequest.Builder(RegistrationRenewalWorker.class, intervalInMinutes, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build();
        workManager.cancelAllWork();
        workManager.enqueueUniquePeriodicWork("Renewal push notification registration", ExistingPeriodicWorkPolicy.REPLACE, renewTokenRequest);

        //Checking the result of last execution. There are two states for periodic work: ENQUEUED and RUNNING. We do checking
        //for every ENQUEUED state, meaning last execution has completed.
        workManager.getWorkInfoByIdLiveData(renewTokenRequest.getId()).observeForever(workInfo -> {
            if (workInfo != null && workInfo.getState() == WorkInfo.State.ENQUEUED) {
                boolean succeeded = registrationKeyManager.getLastExecutionSucceeded();
                if (!succeeded) {
                    RuntimeException exception = new RuntimeException(
                        "Registration renew request failed");
                    errorHandler.accept(exception);
                    this.logger.info("Renew token failed");
                } else {
                    this.logger.info("Renew token succeeded");
                }
            }
        });

    }

    private String decryptPayload(String encryptedPayload) throws Throwable {
        this.logger.verbose(" Decrypting payload.");
        Queue<Pair<SecretKey, SecretKey>> registrationKeyEntries = registrationKeyManager.getAllPairs();

        byte[] encryptedBytes = Base64Util.decodeString(encryptedPayload);
        byte[] encryptionKey = NotificationUtils.extractEncryptionKey(encryptedBytes);
        byte[] iv = NotificationUtils.extractInitializationVector(encryptedBytes);
        byte[] ciphertext = NotificationUtils.extractCipherText(encryptedBytes);
        byte[] hmac = NotificationUtils.extractHmac(encryptedBytes);

        while (!registrationKeyEntries.isEmpty()) {
            Pair<SecretKey, SecretKey> pair = registrationKeyEntries.poll();
            SecretKey cryptoKey = pair.first;
            SecretKey authKey = pair.second;
            if (NotificationUtils.verifyEncryptedPayload(encryptionKey, iv, ciphertext, hmac, authKey)) {
                return NotificationUtils.decryptPushNotificationPayload(iv, ciphertext, cryptoKey);
            }
        }

        // Reject the push when computed signature does not match the included signature - it can not be trusted
        throw logger.logExceptionAsError(
            new RuntimeException("Invalid encrypted push notification payload. Dropped the request!"));
    }

}
