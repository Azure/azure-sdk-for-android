// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.util.Log;
import android.util.Pair;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;

import java.io.File;
import java.io.FileInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * A singleton wrapper which manage the persistence and rotation of secrete keys and execution result related to #{RenewalTokenWorker}.
 * The secrete keys are persisted and loaded using key-store API. We genterate new secrete keys every refresh and evict expired keys
 * from key-store every refresh.
 *
 * RegistrationKeyStore is used as object to manage the keys in file/memory.
 */
public final class RegistrationKeyManager {
    private static RegistrationKeyManager registrationKeyManager;

    private KeyGenerator keyGenerator;

    private RegistrationKeyStore registrationKeyStore;

    //The duration we persist keys in key-store
    public static final int EXPIRATION_TIME_MINUTES = 45;

    public static final String CRYPTO_KEY_PREFIX = "CRYPTO_KEY_";

    public static final String AUTH_KEY_PREFIX = "AUTH_KEY_";

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyManager.class);

    private RegistrationKeyManager()  {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("KeyGenerator failed: " + e.getMessage());
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

    private long getInsertionTime(String alias) {
        RegistrationKeyStore.RegistrationKeyEntry registrationKeyEntry = getSecreteKeyEntry(alias);
        return registrationKeyEntry.getCreationTime();
    }

    private int getNumOfPairs() {
        int numKeyPairs = 0;
        try {
            numKeyPairs = registrationKeyStore.getSize() / 2;
        } catch (Exception e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to get size from key store", e));
        }
        Log.v("RegistrationContainer", "Number of pairs in key-store: " + numKeyPairs);
        return numKeyPairs;
    }

    public void refreshCredentials(String path) {
        synchronized (RegistrationKeyManager.class) {
            Log.v("RegistrationContainer", "refresh keys");
            //Fetch latest data from file
            load(path);

            SecretKey newCryptoKey = keyGenerator.generateKey();
            SecretKey newAuthKey = keyGenerator.generateKey();

            //Rotate key based on creation time
            rotateKeysBasedOnTime(path);
            int existingPairs = getNumOfPairs();
            int newIndex = existingPairs + 1;
            long currentTimeMillis = System.currentTimeMillis();
            String cryptoKeyAlias = CRYPTO_KEY_PREFIX + newIndex;
            String authKeyAlias = AUTH_KEY_PREFIX + newIndex;
            storeKeyWithAlias(newCryptoKey, cryptoKeyAlias, path, currentTimeMillis);
            storeKeyWithAlias(newAuthKey, authKeyAlias, path, currentTimeMillis);
        }
    }

    private void rotateKeysBasedOnTime(String path) {
        int removedPairs = 0;
        int existingPairs = getNumOfPairs();
        //Delete expired keys
        for (int curIndex = 1; curIndex <= getNumOfPairs(); curIndex++) {
            String curCryptoKeyAlias = CRYPTO_KEY_PREFIX + curIndex;
            String curAuthKeyAlias = AUTH_KEY_PREFIX + curIndex;
            long insertionTime = getInsertionTime(curCryptoKeyAlias);
            long currentTime = System.currentTimeMillis();
            long diffInMinutes = (currentTime - insertionTime) / (60 * 1000);
            if (diffInMinutes > EXPIRATION_TIME_MINUTES) {
                // This pair of secrete keys is to remove
                try {
                    registrationKeyStore.deleteEntry(curCryptoKeyAlias, path);
                    registrationKeyStore.deleteEntry(curAuthKeyAlias, path);
                } catch (Exception e) {
                    clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with index: " + curIndex));
                }
                removedPairs++;
            }
        }

        //Rotate remained keys
        if (removedPairs == 0) {
            return;
        }
        int targetIndex = 1;
        for (int index = removedPairs + 1; index <= existingPairs; index++) {
            String sourceCryptoKeyAlias = CRYPTO_KEY_PREFIX + index;
            String sourceAuthKeyAlias = AUTH_KEY_PREFIX + index;
            String targetCryptoKeyAlias = CRYPTO_KEY_PREFIX + targetIndex;
            String targetAuthKeyAlias = AUTH_KEY_PREFIX + targetIndex;
            RegistrationKeyStore.RegistrationKeyEntry sourceCryptoEntry = getSecreteKeyEntry(sourceCryptoKeyAlias);
            RegistrationKeyStore.RegistrationKeyEntry sourceAuthEntry = getSecreteKeyEntry(sourceAuthKeyAlias);
            storeEntry(targetCryptoKeyAlias, path, sourceCryptoEntry);
            storeEntry(targetAuthKeyAlias, path, sourceAuthEntry);

            try {
                registrationKeyStore.deleteEntry(sourceCryptoKeyAlias, path);
                registrationKeyStore.deleteEntry(sourceAuthKeyAlias, path);
            } catch (Exception e) {
                clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with index: " + index));
            }
            targetIndex++;
        }
    }

    //Loads data from key store file to key store entry
    private void load(String path) {
        try (FileInputStream fis = new File(path).exists() ? new FileInputStream(path) : null) {
            registrationKeyStore.load(fis);
            Log.v("RegistrationContainer", "key-store size: " + registrationKeyStore.getSize());
        } catch (Exception e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to load key store", e));
        }
    }

    private void storeEntry(String alias, String path, RegistrationKeyStore.RegistrationKeyEntry registrationKeyEntry) {
       registrationKeyStore.storeKeyEntry(alias, path, registrationKeyEntry);
    }

    //Writing the keys to key-store file for persistence
    private void storeKeyWithAlias(SecretKey secretKey, String alias, String path, long currentTime) {
        if (secretKey == null) {
            return;
        }

        String credentialStr = secreteKeyToStr(secretKey);
        RegistrationKeyStore.RegistrationKeyEntry registrationKeyEntry = new RegistrationKeyStore.RegistrationKeyEntry(credentialStr, currentTime);
        storeEntry(alias, path, registrationKeyEntry);
    }

    private String secreteKeyToStr(SecretKey secretKey) {
        return Base64Util.encodeToString(secretKey.getEncoded());
    }

    private SecretKey secretKeyFromStr(String str) {
        byte[] bytes = Base64Util.decodeString(str);
        return new SecretKeySpec(bytes, 0, bytes.length, "AES");
    }

    //The last pair of secrete keys are to be used to renew registration
    public Pair<SecretKey, SecretKey> getLastPair() {
        synchronized (RegistrationKeyManager.class) {
            int index = getNumOfPairs();
            String cryptoKeyAlias = CRYPTO_KEY_PREFIX + index;
            String authKeyAlias = AUTH_KEY_PREFIX + index;
            SecretKey cryptoKey = getSecreteKey(cryptoKeyAlias);
            SecretKey authKey = getSecreteKey(authKeyAlias);

            return new Pair<>(cryptoKey, authKey);
        }
    }

    //Return all pair of keys as a stack. The most recent pair of keys are to be popped first
    public Stack<Pair<SecretKey, SecretKey>> getAllPairsOfKeys() {
        synchronized (RegistrationKeyManager.class) {
            int lastIndex = getNumOfPairs();
            Stack<Pair<SecretKey, SecretKey>> stackToReturn = new Stack<>();
            for (int i = 1; i <= lastIndex; i++) {
                String cryptoKeyAlias = CRYPTO_KEY_PREFIX + i;
                String authKeyAlias = AUTH_KEY_PREFIX + i;

                SecretKey cryptoKey = getSecreteKey(cryptoKeyAlias);
                SecretKey authKey = getSecreteKey(authKeyAlias);

                Pair<SecretKey, SecretKey> pair = new Pair<>(cryptoKey, authKey);
                stackToReturn.push(pair);
            }
            return stackToReturn;
        }
    }

    //Get secrete key from key store entry using alias
    private SecretKey getSecreteKey(String alias) {
        RegistrationKeyStore.RegistrationKeyEntry registrationKeyEntry = getSecreteKeyEntry(alias);
        if (registrationKeyEntry == null) {
            return null;
        }
        String credential = registrationKeyEntry.getCredential();
        return secretKeyFromStr(credential);
    }

    private RegistrationKeyStore.RegistrationKeyEntry getSecreteKeyEntry(String alias) {
        // get my private key
        RegistrationKeyStore.RegistrationKeyEntry registrationKeyEntry = null;
        try {
            registrationKeyEntry = registrationKeyStore.getKeyEntry(alias);
        } catch (Exception e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to get secrete key", e));
        }
        return registrationKeyEntry;
    }
}
