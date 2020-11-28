// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;

/**
 * Statistics for a given index. Statistics are collected periodically and are
 * not guaranteed to always be up-to-date.
 */
@Fluent
public final class SearchIndexStatistics {
    /*
     * The number of documents in the index.
     */
    @SerdeProperty(value = "documentCount")
    private long documentCount;

    /*
     * The amount of storage in bytes consumed by the index.
     */
    @SerdeProperty(value = "storageSize")
    private long storageSize;

    /**
     * Constructor of {@link SearchIndexStatistics}
     * @param documentCount The number of documents in the index.
     * @param storageSize The amount of storage in bytes consumed by the index.
     */
    @SerdeToPojo
    public SearchIndexStatistics(
        @SerdeProperty(value = "documentCount")
            long documentCount,
        @SerdeProperty(value = "storageSize")
            long storageSize) {
        this.documentCount = documentCount;
        this.storageSize = storageSize;
    }


    /**
     * Get the documentCount property: The number of documents in the index.
     *
     * @return the documentCount value.
     */
    public long getDocumentCount() {
        return this.documentCount;
    }

    /**
     * Get the storageSize property: The amount of storage in bytes consumed by
     * the index.
     *
     * @return the storageSize value.
     */
    public long getStorageSize() {
        return this.storageSize;
    }
}
