// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

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

    private KeyGenerator androidKeyGenerator;

    private KeyGenerator secretKeyGenerator;

    private KeyPairGenerator keyPairGenerator;

    private KeyStore keyStore;

    private KeyMetaDataStore keyMetaDataStore;

    private EnCryptor enCryptor;

    private DeCryptor deCryptor;

    private boolean lastExecutionSucceeded = true;

    //The duration we persist keys in key-store
    public static final int EXPIRATION_TIME_MINUTES = 45;

    public static final String CRYPTO_KEY_PREFIX = "CRYPTO_KEY_";

    public static final String AUTH_KEY_PREFIX = "AUTH_KEY_";

    public static final String KEY_STORE_POSTFIX = "/key-store";

    public static final String KEY_CREATION_TIME_POSTFIX = "/key-creation-time-store";

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyManager.class);

    private RegistrationKeyManager()  {
        enCryptor = new EnCryptor();
        deCryptor = new DeCryptor();
        try {
            this.secretKeyGenerator = KeyGenerator.getInstance("AES");
            this.secretKeyGenerator.init(256);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                androidKeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            } else {
                keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
//                keyPairGenerator.initialize(1024);
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("KeyGenerator failed: " + e.getMessage()));
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
            } else {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
            }
        } catch (KeyStoreException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to initialize key store", e));
        }
        keyMetaDataStore = new KeyMetaDataStore();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    private void setUpAndroidKeyPairGenerator(String alias, Context context) {
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);
            keyPairGenerator.initialize(new KeyPairGeneratorSpec
                .Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=Microsoft ," +
                    " O=M365" +
                    " C=NA"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
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

            //debug
            Queue<Pair<SecretKey, SecretKey>> allPairs = getAllPairs();
            int size = getNumOfPairs() - 1;
            for (int i=size; i>=0; i--) {
                Pair<SecretKey, SecretKey> pair = allPairs.poll();
                String first = secretKeyToStr(pair.first);
                String second = secretKeyToStr(pair.second);
                KeyMetaDataStore.KeyMetaDataEntry entry = keyMetaDataStore.getEntry(CRYPTO_KEY_PREFIX + i);
                long diff = (System.currentTimeMillis() - entry.getCreationTime()) / (60 * 1000);
                Log.i("DebugKeyStore", "index: " + i + ", first key: " + first + ", second key: " + second + ", time-diff: " + diff);
            }
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
        boolean cryptoKeyMissing = true;
        boolean authKeyMissing = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cryptoKeyMissing = getSecretKey(cryptoKeyAlias) == null;
            authKeyMissing = getSecretKey(authKeyAlias) == null;
        } else {
            try {
                cryptoKeyMissing = keyStore.getKey(cryptoKeyAlias, null) == null
                    || keyStore.getCertificate(cryptoKeyAlias) == null
                    || keyStore .getCertificate(cryptoKeyAlias).getPublicKey() == null;
                authKeyMissing = keyStore.getKey(authKeyAlias, null) == null
                    || keyStore.getCertificate(authKeyAlias) == null
                    || keyStore .getCertificate(authKeyAlias).getPublicKey() == null;
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
        }
        boolean cryptoDataMissing = keyMetaDataStore.getEntry(cryptoKeyAlias) == null;
        boolean authDataMissing = keyMetaDataStore.getEntry(authKeyAlias) == null;
        return cryptoKeyMissing | authKeyMissing | cryptoDataMissing | authDataMissing;
    }

    //Loads data from key store file to key store entry
    private void load(String directoryPath) {
        String keyStorePath = directoryPath + KEY_STORE_POSTFIX;
        try (FileInputStream fis = new File(keyStorePath).exists() ? new FileInputStream(keyStorePath) : null) {
//            keyStore.load(fis, null);
            keyStore.load(null, null);
        } catch (IOException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to load key store", e));
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        try (FileInputStream fis2 = new File(keyCreationTimeStorePath).exists() ? new FileInputStream(keyCreationTimeStorePath) : null) {
            keyMetaDataStore.load(fis2);
        } catch (IOException e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to load key creation time store", e));
        }
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
//            keyStore.store(fos, null);
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to save secrete key", e));
        }

        //delete from key-creation-time-store
        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
        try {
            keyMetaDataStore.deleteEntry(keyCreationTimeStorePath, alias);
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

    // Store key information in both 1. key-store 2. key-creation-time-store
    private void storingKeyToFiles(SecretKey secretKey, String alias, String directoryPath, long timeInMilli) {
        if (secretKey == null) {
            return;
        }
        KeyStore.SecretKeyEntry skEntry =
            new KeyStore.SecretKeyEntry(secretKey);
        try {
            keyStore.setEntry(alias, skEntry, null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
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
//            keyStore.store(fos, null);
        } catch (Exception e) {
            throw clientLogger.logExceptionAsError(new RuntimeException("Failed to save secrete key", e));
        }

        // store in key creation time store
        String keyCreationTimeStorePath = directoryPath + KEY_CREATION_TIME_POSTFIX;
//        keyMetaDataStore.storeKeyEntry(keyCreationTimeStorePath, alias, timeInMilli);
    }

    private void moveAnEntry(String fromAlias, String toAlias, String directoryPath, Context context) {
        // Get data to be moved
        KeyMetaDataStore.KeyMetaDataEntry entry = keyMetaDataStore.getEntry(fromAlias);
        SecretKey recoveredKey = recoverSecretKey(fromAlias, entry);
        storeSecretKey(toAlias, directoryPath, entry.getCreationTime(), recoveredKey, context);
    }

    @NonNull
    private SecretKey recoverSecretKey(String alias, KeyMetaDataStore.KeyMetaDataEntry entry) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SecretKey androidSecretKey = null;
            try {
                androidSecretKey = (SecretKey) keyStore.getKey(alias, null);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
            String decrypted = deCryptor.decryptData(androidSecretKey, entry.getEncryptionText(), entry.getIV());
            SecretKey recoveredKey = secretKeyFromStr(decrypted);
            return recoveredKey;
        } else {
            SecretKey recoveredKey = null;
            try {
                PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
                if (publicKey != null) {
                    String decryptedText = deCryptor.decryptData(publicKey, entry.getEncryptionText(), entry.getIV());
                    recoveredKey = secretKeyFromStr(decryptedText);
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            return recoveredKey;
        }
    }

    //1. Set up and generate AndroidSecretKey 2. Encrypt secret key and store metaData
    private void storeSecretKey(String alias, String directoryPath, long timeInMilli, SecretKey secretKey, Context context) {
        Log.i("debugKey", "alias: " + secretKeyToStr(secretKey));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setUpAndroidKeyGenerator(alias);
            androidKeyGenerator.generateKey();
            try {
                enCryptor.encryptText((SecretKey) keyStore.getKey(alias, null), secretKeyToStr(secretKey));
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
        } else {
            setUpAndroidKeyPairGenerator(alias, context);
            keyPairGenerator.generateKeyPair();
            try {
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
                if (privateKey != null && keyStore.getCertificate(alias) != null) {
                    PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
                    if (publicKey != null) {
                        enCryptor.encryptText(publicKey, secretKeyToStr(secretKey));
                    }
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            }
        }

        byte[] encryptedText = enCryptor.encryption;
        byte[] iv = enCryptor.iv;

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

    class EnCryptor {
        private byte[] encryption;
        private byte[] iv;

        private static final String TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding";

        private static final String TRANSFORMATION_ASYMMETRIC = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

        EnCryptor() {
        }

        void encryptText(final Key encryptionKey, final String textToEncrypt) {
            final Cipher cipher;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC);
                    cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
                    iv = cipher.getIV();
                    encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8"));
                } else {
                    cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
                    cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    CipherOutputStream cipherOutputStream = new CipherOutputStream(
                        outputStream, cipher);
                    cipherOutputStream.write(textToEncrypt.getBytes("UTF-8"));
                    cipherOutputStream.close();
                    encryption = outputStream.toByteArray();

                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] getEncryption() {
            return encryption;
        }

        byte[] getIv() {
            return iv;
        }
    }

    class DeCryptor {
        private static final String TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding";

        private static final String TRANSFORMATION_ASYMMETRIC = "RSA/ECB/OAEPPadding";

        DeCryptor() {
        }

        String decryptData(final Key decryptionKey, final byte[] encryptedData, final byte[] encryptionIv) {
            final Cipher cipher;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC);
                    final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
                    cipher.init(Cipher.DECRYPT_MODE, decryptionKey, spec);
                    return new String(cipher.doFinal(encryptedData), "UTF-8");
                } else {
                    cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
                    cipher.init(Cipher.DECRYPT_MODE, decryptionKey);

                    String cipherText = Base64.encodeToString(encryptedData, Base64.DEFAULT);
                    CipherInputStream cipherInputStream = new CipherInputStream(
                        new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), cipher);
                    ArrayList<Byte> values = new ArrayList<>();
                    int nextByte;
                    while ((nextByte = cipherInputStream.read()) != -1) {
                        values.add((byte)nextByte);
                    }

                    byte[] bytes = new byte[values.size()];
                    for(int i = 0; i < bytes.length; i++) {
                        bytes[i] = values.get(i).byteValue();
                    }

                    String finalText = new String(bytes, 0, bytes.length, "UTF-8");
                    return finalText;
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
