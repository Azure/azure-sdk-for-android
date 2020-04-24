// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.annotation.NonNull;

import com.azure.android.storage.blob.StorageBlobClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Package private.
 *
 * A map containing the {@link StorageBlobClient} to be used for low-level storage
 * service calls.
 */
final class StorageBlobClientMap {
    /**
     * Map with key as user defined unique identifier and value as associated
     * blob storage client.
     */
    private Map<String, StorageBlobClient> map = new ConcurrentHashMap<>();

    /**
     * Copies all entries from the given map to this map.
     *
     * @param storageBlobClientMap the blob storage client mapping to be stored in this map
     */
    void putAll(@NonNull Map<String, StorageBlobClient> storageBlobClientMap) {
        this.map.putAll(storageBlobClientMap);
    }

    /**
     * Get the {@link StorageBlobClient} for a specified id.
     *
     * @param storageBlobClientId the unique id of the {@link StorageBlobClient} to retrieve
     * @return the blob storage client if exists, null otherwise
     */
    StorageBlobClient get(String storageBlobClientId) {
        return this.map.get(storageBlobClientId);
    }

    /**
     * Check if the map contains a {@link StorageBlobClient} for the specified id.
     *
     * @param storageBlobClientId the unique id of the blob storage client
     * @return {@code true} if this map contains a blob storage client for the specified id
     */
    boolean isExists(@NonNull String storageBlobClientId) {
        return this.map.containsKey(storageBlobClientId);
    }

    /**
     * Check this map is empty.
     *
     * @return {@code true} if this map contains no entries
     */
    boolean isEmpty() {
        return this.map.isEmpty();
    }
}
