// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import com.azure.android.storage.blob.StorageBlobClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Package private.
 *
 * A map of configured {@link StorageBlobClient} used by {@link UploadHandler} and {@link DownloadHandler} instances to
 * make API calls.
 */
final class StorageBlobClientsMap {
    private static Map<Long, StorageBlobClient> clientsMap = new ConcurrentHashMap<>();

    /**
     * Set a {@link StorageBlobClient} to be used for a transfer.
     *
     * @param transferId The transfer ID.
     * @param client The client.
     */
    static void put(long transferId, StorageBlobClient client) {
        clientsMap.put(transferId, client);
    }

    /**
     * Get the {@link StorageBlobClient} to be used for a transfer.
     *
     * @param transferId The transfer ID.
     * @return the client
     */
    static StorageBlobClient get(long transferId) {
        return clientsMap.get(transferId);
    }

    /**
     * Removes {@link StorageBlobClient} set for a transfer.
     *
     * @param transferId The transfer ID.
     */
    static void remove(long transferId) {
        clientsMap.remove(transferId);
    }

    /**
     * Removes all {@link StorageBlobClient} in the map.
     */
    static void clear() {
        clientsMap.clear();
    }
}
