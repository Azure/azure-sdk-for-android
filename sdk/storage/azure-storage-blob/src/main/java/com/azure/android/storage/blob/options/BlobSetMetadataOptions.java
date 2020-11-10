// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.CpkInfo;

import java.util.Map;
import java.util.Objects;

/**
 * Extended options that may be passed when setting metadata of a blob.
 */
@Fluent
public class BlobSetMetadataOptions {

    private final String containerName;
    private final String blobName;
    private final Map<String, String> metadata;
    private CpkInfo cpkInfo; /* TODO: (gapra) : Should this be exposed as a handwrapped type? */
    private BlobRequestConditions requestConditions;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param metadata The metadata to associate with the blob.
     */
    public BlobSetMetadataOptions(@NonNull String containerName, @NonNull String blobName, @Nullable Map<String, String> metadata) {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        this.containerName = containerName;
        this.blobName = blobName;
        this.metadata = metadata;
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
     * @return The metadata to associate with the blob.
     */
    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return {@link CpkInfo}.
     */
    @Nullable
    public CpkInfo getCpkInfo() {
        return cpkInfo;
    }

    /**
     * @param cpkInfo {@link CpkInfo}
     * @return The updated options.
     */
    @NonNull
    public BlobSetMetadataOptions setCpkInfo(@Nullable CpkInfo cpkInfo) {
        this.cpkInfo = cpkInfo;
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
    public BlobSetMetadataOptions setRequestConditions(@Nullable BlobRequestConditions requestConditions) {
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
    public BlobSetMetadataOptions setTimeout(@Nullable Integer timeout) {
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
    public BlobSetMetadataOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
