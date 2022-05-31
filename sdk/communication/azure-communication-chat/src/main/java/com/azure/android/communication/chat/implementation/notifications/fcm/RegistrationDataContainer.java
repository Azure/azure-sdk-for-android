// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.os.Build;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import com.azure.android.core.logging.ClientLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Set;
import java.util.Stack;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A singleton wrapper which manage the persistence and rotation of secrete keys and execution result related to #{RenewalTokenWorker}.
 * The secrete keys are persisted and loaded using key-store API. We store #{KEY_STORE_SIZE} number of pairs of secrete keys in a local
 * file. All the secrete key pairs are to be tried when decrypt push notification payload.
 *
 * The executionFail flag is to be used in #{PushNotificationClient}.
 */
public class RegistrationDataContainer {
    private static RegistrationDataContainer registrationDataContainer;

    private KeyGenerator keyGenerator;

    private KeyStore keyStore;

    //The number of pairs of secrete keys to persist. When we store more than this size. We rotate the
    //keys. The eldest values is to be over-ridden by the next secrete key.
    public static final int KEY_STORE_SIZE = 10;

    public static final String CRYPTO_KEY_PREFIX = "CRYPTO_KEY_";

    public static final String AUTH_KEY_PREFIX = "AUTH_KEY_";

    private ClientLogger clientLogger = new ClientLogger(RegistrationDataContainer.class);

    private RegistrationDataContainer()  {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("KeyGenerator failed: " + e.getMessage());
        }
        this.keyGenerator.init(256);
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to initialize key store", e));
        }
    }

    public static RegistrationDataContainer instance() {
        if (registrationDataContainer == null) {
            synchronized (RegistrationDataContainer.class) {
                if (registrationDataContainer == null) {
                    registrationDataContainer = new RegistrationDataContainer();
                }
            }
        }
        return registrationDataContainer;
    }

    //Dummy password for accessing credentials in key store. The keys are refreshed with certain interval, so no need
    //to protect with complex password.
    private char[] getPassword() {
        return "communication.chat.password".toCharArray();
    }

    //Loads data from key store file to key store entry
    private void loading(String path) {
        char[] password = getPassword();

        try (FileInputStream fis = new File(path).exists() ? new FileInputStream(path) : null) {
            keyStore.load(fis, password);
            Log.v("RegistrationContainer", "key-store size: " + keyStore.size());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to load key store", e));
        }
    }

    //Get secrete key from key store entry using alias
    public SecretKey getSecreteKey(String alias) {
        KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(getPassword());

        // get my private key
        KeyStore.SecretKeyEntry pkEntry = null;
        try {
            pkEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
        } catch (Exception e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to get secrete key", e));
        }
        if (pkEntry == null) {
            return null;
        }
        SecretKey secretKey = pkEntry.getSecretKey();
        return secretKey;
    }

    //Writing the keys to key-store file for persistence
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void storingKeyWithAlias(SecretKey secretKey, String alias, String path) {
        if (secretKey == null) {
            return;
        }
        KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(getPassword());
        Set<KeyStore.Entry.Attribute> attributes = null;
        KeyStore.SecretKeyEntry skEntry =
            new KeyStore.SecretKeyEntry(secretKey);
        try {
            keyStore.setEntry(alias, skEntry, protParam);
        } catch (KeyStoreException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to set entry for key store", e));
        }

        File outputFile = new File(path);
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                clientLogger.logExceptionAsError(new RuntimeException("Failed to create key store file", e));
            }
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            keyStore.store(fos, getPassword());
        } catch (Exception e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to save secrete key", e));
        }
    }

    //The last pair of secrete keys are to be used to renew registration
    public Pair<SecretKey, SecretKey> getLastPair() {
        synchronized (RegistrationDataContainer.class) {
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
        synchronized (RegistrationDataContainer.class) {
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

    //Generate new pair of secrete key. And rotate keys if key-store exceed the size #{KEY_STORE_SIZE}
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void refreshCredentials(String path) {
        synchronized (RegistrationDataContainer.class) {
            Log.v("RegistrationContainer", "refresh keys");
            //Fetch latest data from file
            loading(path);

            SecretKey newCryptoKey = keyGenerator.generateKey();
            SecretKey newAuthKey = keyGenerator.generateKey();
            int existingPairs = getNumOfPairs();
            //Rotation if key-store is full
            if (existingPairs >= KEY_STORE_SIZE) {
                rotateKeys(path);
            }

            int lastIndex = Math.min(KEY_STORE_SIZE, existingPairs + 1);
            String cryptoKeyAlias = CRYPTO_KEY_PREFIX + lastIndex;
            String authKeyAlias = AUTH_KEY_PREFIX + lastIndex;
            long currentTimeMillis = System.currentTimeMillis();
            storingKeyWithAlias(newCryptoKey, cryptoKeyAlias, path);
            storingKeyWithAlias(newAuthKey, authKeyAlias, path);
        }
    }

    //Getting rid of the eldest key-pair. Each remaining key with index n replace the key with index (n-1)
    //For example, assuming key-store size is 3. [key1, key2, key3] -> [key2, key3]. There is space for inserting new key
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void rotateKeys(String path) {
        for (int index = 2; index <= KEY_STORE_SIZE; index++) {
            String nextCryptoKeyAlias = CRYPTO_KEY_PREFIX + index;
            SecretKey nextCryptoKey = getSecreteKey(nextCryptoKeyAlias);
            String nextAuthKeyAlias = AUTH_KEY_PREFIX + index;
            SecretKey nextAuthKey = getSecreteKey(nextAuthKeyAlias);

            //Over-ridden key with its next key
            int last = index - 1;
            String lastCryptoKeyAlias = CRYPTO_KEY_PREFIX + last;
            storingKeyWithAlias(nextCryptoKey, lastCryptoKeyAlias, path);
            String lastAuthKeyAlias = AUTH_KEY_PREFIX + last;
            storingKeyWithAlias(nextAuthKey, lastAuthKeyAlias, path);

            try {
                keyStore.deleteEntry(nextCryptoKeyAlias);
                keyStore.deleteEntry(nextAuthKeyAlias);
            } catch (KeyStoreException e) {
                clientLogger.logExceptionAsError(new RuntimeException("Failed to delete entry from key-store with index: " + index));
            }
        }
    }

    private int getNumOfPairs() {
        int numKeyPairs = 0;
        try {
            numKeyPairs = keyStore.size() / 2;
        } catch (KeyStoreException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to get size from key store"));
        }
        Log.v("RegistrationContainer", "Number of pairs in key-store: " + numKeyPairs);
        return numKeyPairs;
    }
}
