package com.azure.android.communication.chat.implementation.notifications.fcm;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.Getter;
import lombok.Setter;

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

    public void refreshCredentials() {
        preCryptoKey = curCryptoKey;
        preAuthKey = curAuthKey;
        curCryptoKey = keyGenerator.generateKey();
        curAuthKey = keyGenerator.generateKey();
        keyRotationTime = System.currentTimeMillis();
    }
}
