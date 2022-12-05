// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A singleton wrapper which manage the persistence and rotation of secret keys and execution result related to #{RenewalTokenWorker}.
 * Secret keys information is persisted in two files. One is the Android-platform provided key-store API for actual secret key. The
 * other is the alias with the creation time.
 *
 * During each refresh operation. A new pair of secret keys are generated as cryptoKey and authKey. They are persisted into the file
 * system. While secret keys created longer than #{EXPIRATION_TIME_MINUTES} get removed.
 */
public final class RegistrationKeyManager {
    private static RegistrationKeyManager registrationKeyManager;

    private KeyGenerator androidKeyGenerator;

    private KeyGenerator secretKeyGenerator;

    private KeyStore keyStore;

    private KeyMetaDataStore keyMetaDataStore;

    private Encryptor encryptor;

    private Decryptor decryptor;

    private boolean lastExecutionSucceeded = true;

    //The duration we persist keys in key-store
    public static final int EXPIRATION_TIME_MINUTES = 45;

    public static final String CRYPTO_KEY_PREFIX = "CRYPTO_KEY_";

    public static final String AUTH_KEY_PREFIX = "AUTH_KEY_";

    public static final String KEY_CREATION_TIME_POSTFIX = "/key-creation-time-store";

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyManager.class);

    private RegistrationKeyManager()  {
        encryptor = new Encryptor();
        decryptor = new Decryptor();
        try {
            this.secretKeyGenerator = KeyGenerator.getInstance("AES");
            this.secretKeyGenerator.init(256);
            androidKeyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("KeyGenerator failed: " + e.getMessage()));
        }

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to initialize key store", e));
        }
        keyMetaDataStore = new KeyMetaDataStore();
    }

    private void setUpAndroidKeyGenerator(String alias) {
        try {
            androidKeyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
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

    private int getNumOfPairs() {
        int numKeyPairs = 0;
        try {
            numKeyPairs = Math.max(keyStore.size(), keyMetaDataStore.getSize()) / 2;
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to get size from key store", e));
        }
        return numKeyPairs;
    }

    public void refreshCredentials(String directoryPath, Context context) {
        synchronized (RegistrationKeyManager.class) {
            clientLogger.info("Refresh credentials");
            //Fetch latest data from file
            load(directoryPath);

            rotateKeys(directoryPath, context);
            int index = getNumOfPairs();
            String cryptoAlias = CRYPTO_KEY_PREFIX + index;
            String authAlias = AUTH_KEY_PREFIX + index;
            long currentTimeMillis = System.currentTimeMillis();
            storeSecretKey(cryptoAlias, directoryPath, currentTimeMillis, secretKeyGenerator.generateKey(), context);
            storeSecretKey(authAlias, directoryPath, currentTimeMillis, secretKeyGenerator.generateKey(), context);
        }
    }

    private int extractIndex(String alias) {
        if (alias.indexOf(CRYPTO_KEY_PREFIX) == 0) {
            return Integer.parseInt(alias.substring(alias.lastIndexOf('_') + 1));
        }
        return Integer.parseInt(alias.substring(alias.lastIndexOf('_') + 1));
    }

    // Clear keys created more than #{EXPIRATION_TIME_MINUTES} and keeps records consistent across key-store and key-creation-time-store
    private void rotateKeys(String directoryPath, Context context) {
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
        Set<String> aliases = keyMetaDataStore.getAliases();
        for (String alias : aliases) {
            set.add(extractIndex(alias));
        }

        //Delete expired keys or inconsistent records
        for (int curIndex : set) {
            String cryptoKeyAlias = CRYPTO_KEY_PREFIX + curIndex;
            String authKeyAlias = AUTH_KEY_PREFIX + curIndex;
            Long insertionTime = getCreationTime(cryptoKeyAlias);
            if (insertionTime == null) {
                insertionTime = 0L;
            }
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

            //No need to move
            if (toIndex == fromIndex) {
                toIndex++;
                continue;
            }

            String toCryptoAlias = CRYPTO_KEY_PREFIX + toIndex;
            String toAuthAlias = AUTH_KEY_PREFIX + toIndex;
            moveAnEntry(fromCryptoAlias, toCryptoAlias, directoryPath, context);
            moveAnEntry(fromAuthAlias, toAuthAlias, directoryPath, context);

            deleteKeyFromFiles(fromCryptoAlias, directoryPath);
            deleteKeyFromFiles(fromAuthAlias, directoryPath);
            toIndex++;
        }
    }

    // Checking if any entries missed
    private boolean anyEntryMissed(String cryptoKeyAlias, String authKeyAlias) {
        boolean cryptoKeyMissing = getSecretKey(cryptoKeyAlias) == null;
        boolean authKeyMissing = getSecretKey(authKeyAlias) == null;

        boolean cryptoDataMissing = keyMetaDataStore.getEntry(cryptoKeyAlias) == null;
        boolean authDataMissing = keyMetaDataStore.getEntry(authKeyAlias) == null;
        return cryptoKeyMissing | authKeyMissing | cryptoDataMissing | authDataMissing;
    }

    //Loads data from key store file to key store entry
    private void load(String directoryPath) {
        try {
            //Loading is required for androidKeyStore while file path is not needed
            keyStore.load(null, null);
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to load key store", e));
        }

        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        try (FileInputStream fis2 = new File(keyCreationTimeStorePath).exists() ? new FileInputStream(keyCreationTimeStorePath) : null) {
            keyMetaDataStore.load(fis2);
        } catch (IOException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to load key creation time store", e));
        }
    }

    private void deleteKeyFromFiles(String alias, String directoryPath) {
        //Delete from key-store, the result automatically updated to key-store file system
        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with alias: " + alias, e));
        }

        //delete from key-creation-time-store
        String keyMetaDataStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        try {
            keyMetaDataStore.deleteEntry(keyMetaDataStorePath, alias);
        } catch (Exception e) {
            RuntimeException runtimeException = new RuntimeException("keyMetaDataStore failed to delete entry");
            throw clientLogger.logExceptionAsError(runtimeException);
        }
    }

    public Long getCreationTime(String alias) {
        return keyMetaDataStore.getCreationTime(alias);
    }

    public SecretKey getSecretKey(String alias) {
        KeyStore.SecretKeyEntry pkEntry = null;
        try {
            pkEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to get secret key", e));
        }
        if (pkEntry == null) {
            return null;
        }
        SecretKey secretKey = pkEntry.getSecretKey();
        return secretKey;
    }

    private void moveAnEntry(String fromAlias, String toAlias, String directoryPath, Context context) {
        // Get data to be moved
        KeyMetaDataStore.KeyMetaDataEntry entry = keyMetaDataStore.getEntry(fromAlias);
        SecretKey recoveredKey = recoverSecretKey(fromAlias, entry);
        storeSecretKey(toAlias, directoryPath, entry.getCreationTime(), recoveredKey, context);
    }

    @NonNull
    private SecretKey recoverSecretKey(String alias, KeyMetaDataStore.KeyMetaDataEntry entry) {
        SecretKey androidSecretKey = null;
        try {
            androidSecretKey = (SecretKey) keyStore.getKey(alias, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to restore secret key", e));
        }
        String decrypted = decryptor.decrypt(androidSecretKey, entry.getCiphertext(), entry.getIV());
        SecretKey recoveredKey = secretKeyFromStr(decrypted);
        return recoveredKey;

    }

    //1. Set up and generate AndroidSecretKey 2. Encrypt secret key and store metaData
    private void storeSecretKey(String alias, String directoryPath, long timeInMilli, SecretKey secretKey, Context context) {
        setUpAndroidKeyGenerator(alias);
        androidKeyGenerator.generateKey();
        try {
            encryptor.encrypt((SecretKey) keyStore.getKey(alias, null), secretKeyToStr(secretKey));
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to store secret key", e));
        }

        byte[] encryptedText = encryptor.ciphertext;
        byte[] iv = encryptor.iv;

        // store in key creation time store
        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        KeyMetaDataStore.KeyMetaDataEntry entry = new KeyMetaDataStore.KeyMetaDataEntry(iv, encryptedText, timeInMilli);
        keyMetaDataStore.storeKeyEntry(keyCreationTimeStorePath, alias, entry);
    }

    private String secretKeyToStr(SecretKey secretKey) {
        return Base64Util.encodeToString(secretKey.getEncoded());
    }

    public Pair<SecretKey, SecretKey> getOnePair(int index) {
        String cryptoAlias = CRYPTO_KEY_PREFIX + index;
        String authAlias = AUTH_KEY_PREFIX + index;
        KeyMetaDataStore.KeyMetaDataEntry cryptoEntry = keyMetaDataStore.getEntry(cryptoAlias);
        KeyMetaDataStore.KeyMetaDataEntry authEntry = keyMetaDataStore.getEntry(authAlias);
        SecretKey cryptoKey = recoverSecretKey(cryptoAlias, cryptoEntry);
        SecretKey authKey = recoverSecretKey(authAlias, authEntry);
        return new Pair<>(cryptoKey, authKey);
    }

    public Pair<SecretKey, SecretKey> getLastPair() {
        int lastIndex = getNumOfPairs() - 1;
        return getOnePair(lastIndex);
    }

    //Return all pair of keys as a stack. The most recent pair of keys are to be popped first
    public Queue<Pair<SecretKey, SecretKey>> getAllPairs() {
        synchronized (RegistrationKeyManager.class) {
            int pairs = getNumOfPairs() - 1;
            Queue<Pair<SecretKey, SecretKey>> res = new LinkedList<>();
            for (int i = pairs; i >= 0; i--) {
                Pair<SecretKey, SecretKey> pair = getOnePair(i);
                res.offer(pair);
            }
            return res;
        }
    }

    private SecretKey secretKeyFromStr(String str) {
        byte[] bytes = Base64Util.decodeString(str);
        return new SecretKeySpec(bytes, 0, bytes.length, "AES");
    }

    class Encryptor {
        private byte[] ciphertext;
        private byte[] iv;

        private static final String TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding";

        Encryptor() {
        }

        void encrypt(final Key encryptionKey, final String plaintext) {
            final Cipher cipher;
            try {
                cipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC);
                cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
                iv = cipher.getIV();
                ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException
                | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
                throw clientLogger.logExceptionAsError(new RuntimeException("Failed to encrypt data", e));
            }
        }
    }

    class Decryptor {
        private static final String TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding";

        Decryptor() {
        }

        String decrypt(final Key decryptionKey, final byte[] ciphertext, final byte[] encryptionIv) {
            final Cipher cipher;
            try {
                cipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC);
                final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
                cipher.init(Cipher.DECRYPT_MODE, decryptionKey, spec);
                return new String(cipher.doFinal(ciphertext), "UTF-8");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException
                | InvalidKeyException e) {
                throw clientLogger.logExceptionAsError(new RuntimeException("Failed to decrypt data", e));
            }
        }
    }
}