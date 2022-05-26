package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Stack;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.Getter;
import lombok.Setter;

/**
 * A singleton wrapper which manage the persistence and rotation of secrete keys and execution result related to #{RenewalTokenWorker}.
 * The secrete keys are persisted and loaded using key-store API. We store #{keyStoreSize} number of pairs of secrete keys in a local
 * file. All the secrete key pairs are to be tried when decrypt push notification payload.
 *
 * The executionFail flag is to be used in #{PushNotificationClient}.
 */
public class RegistrationDataContainer {
    private static RegistrationDataContainer registrationDataContainer;

    private KeyGenerator keyGenerator;

    private KeyStore keyStore;

    @Getter
    @Setter
    private boolean executionFail;

    //The number of pairs of secrete keys to persist. When we store more than this size. We rotate the
    //keys. The eldest values is to be over-ridden by the next secrete key.
    public final static int keyStoreSize = 10;

    public final static String cryptoKeyPrefix = "CRYPTO_KEY_";

    public final static String authKeyPrefix = "AUTH_KEY_";

    public RegistrationDataContainer()  {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("KeyGenerator failed: " + e.getMessage());
        }
        this.keyGenerator.init(256);
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to initialize key store", e);
        }
        executionFail = false;
    }

    public static RegistrationDataContainer instance() {
        if (registrationDataContainer == null) {
            registrationDataContainer = new RegistrationDataContainer();
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
            throw new RuntimeException("Failed to load key store", e);
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
            throw new RuntimeException("Failed to get secrete key", e);
        }
        if (pkEntry == null) {
            return null;
        }
        SecretKey secretKey = pkEntry.getSecretKey();
        return secretKey;
    }

    //Writing the keys to key-store file for persistence
    private void storingKeyWithAlias(SecretKey secretKey, String alias, String path) {
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
            throw new RuntimeException("Failed to set entry for key store", e);
        }

        File outputFile = new File(path);
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                new RuntimeException("Failed to create key store file", e);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            keyStore.store(fos, getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save secrete key", e);
        }
    }

    //The last pair of secrete keys are to be used to renew registration
    public Pair<SecretKey, SecretKey> getLastPair() {
        int index = getNumOfPairs();
        String cryptoKeyAlias = cryptoKeyPrefix + index;
        String authKeyAlias = authKeyPrefix + index;
        SecretKey cryptoKey = getSecreteKey(cryptoKeyAlias);
        SecretKey authKey = getSecreteKey(authKeyAlias);

        return new Pair<>(cryptoKey, authKey);
    }

    //Return all pair of keys as a stack. The most recent pair of keys are to be popped first
    public Stack<Pair<SecretKey, SecretKey>> getAllPairsOfKeys() {
        int lastIndex = getNumOfPairs();
        Stack<Pair<SecretKey, SecretKey>> stackToReturn = new Stack<>();
        for (int i=1; i<=lastIndex; i++) {
            String cryptoKeyAlias = cryptoKeyPrefix + i;
            String authKeyAlias = authKeyPrefix + i;

            SecretKey cryptoKey = getSecreteKey(cryptoKeyAlias);
            SecretKey authKey = getSecreteKey(authKeyAlias);

            Pair<SecretKey, SecretKey> pair = new Pair<>(cryptoKey, authKey);
            stackToReturn.push(pair);
        }
        return stackToReturn;
    }

    //Generate new pair of secrete key. And rotate keys if key-store exceed the size #{keyStoreSize}
    public void refreshCredentials(String path) {
        Log.v("RegistrationContainer", "refresh keys");
        //Fetch latest data from file
        loading(path);

        SecretKey newCryptoKey = keyGenerator.generateKey();
        SecretKey newAuthKey = keyGenerator.generateKey();
        int existingPairs = getNumOfPairs();
        //Rotation if key-store is full
        if (existingPairs >= keyStoreSize) {
            rotateKeys(path);
        }

        int lastIndex = Math.min(keyStoreSize, existingPairs + 1);
        String cryptoKeyAlias = cryptoKeyPrefix + lastIndex;
        String authKeyAlias = authKeyPrefix + lastIndex;
        storingKeyWithAlias(newCryptoKey, cryptoKeyAlias, path);
        storingKeyWithAlias(newAuthKey, authKeyAlias, path);
    }

    //Getting rid of the eldest key-pair. Each remaining key with index n replace the key with index (n-1)
    //For example, assuming key-store size is 3. [key1, key2, key3] -> [key2, key3]. There is space for inserting new key
    private void rotateKeys(String path) {
        for (int index = 2; index <= keyStoreSize; index++) {
            String nextCryptoKeyAlias = cryptoKeyPrefix + index;
            SecretKey nextCryptoKey = getSecreteKey(nextCryptoKeyAlias);
            String nextAuthKeyAlias = authKeyPrefix + index;
            SecretKey nextAuthKey = getSecreteKey(nextAuthKeyAlias);

            //Over-ridden key with its next key
            int last = index - 1;
            String lastCryptoKeyAlias = cryptoKeyPrefix + last;
            storingKeyWithAlias(nextCryptoKey, lastCryptoKeyAlias, path);
            String lastAuthKeyAlias = authKeyPrefix + last;
            storingKeyWithAlias(nextAuthKey, lastAuthKeyAlias, path);

            try {
                keyStore.deleteEntry(nextCryptoKeyAlias);
                keyStore.deleteEntry(nextAuthKeyAlias);
            } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to delete entry from key-store with index: " + index);
            }
        }
    }

    private int getNumOfPairs() {
        int numKeyPairs = 0;
        try {
            numKeyPairs = keyStore.size() / 2;
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get size from key store");
        }
        Log.v("RegistrationContainer", "Number of pairs in key-store: " + numKeyPairs);
        return numKeyPairs;
    }
}
