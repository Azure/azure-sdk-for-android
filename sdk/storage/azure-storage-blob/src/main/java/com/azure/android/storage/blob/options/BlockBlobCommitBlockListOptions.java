// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.CpkInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extended options that may be passed when committing a block list on a block blob.
 */
@Fluent
public class BlockBlobCommitBlockListOptions {

    private final String containerName;
    private final String blobName;
    private final List<String> base64BlockIds;
    private byte[] contentMd5;
    private byte[] contentCrc64;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier accessTier;
    private CpkInfo cpkInfo;
    private BlobRequestConditions requestConditions;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     */
    public BlockBlobCommitBlockListOptions(@NonNull String containerName, @NonNull String blobName, @Nullable List<String> base64BlockIds) {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        this.containerName = containerName;
        this.blobName = blobName;
        this.base64BlockIds = base64BlockIds;
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
     * @return The block ids to be committed.
     */
    @Nullable
    public List<String> getBase64BlockIds() {
        return base64BlockIds;
    }

    /**
     * @return An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     */
    @Nullable
    public byte[] getContentMd5() {
        return contentMd5;
    }

    /**
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @return The updated options.
     */
    @NonNull
    public BlockBlobCommitBlockListOptions setContentMd5(@Nullable byte[] contentMd5) {
        this.contentMd5 = contentMd5;
        return this;
    }

    /**
     * @return A CRC64 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     */
    @Nullable
    public byte[] getContentCrc64() {
        return contentCrc64;
    }

    /**
     * @param contentCrc64 A CRC64 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @return The updated options.
     */
    @NonNull
    public BlockBlobCommitBlockListOptions setContentCrc64(@Nullable byte[] contentCrc64) {
        this.contentCrc64 = contentCrc64;
        return this;
    }

    /**
     * @return {@link BlobHttpHeaders}
     */
    @Nullable
    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * @param headers {@link BlobHttpHeaders}
     * @return The updated {@code AppendBlobCreateOptions}
     */
    @NonNull
    public BlockBlobCommitBlockListOptions setHeaders(@Nullable BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return The metadata to associate with the blob.
     */
    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata The metadata to associate with the blob.
     * @return The updated options
     */
    @NonNull
    public BlockBlobCommitBlockListOptions setMetadata(@Nullable Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return The tags to associate with the blob.
     */
    @Nullable
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    @NonNull
    public BlockBlobCommitBlockListOptions setTags(@Nullable Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @return {@link AccessTier}
     */
    @Nullable
    public AccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @param accessTier {@link AccessTier}
     * @return The updated options.
     */
    @NonNull
    public BlockBlobCommitBlockListOptions setAccessTier(@Nullable AccessTier accessTier) {
        this.accessTier = accessTier;
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
    public BlockBlobCommitBlockListOptions setCpkInfo(@Nullable CpkInfo cpkInfo) {
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
    public BlockBlobCommitBlockListOptions setRequestConditions(@Nullable BlobRequestConditions requestConditions) {
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
    public BlockBlobCommitBlockListOptions setTimeout(@Nullable Integer timeout) {
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
    public BlockBlobCommitBlockListOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
