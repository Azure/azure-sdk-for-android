// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.BlobRequestConditions;

import java.util.Objects;

/**
 * Extended options that may be passed when getting tags for a blob.
 */
@Fluent
public class BlobGetTagsOptions {

    private final String containerName;
    private final String blobName;
    private String snapshot; /*  TODO: (gapra) : Should  we even expose this since we cant make snapshots anyway?*/
    private BlobRequestConditions requestConditions;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     * @param blobName The blob name.
     */
    public BlobGetTagsOptions(@NonNull String containerName, @NonNull String blobName) {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        this.containerName = containerName;
        this.blobName = blobName;
    }

    /**
     * @return The container name.
     */
    @NonNull
    public String getContainerName() {
        return containerName;
    }

    /**
     * @return The blob name.
     */
    @NonNull
    public String getBlobName() {
        return blobName;
    }

    /**
     * @return the snapshot identifier for the blob.
     */
    @Nullable
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * @param snapshot The snapshot parameter is an opaque DateTime value that, when present, specifies the blob
     * snapshot to retrieve. For more information on working with blob snapshots,
     * see <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/creating-a-snapshot-of-a-blob"></a>Creating a Snapshot of a Blob</a>.
     * @return The updated options.
     */
    @NonNull
    public BlobGetTagsOptions setSnapshot(@Nullable String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions}.
     */
    @Nullable
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    @NonNull
    public BlobGetTagsOptions setRequestConditions(@Nullable BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return The timeout parameter expressed in seconds.
     */
    @Nullable
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout parameter expressed in seconds. For more information, see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations">Setting Timeouts for Blob Service Operations</a>.
     * @return The updated options.
     */
    @NonNull
    public BlobGetTagsOptions setTimeout(@Nullable Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @return The token to request cancellation.
     */
    @Nullable
    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }

    /**
     * @param cancellationToken The token to request cancellation.
     * @return The updated options.
     */
    @NonNull
    public BlobGetTagsOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
