// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import com.azure.android.storage.blob.StorageBlobClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Package private.
 *
 * A map of configured {@link StorageBlobClient} used by {@link UploadHandler} instances to
 * make API calls.
 */
final class StorageBlobClientsMap {
    private static Map<Long, StorageBlobClient> clientsMap = new ConcurrentHashMap<>();

    /**
     * Set a {@link StorageBlobClient} to be used for an upload.
     *
     * @param uploadId the upload id
     * @param client the client
     */
    static void put(long uploadId, StorageBlobClient client) {
        clientsMap.put(uploadId, client);
    }

    /**
     * Get the {@link StorageBlobClient} to be used for an upload.
     *
     * @param uploadId the upload id
     * @return the client
     */
    static StorageBlobClient get(long uploadId) {
        return clientsMap.get(uploadId);
    }

    /**
     * Removes {@link StorageBlobClient} set for an upload.
     *
     * @param uploadId the upload id
     */
    static void remove(long uploadId) {
        clientsMap.remove(uploadId);
    }

    /**
     * Removes all {@link StorageBlobClient} in the map.
     */
    static void clear() {
        clientsMap.clear();
    }
}
