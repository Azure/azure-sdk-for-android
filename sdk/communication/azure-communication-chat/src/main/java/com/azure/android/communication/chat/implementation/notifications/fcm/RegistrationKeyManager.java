// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A singleton wrapper which manage the persistence and rotation of secrete keys and execution result related to #{RenewalTokenWorker}.
 * The secrete keys are persisted and loaded using key-store API. We generate new secrete keys every refresh and evict expired keys
 * from key-store every refresh.
 *
 * RegistrationKeyStore is used as object to manage the keys in file/memory.
 */
public final class RegistrationKeyManager {
    private static RegistrationKeyManager registrationKeyManager;

    private KeyGenerator keyGenerator;

    private RegistrationKeyStore registrationKeyStore;

    private boolean lastExecutionSucceeded = true;

    //The duration we persist keys in key-store
    public static final int EXPIRATION_TIME_MINUTES = 45;

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyManager.class);

    private RegistrationKeyManager()  {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("KeyGenerator failed: " + e.getMessage()));
        }
        this.keyGenerator.init(256);
        registrationKeyStore = new RegistrationKeyStore();
    }

    public static RegistrationKeyManager instance() {
        if (registrationKeyManager == null) {
            synchronized (RegistrationKeyManager.class) {
                if (registrationKeyManager == null) {
                    registrationKeyManager = new RegistrationKeyManager();
                }
            }
        }
        return registrationKeyManager;
    }

    public void setLastExecutionSucceeded(boolean bool) {
        this.lastExecutionSucceeded = bool;
    }

    public boolean getLastExecutionSucceeded() {
        return lastExecutionSucceeded;
    }

    private long getInsertionTime(RegistrationKeyStore.RegistrationKeyEntry entry) {
        return entry.getCreationTime();
    }

    private int getNumOfPairs() {
        int numKeyPairs = 0;
        try {
            numKeyPairs = registrationKeyStore.getSize();
        } catch (Exception e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to get size from key store", e));
        }
        return numKeyPairs;
    }

    public void refreshCredentials(String path) {
        synchronized (RegistrationKeyManager.class) {
            clientLogger.verbose("Refresh credentials");
            //Fetch latest data from file
            load(path);

            //Rotate key based on creation time
            rotateKeysBasedOnTime(path);
            SecretKey newCryptoKey = keyGenerator.generateKey();
            String cryptoCredential = secretKeyToStr(newCryptoKey);
            SecretKey newAuthKey = keyGenerator.generateKey();
            String authCredential = secretKeyToStr(newAuthKey);
            long currentTimeMillis = System.currentTimeMillis();
            RegistrationKeyStore.RegistrationKeyEntry entry = new RegistrationKeyStore.RegistrationKeyEntry(cryptoCredential, authCredential, currentTimeMillis);
            registrationKeyStore.storeKeyEntry(path, entry);
        }
    }

    private void rotateKeysBasedOnTime(String path) {
        int size = getNumOfPairs();
        //Delete expired keys
        for (int curIndex = 0; curIndex < size; curIndex++) {
            RegistrationKeyStore.RegistrationKeyEntry entry = registrationKeyStore.getFirstEntry();
            long insertionTime = getInsertionTime(entry);
            long currentTime = System.currentTimeMillis();
            long diffInMinutes = (currentTime - insertionTime) / (60 * 1000);
            if (diffInMinutes > EXPIRATION_TIME_MINUTES) {
                // This pair of secrete keys is to remove
                try {
                    registrationKeyStore.deleteFirstEntry(path);
                } catch (Exception e) {
                    clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with index: " + curIndex));
                }
            } else {
                break;
            }
        }
    }

    //Loads data from key store file to key store entry
    private void load(String path) {
        try (FileInputStream fis = new File(path).exists() ? new FileInputStream(path) : null) {
            registrationKeyStore.load(fis);
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to load key store", e));
        }
    }

    private String secretKeyToStr(SecretKey secretKey) {
        return Base64Util.encodeToString(secretKey.getEncoded());
    }

    //The last pair of secrete keys are to be used to renew registration
    public RegistrationKeyStore.RegistrationKeyEntry getLastEntry() {
        synchronized (RegistrationKeyManager.class) {
            return registrationKeyStore.getLastEntry();
        }
    }

    //Return all pair of keys as a stack. The most recent pair of keys are to be popped first
    public Stack<RegistrationKeyStore.RegistrationKeyEntry> getAllEntries() {
        synchronized (RegistrationKeyManager.class) {
            Stack<RegistrationKeyStore.RegistrationKeyEntry> stackToReturn = registrationKeyStore.getAllEntries();
            return stackToReturn;
        }
    }
}
