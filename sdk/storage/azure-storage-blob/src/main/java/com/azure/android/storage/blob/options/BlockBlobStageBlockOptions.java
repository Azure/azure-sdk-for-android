// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.CoreUtil;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.CpkInfo;

import java.util.Objects;

/**
 * Extended options that may be passed when staging a block for a block blob.
 */
@Fluent
public class BlockBlobStageBlockOptions {

    private final String containerName;
    private final String blobName;
    private final String base64BlockId;
    private final byte[] data;
    private byte[] contentMd5;
    private byte[] contentCrc64;
    private Boolean computeMd5;
    private CpkInfo cpkInfo;
    private BlobRequestConditions requestConditions;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param base64BlockId A Base-64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block.
     */
    public BlockBlobStageBlockOptions(@NonNull String containerName, @NonNull String blobName, @NonNull String base64BlockId, @NonNull byte[] data) {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        Objects.requireNonNull(base64BlockId);
        Objects.requireNonNull(data);
        this.containerName = containerName;
        this.blobName = blobName;
        this.base64BlockId = base64BlockId;
        this.data = CoreUtil.clone(data);
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
     * @return The id for this block.
     */
    @NonNull
    public String getBase64BlockId() {
        return base64BlockId;
    }

    /**
     * @return The data to write to the block.
     */
    @NonNull
    public byte[] getData() {
        return CoreUtil.clone(data.clone());
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
     * Note: This must not be used in conjunction with {@link BlockBlobStageBlockOptions#computeMd5}.
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @return The updated options.
     */
    @NonNull
    public BlockBlobStageBlockOptions setContentMd5(@Nullable byte[] contentMd5) {
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
    public BlockBlobStageBlockOptions setContentCrc64(@Nullable byte[] contentCrc64) {
        this.contentCrc64 = contentCrc64;
        return this;
    }

    /**
     * @return Whether or not the library should calculate the md5 and send it for the service to verify.
     */
    @Nullable
    public Boolean isComputeMd5() {
        return computeMd5;
    }

    /**
     * Note: This must not be used in conjunction with {@link BlockBlobStageBlockOptions#contentMd5}.
     *
     * @param computeMd5 Whether or not the library should calculate the md5 and send it for the service to
     * verify.
     * @return The updated options.
     */
    @NonNull
    public BlockBlobStageBlockOptions setComputeMd5(@Nullable Boolean computeMd5) {
        this.computeMd5 = computeMd5;
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
    public BlockBlobStageBlockOptions setCpkInfo(@Nullable CpkInfo cpkInfo) {
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
    public BlockBlobStageBlockOptions setRequestConditions(@Nullable BlobRequestConditions requestConditions) {
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
    public BlockBlobStageBlockOptions setTimeout(@Nullable Integer timeout) {
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
    public BlockBlobStageBlockOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
