// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.util.Log;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class represents a storage facility for secret key as string and its creation time. The
 * data is persisted in a file as json format. The key-store itself could load data from file for
 * faster operation.
 *
 * The file persists all the data in Json format. Latest record is added to last of the list. For example:
 * [
 *     {
 *         "abcdefgh",
 *         "ijklmno"
 *         "1234";
 *     },
 *     {
 *         "oklliabe",
 *         "abcdefgh",
 *         "1236";
 *     }
 * ]
 */
public class RegistrationKeyStore {
    private List<RegistrationKeyEntry> list;

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyStore.class);

    public RegistrationKeyStore() {
        list = new ArrayList<>();
    }

    public int getSize() {
        return list.size();
    }

    //Loading data with given path to memory
    public void load(InputStream inputStream) {
        //Nothing to read from
        if (inputStream == null) {
            return;
        }

        // convert JSON array to Java List
        try {
            list = new ObjectMapper().readValue(inputStream, new TypeReference<List<RegistrationKeyEntry>>() { });
            for (int i=0; i<list.size(); i++) {
                RegistrationKeyEntry entry = list.get(i);
                long diff = (System.currentTimeMillis() - entry.getCreationTime()) / (1000 * 60) ;
            }
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException(e));
        }
    }

    //Write in-memory map into file with Json format
    private void writeJsonToFile(String path) {
        File outputFile = new File(path);
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to create key store file", e));
        }
        String jsonStr = null;
        try {
            jsonStr = new ObjectMapper().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to generate JSON object", e));
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            //convert string to byte array
            byte[] bytes = jsonStr.getBytes();
            //write byte array to file
            bos.write(bytes);
            bos.close();
            fos.close();
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Filed writing key map to file", e));
        }
    }

    public void storeKeyEntry(String path, RegistrationKeyEntry registrationKeyEntry) {
        list.add(list.size(), registrationKeyEntry);
        writeJsonToFile(path);
    }

    public void deleteFirstEntry(String path) {
        list.remove(0);
        writeJsonToFile(path);
    }

    public RegistrationKeyEntry getFirstEntry() {
        return list.get(0);
    }

    public RegistrationKeyEntry getLastEntry() {
        return list.get(list.size() - 1);
    }

    public Stack<RegistrationKeyEntry> getAllEntries() {
        Stack<RegistrationKeyEntry> stack = new Stack<>();
        stack.addAll(list);
        return stack;
    }

    @JsonIgnoreProperties(ignoreUnknown = true, value = {"authKey", "cryptoKey"})
    public static class RegistrationKeyEntry {
        @JsonProperty("cryptoCredential")
        private String cryptoCredential;

        @JsonProperty("authCredential")
        private String authCredential;

        private long creationTime;

        public SecretKey getCryptoKey() {
            return secretKeyFromStr(cryptoCredential);
        }

        public SecretKey getAuthKey() {
            return secretKeyFromStr(authCredential);
        }

        public long getCreationTime() {
            return creationTime;
        }

        private SecretKey secretKeyFromStr(String str) {
            byte[] bytes = Base64Util.decodeString(str);
            return new SecretKeySpec(bytes, 0, bytes.length, "AES");
        }

        public RegistrationKeyEntry() {

        }

        public RegistrationKeyEntry(String cryptoCredential, String authCredential, long creationTime) {
            this.cryptoCredential = cryptoCredential;
            this.authCredential = authCredential;
            this.creationTime = creationTime;
        }
    }
}
