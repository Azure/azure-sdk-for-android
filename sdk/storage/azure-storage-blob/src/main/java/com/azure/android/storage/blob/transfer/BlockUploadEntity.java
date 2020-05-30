// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.azure.android.core.util.Base64Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static androidx.room.ForeignKey.CASCADE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Package private.
 *
 * Represents metadata for a block upload.
 *
 * There is one-to-many relationship between a {@link BlobUploadEntity} and a set
 * of {@link BlockUploadEntity}. Each {@link BlockUploadEntity} in the set
 * describes a chunk of the file referenced by the {@link BlobUploadEntity}
 *
 * The Data Access Object type {@link UploadDao} exposes DB store and read methods on this model.
 *
 * @see TransferDatabase
 */
@Entity(tableName = "blockuploads",
    foreignKeys = @ForeignKey(entity = BlobUploadEntity.class,
        parentColumns = "key",
        childColumns = "blob_key",
        onDelete = CASCADE),
    indices = {@Index("blob_key")})
final class BlockUploadEntity {
    /**
     * A unique key for the block upload metadata.
     *
     * This key identifies the metadata in the local store, which is different
     * from {@link BlockUploadEntity#blockId}. Block id is used by storage service
     * is uniquely identify the block.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "key")
    public Long key;
    /**
     * The key of {@link BlobUploadEntity} that this {@link BlockUploadEntity} belongs to.
     */
    @ColumnInfo(name = "blob_key")
    public long blobKey;
    /**
     * The offset in the content from which the block starts.
     */
    @ColumnInfo(name = "block_offset")
    public int blockOffset;
    /**
     * The block size in bytes.
     */
    @ColumnInfo(name = "block_size")
    public int blockSize;
    /**
     * The base64 block id used by the storage service to uniquely identify the block.
     */
    @ColumnInfo(name = "block_id")
    public String blockId;
    /**
     * The current state of the block upload operation.
     */
    @ColumnInfo(name = "block_upload_state")
    @TypeConverters(ColumnConverter.class)
    public volatile BlockTransferState state;
    /**
     * holds the exception indicating the reason for block staging (upload) failure.
     *
     * This is not persisted
     */
    @Ignore
    private Throwable stagingError;

    /**
     * Creates BlockUploadEntity, this constructor is used by Room library
     * when re-hydrating metadata from local store.
     */
    public BlockUploadEntity() {}

    /**
     * Create a new BlockUploadEntity to persist in local store.
     *
     * @param blockId the base64 block id
     * @param blockOffset the offset in the content from which the block starts
     * @param blockSize the block size in bytes
     */
    private BlockUploadEntity(String blockId, int blockOffset, int blockSize) {
        Objects.requireNonNull(blockId);
        this.blockId = blockId;
        this.blockOffset = blockOffset;
        this.blockSize = blockSize;
        this.state = BlockTransferState.WAIT_TO_BEGIN;
    }

    /**
     * Set the {@link BlobUploadEntity#key} for this block. This is the foreign key referring
     * the BlobUploadEntity this block is a part of.
     *
     * @param blobKey the block key (aka uploadId)
     */
    void setBlobKey(long blobKey) {
        this.blobKey = blobKey;
    }

    /**
     * Set the block staging failure error.
     *
     * @param t the error
     */
    void setStagingError(Throwable t) {
        this.stagingError = t;
    }

    /**
     * Get the block staging (upload) failure error.
     *
     * @return the staging failure error or null if there is no error
     */
    Throwable getStagingError() {
        return this.stagingError;
    }

    /**
     * Factory method to create a collection of {@link BlockUploadEntity} describing content blocks to upload.
     *
     * @param contentSize the total size of the content to upload
     * @param blockSize the size of one block in the content
     * @return the collection of {@link BlockUploadEntity} describing each block of the content to upload
     */
    static List<BlockUploadEntity> createBlockEntities(long contentSize,
                                                       int blockSize) {
        final List<BlockUploadEntity> blockUploadEntities = new ArrayList<>();
        if (contentSize <= blockSize) {
            final String blockId = Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
            BlockUploadEntity blockUploadEntity = new BlockUploadEntity(
                blockId,
                0,
                (int) contentSize);
            blockUploadEntities.add(blockUploadEntity);
        } else {
            long remainingLength = contentSize;
            int fileOffset = 0;
            int blocksCount = (int) Math.ceil(remainingLength / (double) blockSize);
            for (int i = 0; i < blocksCount; i++) {
                final String blockId = Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
                final int currentBlockLength = (int) Math.min(blockSize, remainingLength);
                BlockUploadEntity blockUploadEntity = new BlockUploadEntity(
                    blockId,
                    fileOffset,
                    currentBlockLength);
                blockUploadEntities.add(blockUploadEntity);
                fileOffset += currentBlockLength;
                remainingLength -= currentBlockLength;
            }
        }
        return blockUploadEntities;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" key:" + this.key);
        builder.append(" blobKey:" + this.blobKey);
        builder.append(" blockOffset:" + this.blockOffset);
        builder.append(" blockSize:" + this.blockSize);
        builder.append(" state:" + this.state);
        if (this.stagingError != null) {
            builder.append(" stagingError:" + this.stagingError.getMessage());
        }
        return builder.toString();
    }
}
