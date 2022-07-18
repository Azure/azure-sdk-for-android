// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.util.Pair;

import com.azure.android.core.logging.ClientLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A singleton wrapper which manage the persistence and rotation of secrete keys and execution result related to #{RenewalTokenWorker}.
 * Secret keys information is persisted in two files. One is the Android-platform provided key-store API for actual secret key. The
 * other is the alias with the creation time.
 *
 * During each refresh operation. A new pair of secret keys are generated as cryptoKey and authKey. They are persisted into the file
 * system. While secret keys created longer than #{EXPIRATION_TIME_MINUTES} get removed.
 */
public final class RegistrationKeyManager {
    private static RegistrationKeyManager registrationKeyManager;

    private KeyGenerator keyGenerator;

    private KeyStore keyStore;

    private KeyCreationTimeStore keyCreationTimeStore;

    private boolean lastExecutionSucceeded = true;

    //The duration we persist keys in key-store
    public static final int EXPIRATION_TIME_MINUTES = 18;

    public static final String CRYPTO_KEY_PREFIX = "CRYPTO_KEY_";

    public static final String AUTH_KEY_PREFIX = "AUTH_KEY_";

    public static final String KEY_STORE_POSTFIX = "/key-store";

    public static final String KEY_CREATION_TIME_POSTFIX = "/key-creation-time-store";

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyManager.class);

    private RegistrationKeyManager()  {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("KeyGenerator failed: " + e.getMessage()));
        }
        this.keyGenerator.init(256);
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to initialize key store", e));
        }
        keyCreationTimeStore = new KeyCreationTimeStore();
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

    private char[] getKeyStorePassword() {
        return "com.azure.android.communication.chat.android".toCharArray();
    }

    public void setLastExecutionSucceeded(boolean bool) {
        this.lastExecutionSucceeded = bool;
    }

    public boolean getLastExecutionSucceeded() {
        return lastExecutionSucceeded;
    }

    private int getNumOfPairs() {
        int numKeyPairs = 0;
        try {
            numKeyPairs = Math.max(keyStore.size(), keyCreationTimeStore.getSize()) / 2;
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to get size from key store", e));
        }
        return numKeyPairs;
    }

    public void refreshCredentials(String directoryPath) {
        synchronized (RegistrationKeyManager.class) {
            clientLogger.info("Refresh credentials");
            //Fetch latest data from file
            load(directoryPath);

            rotateKeys(directoryPath);
            int index = getNumOfPairs();
            SecretKey newCryptoKey = keyGenerator.generateKey();
            String cryptoAlias = CRYPTO_KEY_PREFIX + index;
            SecretKey newAuthKey = keyGenerator.generateKey();
            String authAlias = AUTH_KEY_PREFIX + index;
            long currentTimeMillis = System.currentTimeMillis();
            storingKeyToFiles(newCryptoKey, cryptoAlias, directoryPath, currentTimeMillis);
            storingKeyToFiles(newAuthKey, authAlias, directoryPath, currentTimeMillis);
        }
    }

    private int extractIndex(String alias) {
        if (alias.indexOf(CRYPTO_KEY_PREFIX) == 0) {
            return Integer.parseInt(alias.replaceAll(CRYPTO_KEY_PREFIX, ""));
        }
        return Integer.parseInt(alias.replaceAll(AUTH_KEY_PREFIX, ""));
    }

    // Clear keys created more than #{EXPIRATION_TIME_MINUTES} and keeps records consistent across key-store and key-creation-time-store
    private void rotateKeys(String directoryPath) {
        int removed = 0;
        HashSet<Integer> set = new HashSet<>();
        HashSet<Integer> removedSet = new HashSet<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                set.add(extractIndex(aliases.nextElement()));
            }
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed iterate key-store", e));
        }
        Set<String> aliases = keyCreationTimeStore.getAliases();
        for (String alias : aliases) {
            set.add(extractIndex(alias));
        }

        //Delete expired keys or inconsistent records
        for (int curIndex : set) {
            String cryptoKeyAlias = CRYPTO_KEY_PREFIX + curIndex;
            String authKeyAlias = AUTH_KEY_PREFIX + curIndex;
            Long insertionTime = getCreationTime(cryptoKeyAlias);
            long currentTime = System.currentTimeMillis();
            long diffInMinutes = (currentTime - insertionTime) / (60 * 1000);

            if (diffInMinutes > EXPIRATION_TIME_MINUTES || anyEntryMissed(cryptoKeyAlias, authKeyAlias)) {
                try {
                    deleteKeyFromFiles(cryptoKeyAlias, directoryPath);
                    deleteKeyFromFiles(authKeyAlias, directoryPath);
                    removedSet.add(curIndex);
                    removed++;
                } catch (Exception e) {
                    throw clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with index: " + curIndex, e));
                }
            }
        }
        set.removeAll(removedSet);

        //Rotate to fill the empty entries. Move remained entries to lowest index
        int toIndex = 0;
        if (removed == 0) {
            return;
        }
        for (int fromIndex : set) {
            String fromCryptoAlias = CRYPTO_KEY_PREFIX + fromIndex;
            String fromAuthAlias = AUTH_KEY_PREFIX + fromIndex;

            //This record is cleared or no need to move
            if (anyEntryMissed(fromCryptoAlias, fromAuthAlias) || toIndex == fromIndex) {
                toIndex++;
                continue;
            }
            SecretKey cryptoKey = getSecreteKey(fromCryptoAlias);
            SecretKey authKey = getSecreteKey(fromAuthAlias);

            String toCryptoAlias = CRYPTO_KEY_PREFIX + toIndex;
            String toAuthAlias = AUTH_KEY_PREFIX + toIndex;
            long creationTime = getCreationTime(fromCryptoAlias);
            storingKeyToFiles(cryptoKey, toCryptoAlias, directoryPath, creationTime);
            storingKeyToFiles(authKey, toAuthAlias, directoryPath, creationTime);

            deleteKeyFromFiles(fromCryptoAlias, directoryPath);
            deleteKeyFromFiles(fromAuthAlias, directoryPath);
            toIndex++;
        }
    }

    // Checking if any entries missed
    private boolean anyEntryMissed(String cryptoKeyAlias, String authKeyAlias) {
        boolean cryptoKeyMissing = getSecreteKey(cryptoKeyAlias) == null;
        boolean authKeyMissing = getSecreteKey(authKeyAlias) == null;
        boolean cryptoTimeMissing = getCreationTime(cryptoKeyAlias) == null;
        boolean authTimeMissing = getCreationTime(authKeyAlias) == null;
        return cryptoKeyMissing | authKeyMissing | cryptoTimeMissing | authTimeMissing;
    }

    //Loads data from key store file to key store entry
    private void load(String directoryPath) {
        char[] password = getKeyStorePassword();
        String keyStorePath = directoryPath + KEY_STORE_POSTFIX;
        try (FileInputStream fis = new File(keyStorePath).exists() ? new FileInputStream(keyStorePath) : null) {
            keyStore.load(fis, password);
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to load key store", e));
        }

        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        try (FileInputStream fis2 = new File(keyCreationTimeStorePath).exists() ? new FileInputStream(keyCreationTimeStorePath) : null) {
            keyCreationTimeStore.load(fis2);
        } catch (IOException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to load key creation time store", e));
        }
    }

    private char[] getPassword() {
        return "com.azure.android.communication.chat.android".toCharArray();
    }

    private void deleteKeyFromFiles(String alias, String directoryPath) {
        //delete from key-store
        String keyStorePath = directoryPath + KEY_STORE_POSTFIX;
        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with alias: " + alias, e));
        }

        File outputFile = new File(keyStorePath);
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                throw clientLogger.logExceptionAsError(new RuntimeException("Failed to create key store file", e));
            }
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            keyStore.store(fos, getPassword());
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to save secrete key", e));
        }

        //delete from key-creation-time-store
        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        try {
            keyCreationTimeStore.deleteEntry(keyCreationTimeStorePath, alias);
        } catch (Exception e) {
            RuntimeException runtimeException = new RuntimeException("keyCreationTimeStore failed to delete entry");
            throw clientLogger.logExceptionAsError(runtimeException);
        }
    }

    public Long getCreationTime(String alias) {
        return keyCreationTimeStore.getCreationTime(alias);
    }

    public SecretKey getSecreteKey(String alias) {
        KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(getPassword());

        // get my private key
        KeyStore.SecretKeyEntry pkEntry = null;
        try {
            pkEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to get secret key", e));
        }
        if (pkEntry == null) {
            return null;
        }
        SecretKey secretKey = pkEntry.getSecretKey();
        return secretKey;
    }

    // Store key information in both 1. key-store 2. key-creation-time-store
    private void storingKeyToFiles(SecretKey secretKey, String alias, String directoryPath, long timeInMilli) {
        if (secretKey == null) {
            return;
        }
        KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(getPassword());
        KeyStore.SecretKeyEntry skEntry =
            new KeyStore.SecretKeyEntry(secretKey);
        try {
            keyStore.setEntry(alias, skEntry, protParam);
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to set entry for key store", e));
        }

        // store away the keystore
        String keyStorePath = directoryPath + KEY_STORE_POSTFIX;
        File outputFile = new File(keyStorePath);
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                throw clientLogger.logExceptionAsError(new RuntimeException("Failed to create key store file", e));
            }
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            keyStore.store(fos, getPassword());
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to save secrete key", e));
        }

        // store in key creation time store
        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        keyCreationTimeStore.storeKeyEntry(keyCreationTimeStorePath, alias, timeInMilli);
    }

    public Pair<SecretKey, SecretKey> getLastPair() {
        int lastIndex = getNumOfPairs() - 1;
        String cryptoAlias = CRYPTO_KEY_PREFIX + lastIndex;
        String authAlias = AUTH_KEY_PREFIX + lastIndex;
        SecretKey cryptoKey = getSecreteKey(cryptoAlias);
        SecretKey authKey = getSecreteKey(authAlias);
        return new Pair<>(cryptoKey, authKey);
    }

    //Return all pair of keys as a stack. The most recent pair of keys are to be popped first
    public Stack<Pair<SecretKey, SecretKey>> getAllPairs() {
        synchronized (RegistrationKeyManager.class) {
            int pairs = getNumOfPairs();
            Stack<Pair<SecretKey, SecretKey>> res = new Stack<>();
            for (int i = 0; i < pairs; i++) {
                String cryptoAlias = CRYPTO_KEY_PREFIX + i;
                String authAlias = AUTH_KEY_PREFIX + i;
                SecretKey cryptoKey = getSecreteKey(cryptoAlias);
                SecretKey authKey = getSecreteKey(authAlias);
                Pair<SecretKey, SecretKey> pair = new Pair<>(cryptoKey, authKey);
                res.push(pair);
            }
            return res;
        }
    }
}
