// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import com.azure.android.core.logging.ClientLogger;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a storage facility for storing alias and its creation time of secret keys. The
 * data is persisted in a file as json format. The key-store itself could load data from file for
 * faster operation.
 *
 * The file persists all the data in Json format. Latest record is added to last of the list. For example:
 * {
 *     "key_1": 1234,
 *     "key_2": 1236
 *
 * }
 */
public class KeyMetaDataStore {
    private Map<String, KeyMetaDataEntry> map;

    private ClientLogger clientLogger = new ClientLogger(KeyMetaDataStore.class);

    public KeyMetaDataStore() {
        map = new HashMap<>();
    }

    public int getSize() {
        return map.size();
    }

    //Loading data with given path to memory
    public void load(InputStream inputStream) {
        //Nothing to read from
        if (inputStream == null) {
            return;
        }

        // convert JSON array to map
        try {
            map = new ObjectMapper().readValue(inputStream, new TypeReference<Map<String, KeyMetaDataEntry>>() { });
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException(e));
        }
    }

    //Write in-memory map into file with Json format
    private void writeJsonToFile(String path) {
        File outputFile = new File(path);
        try {
            boolean newFile = outputFile.createNewFile();
            if (newFile) {
                clientLogger.verbose("new file created for storing push notification credentials");
            }
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to create key store file", e));
        }
        String jsonStr = "";
        try {
            jsonStr = new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to generate JSON object", e));
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            //convert string to byte array
            byte[] bytes = jsonStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            //write byte array to file
            bos.write(bytes);
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Filed writing key map to file", e));
        }
    }

    public void deleteEntry(String filePath, String alias) {
        map.remove(alias);
        writeJsonToFile(filePath);
    }

    public Long getCreationTime(String alias) {
        if (!map.containsKey(alias)) {
            return null;
        }
        return map.get(alias).creationTime;
    }

    public KeyMetaDataEntry getEntry(String alias) {
        return map.get(alias);
    }

    public void storeKeyEntry(String filePath, String alias, KeyMetaDataEntry keyMetaDataEntry) {
        map.put(alias, keyMetaDataEntry);
        writeJsonToFile(filePath);
    }

    public Set<String> getAliases() {
        return map.keySet();
    }

//    @JsonIgnoreProperties(ignoreUnknown = true, value = {"authKey", "cryptoKey"})
    public static class KeyMetaDataEntry {
        @JsonProperty("iv")
        private byte[] iv;

        @JsonProperty("encryptionText")
        private byte[] encryptionText;

        private long creationTime;

        public KeyMetaDataEntry() {
        }

        public byte[] getIV() {
            return iv;
        }

        public byte[] getEncryptionText() {
            return encryptionText;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public KeyMetaDataEntry(byte[] iv, byte[] encryptionText, long creationTime) {
            this.iv = iv;
            this.encryptionText = encryptionText;
            this.creationTime = creationTime;
        }
    }
}
