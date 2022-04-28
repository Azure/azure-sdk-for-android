// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.fcm;

import static com.azure.android.communication.chat.BuildConfig.PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL;

import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
    private Consumer<Throwable> registrationErrorHandler;
    private Timer registrationRenewScheduleTimer;
    private KeyGenerator keyGenerator;
    private SecretKey cryptoKey;
    private SecretKey authKey;
    private SecretKey previousCryptoKey;
    private SecretKey previousAuthKey;
    private long keyRotateTimeMillis;
    private WorkManager workManager;

    private static final int KEY_SIZE = 256;
    private static final long KEY_ROTATE_GRACE_PERIOD_MILLIS = 3600000;

    public PushNotificationClient(CommunicationTokenCredential communicationTokenCredential) {
        this.communicationTokenCredential = communicationTokenCredential;
        this.pushNotificationListeners = new HashMap<>();
        this.isPushNotificationsStarted = false;
        this.registrarClient = new RegistrarClient();
        this.deviceRegistrationToken = null;
        this.registrationErrorHandler = null;
        this.registrationRenewScheduleTimer = null;
        this.workManager = WorkManager.getInstance();
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
            this.refreshEncryptionKeys();
            this.registrarClient.register(skypeUserToken, deviceRegistrationToken, this.cryptoKey, this.authKey);
        } catch (Throwable throwable) {
            this.logger.error("Failed to start push notifications!");
            errorHandler.accept(throwable);
            return;
        }

        this.isPushNotificationsStarted = true;
        this.pushNotificationListeners.clear();
        this.logger.info("Successfully started push notifications!");

        long delayInMS = 1000L * (long) (Integer.parseInt(PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL) - 30);
//        long delayInMS = (long) 9000;
//        this.scheduleRegistrationRenew2();
        startRegistrationRenewalWorker();
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
        workManager.cancelAllWork();
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

    private void startRegistrationRenewalWorker() {
        Log.i("RenewTokenWorker", "Initialize RenewTokenWorker in background");
        Data inputData = new Data.Builder().putString("deviceRegistrationToken", deviceRegistrationToken).build();

        PeriodicWorkRequest renewTokenRequest =
            new PeriodicWorkRequest.Builder(RenewTokenWorker.class, 15, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build();
        workManager.enqueue(renewTokenRequest);

        //Checking the output of each worker result and handle with registrationErrorHandler.
        //We check the boolean value in output data for the result.
        workManager.getWorkInfoByIdLiveData(renewTokenRequest.getId()).observeForever(workInfo -> {
            if (workInfo != null) {
                Data outputData = workInfo.getOutputData();
                //Updating credentials
                String strCryptoKey = outputData.getString("strCryptoKey");
                String strAuthKey = outputData.getString("strAuthKey");
                this.previousCryptoKey = secretKeyFromStr(strCryptoKey);
                this.previousAuthKey = secretKeyFromStr(strAuthKey);
                this.keyRotateTimeMillis = System.currentTimeMillis();

                //We assume result is success unless getting false from output
                boolean result = outputData.getBoolean("success", true);
                if (!result) {
                    RuntimeException exception = new RuntimeException(
                        "Registration renew request failed after "
                            + NotificationUtils.MAX_REGISTRATION_RETRY_COUNT
                            + "retries.");
                    this.registrationErrorHandler.accept(exception);
                    stopPushNotifications();
                    Log.i("RenewTokenWorker", "Renew token failed");
                } else {
                    Log.i("RenewTokenWorker", "Renew token succeeded");
                }
            }
        });
    }

    private void scheduleRegistrationRenew( long delayMs, final int tryCount) {
        delayMs = 60000l;
        if (!this.isPushNotificationsStarted) {
            this.logger.info("Push notifications have already been stopped! No need to renew registration.");
            return;
        }

        if (this.deviceRegistrationToken == null) {
            stopPushNotifications();
            IllegalStateException exception = new IllegalStateException("No device registration token stored!");
            this.logger.logExceptionAsError(exception);
            if (this.registrationErrorHandler != null) {
                this.registrationErrorHandler.accept(exception);
            }
            return;
        }

        if (tryCount > NotificationUtils.MAX_REGISTRATION_RETRY_COUNT) {
            stopPushNotifications();
            RuntimeException exception = new RuntimeException(
                "Registration renew request failed after "
                + NotificationUtils.MAX_REGISTRATION_RETRY_COUNT
                + "retries.");
            this.logger.logExceptionAsError(exception);
            if (this.registrationErrorHandler != null) {
                this.registrationErrorHandler.accept(exception);
            }
            return;
        }

        this.logger.info("Scheduling Registrar registration to automatically renew in " + delayMs + " ms");

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                boolean retry = false;

                PushNotificationClient.this.logger.info("Renew Registrar registration attempt #" + tryCount);

                String skypeUserToken;
                try {
                    skypeUserToken = communicationTokenCredential.getToken().get().getToken();
                    Log.i("renew token", "get renewed push notification token: " + skypeUserToken);
                    PushNotificationClient.this.refreshEncryptionKeys();
                    PushNotificationClient.this.registrarClient.register(
                        skypeUserToken,
                        deviceRegistrationToken,
                        PushNotificationClient.this.cryptoKey,
                        PushNotificationClient.this.authKey);
                } catch (ExecutionException | InterruptedException e) {
                    Log.i("exception", e.getMessage());
                    PushNotificationClient.this.logger.logExceptionAsError(
                        new RuntimeException("Get skype user token for push notification failed: " + e.getMessage()));
                    retry = true;
                } catch (Throwable throwable) {
                    PushNotificationClient.this.logger.logThrowableAsError(throwable);
                    Log.i("exception", throwable.getMessage());
                    retry = true;
                }

                if (retry) {
                    long delayInMS = 1000L * (long) Math.min(
                        (int) Math.pow(2.0D, (double) tryCount), NotificationUtils.MAX_REGISTRATION_RETRY_DELAY_S);
                    PushNotificationClient.this.logger.info("Registration renew failed, will retry in " + delayInMS + " ms");
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

    private void refreshEncryptionKeys() throws Throwable {
        if (this.keyGenerator == null) {
            this.keyGenerator = KeyGenerator.getInstance("AES");
            this.keyGenerator.init(KEY_SIZE);
        }

        this.previousCryptoKey = this.cryptoKey;
        this.previousAuthKey = this.authKey;

        this.cryptoKey = this.keyGenerator.generateKey();
        this.authKey = this.keyGenerator.generateKey();

        this.keyRotateTimeMillis = System.currentTimeMillis();
    }

    private String decryptPayload(String encryptedPayload) throws Throwable {
        this.logger.verbose(" Decrypting payload.");

        byte[] encryptedBytes = Base64Util.decodeString(encryptedPayload);
        byte[] encryptionKey = NotificationUtils.extractEncryptionKey(encryptedBytes);
        byte[] iv = NotificationUtils.extractInitializationVector(encryptedBytes);
        byte[] cipherText = NotificationUtils.extractCipherText(encryptedBytes);
        byte[] hmac = NotificationUtils.extractHmac(encryptedBytes);

        if (NotificationUtils.verifyEncryptedPayload(encryptionKey, iv, cipherText, hmac, this.authKey)) {
            return NotificationUtils.decryptPushNotificationPayload(iv, cipherText, this.cryptoKey);
        }

        // When client has registered a new key, because of eventual consistency, latencies and concurrency,
        // the old key can still be used by server side for some notifications
        // Try old key if the new key failed to decrypt the payload.
        if (inKeyRotationGracePeriod()
            && NotificationUtils.verifyEncryptedPayload(encryptionKey, iv, cipherText, hmac, this.previousAuthKey)) {
            return NotificationUtils.decryptPushNotificationPayload(iv, cipherText, this.previousCryptoKey);
        }

        // Reject the push when computed signature does not match the included signature - it can not be trusted
        throw logger.logExceptionAsError(
            new RuntimeException("Invalid encrypted push notification payload. Dropped the request!"));
    }

    private boolean inKeyRotationGracePeriod() {
        if (this.previousAuthKey != null) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - this.keyRotateTimeMillis <= KEY_ROTATE_GRACE_PERIOD_MILLIS) {
                return true;
            }
        }

        return false;
    }

    private SecretKey secretKeyFromStr(String str) {
        byte[] decodedKey = Base64Util.decodeString(str);
        if (decodedKey == null) {
            return null;
        }
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
