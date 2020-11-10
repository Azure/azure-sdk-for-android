// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.CpkInfo;

import java.util.Objects;

/**
 * Extended options that may be passed when downloading a blob.
 */
@Fluent
public class BlobRawDownloadOptions {

    private final String containerName;
    private final String blobName;
    private String snapshot;
    private BlobRange range;
    private CpkInfo cpkInfo;
    private BlobRequestConditions requestConditions;
    private Boolean retrieveContentRangeMd5;
    private Boolean retrieveContentRangeCrc64;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     * @param blobName The blob name.
     */
    public BlobRawDownloadOptions(@NonNull String containerName, @NonNull String blobName) {
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
    public BlobRawDownloadOptions setSnapshot(@Nullable String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * @return {@link BlobRange}
     */
    @Nullable
    public BlobRange getRange() {
        return range;
    }

    /**
     * @param range {@link BlobRange}
     * @return The updated options.
     */
    @NonNull
    public BlobRawDownloadOptions setRange(@Nullable BlobRange range) {
        this.range = range;
        return this;
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
    public BlobRawDownloadOptions setCpkInfo(@Nullable CpkInfo cpkInfo) {
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
    public BlobRawDownloadOptions setRequestConditions(@Nullable BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return Whether the contentMD5 for the specified blob range should be returned.
     */
    @Nullable
    public Boolean isRetrieveContentRangeMd5() {
        return retrieveContentRangeMd5;
    }

    /**
     * @param retrieveContentRangeMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @return The updated options.
     */
    @NonNull
    public BlobRawDownloadOptions setRetrieveContentRangeMd5(@Nullable Boolean retrieveContentRangeMd5) {
        this.retrieveContentRangeMd5 = retrieveContentRangeMd5;
        return this;
    }

    /**
     * @return Whether the CRC-64 hash for the specified blob range should be returned.
     */
    @Nullable
    public Boolean isRetrieveContentRangeCrc64() {
        return retrieveContentRangeCrc64;
    }

    /**
     * @param retrieveContentRangeCrc64 Whether the CRC-64 hash for the specified blob range should be returned.
     * @return The updated options.
     */
    @NonNull
    public BlobRawDownloadOptions setRetrieveContentRangeCrc64(@Nullable Boolean retrieveContentRangeCrc64) {
        this.retrieveContentRangeCrc64 = retrieveContentRangeCrc64;
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
    public BlobRawDownloadOptions setTimeout(@Nullable Integer timeout) {
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
    public BlobRawDownloadOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
