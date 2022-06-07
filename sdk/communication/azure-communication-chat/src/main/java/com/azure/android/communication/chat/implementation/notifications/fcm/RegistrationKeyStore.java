// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.util.Log;

import com.azure.android.core.logging.ClientLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a storage facility for secrete key as string and its creation time. The
 * data is persisted in a file as json format. The key-store itself could load data from file for
 * faster operation.
 *
 * The file persists all the data in Json format. For example:
 * {
 *     "alias_1": {
 *         "credential": "abcdefgh",
 *         "creationTime": "1234";
 *     },
 *     "alias_2": {
 *         "credential": "abcdefgh",
 *         "creationTime": "1234";
 *     }
 * }
 */
public class RegistrationKeyStore {
    private Map<String, RegistrationKeyEntry> map;

    private ClientLogger clientLogger = new ClientLogger(RegistrationKeyStore.class);

    public RegistrationKeyStore() {
        map = new HashMap<>();
    }

    public int getSize() {
        return map.size();
    }

    //Loading data with given path to memory
    public void loading(InputStream inputStream) {
        //Nothing to read from
        if (inputStream == null) {
            return;
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, RegistrationKeyEntry>>() {}.getType();
            Reader reader = new InputStreamReader(inputStream);
            map = gson.fromJson(reader, type);
            reader.close();
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException(e));
        }
    }

    public void storingKeyEntry(String alias, String path, RegistrationKeyEntry registrationKeyEntry) {
        map.put(alias, registrationKeyEntry);
        writeJsonToFile(path);
    }

    public void deleteEntry(String alias, String path) {
        map.remove(alias);
        writeJsonToFile(path);
    }

    //Write in-memory map into file with Json format
    private void writeJsonToFile(String path) {
        File outputFile = new File(path);
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            clientLogger.logExceptionAsError(new RuntimeException("Failed to create key store file", e));
        }
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map, map.getClass());

        try(FileOutputStream fos = new FileOutputStream(outputFile);
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

    public RegistrationKeyEntry getKeyEntry(String alias) {
        return map.get(alias);
    }

    public static class RegistrationKeyEntry {
        private String credential;

        private long creationTime;

        public String getCredential() {
            return credential;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public RegistrationKeyEntry(String credential, long creationTime) {
            this.credential = credential;
            this.creationTime = creationTime;
        }
    }
}
