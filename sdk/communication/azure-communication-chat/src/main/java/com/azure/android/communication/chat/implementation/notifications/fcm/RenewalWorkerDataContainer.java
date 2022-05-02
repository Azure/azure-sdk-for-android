package com.azure.android.communication.chat.implementation.notifications.fcm;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.Getter;
import lombok.Setter;

/**
 * A singleton wrapper which contains KeyGenerator, secret keys and execution result related to #{RenewalTokenWorker}.
 * The major reason for having this class is that Peoriodic Worker only has ENQUEUED and RUNNING state. There is no
 * approach to pass execution result data by design. We use this shared instance to maintain values that we need by
 * both PushNotificationClient and RenewalTokenWorker.
 */
public class RenewalWorkerDataContainer {
    private static RenewalWorkerDataContainer renewalWorkerDataContainer;

    private KeyGenerator keyGenerator;

    @Getter
    @Setter
    private SecretKey curCryptoKey;

    @Getter
    @Setter
    private SecretKey curAuthKey;

    @Getter
    @Setter
    private SecretKey preCryptoKey;

    @Getter
    @Setter
    private SecretKey preAuthKey;

    @Getter
    @Setter
    private boolean executionFail;

    @Getter
    @Setter
    private long keyRotationTime;

    public RenewalWorkerDataContainer()  {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("KeyGenerator failed: " + e.getMessage());
        }
        this.keyGenerator.init(256);
        executionFail = false;
    }

    public static RenewalWorkerDataContainer instance() {
        if (renewalWorkerDataContainer == null) {
            renewalWorkerDataContainer = new RenewalWorkerDataContainer();
        }
        return renewalWorkerDataContainer;
    }

    /**
     * Update stored cryptoKeys and authKeys. The current secret key become the new values generated from key generator.
     * The previous cryptoKeys and authKeys store the previous values. Those keys are used for decrypt push notification
     * body.
     */
    public void refreshCredentials() {
        preCryptoKey = curCryptoKey;
        preAuthKey = curAuthKey;
        curCryptoKey = keyGenerator.generateKey();
        curAuthKey = keyGenerator.generateKey();
        keyRotationTime = System.currentTimeMillis();
    }
}
