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
 * Represents metadata for a block download.
 *
 * There is one-to-many relationship between a {@link BlobDownloadEntity} and a set of {@link BlockDownloadEntity}.
 * Each {@link BlockDownloadEntity} in the set describes a chunk of the file referenced by the
 * {@link BlobDownloadEntity}
 *
 * The Data Access Object type {@link DownloadDao} exposes DB store and read methods on this model.
 *
 * @see TransferDatabase
 */
@Entity(tableName = "blockdownloads",
    foreignKeys = @ForeignKey(entity = BlobDownloadEntity.class,
        parentColumns = "key",
        childColumns = "blob_key",
        onDelete = CASCADE),
    indices = {@Index("blob_key")})
final class BlockDownloadEntity {
    /**
     * A unique key for the block download metadata.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "key")
    public Long key;

    /**
     * The key of {@link BlobDownloadEntity} that this {@link BlockDownloadEntity} belongs to.
     */
    @ColumnInfo(name = "blob_key")
    public long blobKey;

    /**
     * The absolute path to the file that the block is a part of.
     */
    @ColumnInfo(name = "file_path")
    public String filePath;

    /**
     * The offset in the blob from which block contents starts.
     */
    @ColumnInfo(name = "block_offset")
    public long blockOffset;

    /**
     * The block size in bytes.
     */
    @ColumnInfo(name = "block_size")
    public long blockSize;

    /**
     * The base64 block ID used to uniquely identify the block.
     */
    @ColumnInfo(name = "block_id")
    public String blockId;

    /**
     * The current state of the block download operation.
     */
    @ColumnInfo(name = "block_download_state")
    @TypeConverters(ColumnConverter.class)
    public volatile BlockTransferState state;

    /**
     * Holds the exception indicating the reason for block download failure.
     *
     * This is not persisted.
     */
    @Ignore
    private Throwable downloadError;

    /**
     * Creates BlockDownloadEntity, this constructor is used by Room library when re-hydrating metadata from local
     * store.
     */
    public BlockDownloadEntity() {}

    /**
     * Create a new BlockDownloadEntity to persist in local store.
     *
     * @param blockId The base64 block ID
     * @param blockOffset The offset in the blob from which block contents starts.
     * @param blockSize The block size in bytes.
     */
    private BlockDownloadEntity(String blockId, long blockOffset, long blockSize) {
        Objects.requireNonNull(blockId);

        this.blockId = blockId;
        this.blockOffset = blockOffset;
        this.blockSize = blockSize;
        this.state = BlockTransferState.WAIT_TO_BEGIN;
    }

    /**
     * Set the {@link BlobDownloadEntity#key} for this block. This is the foreign key referring
     * the BlobDownloadEntity this block is a part of.
     *
     * @param blobKey The blob key (a.k.a. downloadId).
     */
    void setBlobKey(long blobKey) {
        this.blobKey = blobKey;
    }

    /**
     * Set the block download failure error.
     *
     * @param t The error.
     */
    void setDownloadError(Throwable t) {
        this.downloadError = t;
    }

    /**
     * Get the block download failure error.
     *
     * @return The download failure error or null if there is no error.
     */
    Throwable getDownloadError() {
        return this.downloadError;
    }

    /**
     * Factory method to create a collection of {@link BlockDownloadEntity} for a blob.
     *
     * @param blobSize The size of the blob to download.
     * @param blockSize The size of one block in the blob.
     * @return A collection of {@link BlockDownloadEntity} describing each block of the blob to download.
     */
    static List<BlockDownloadEntity> createBlockEntities(long blobSize,
                                                         long blockSize) {
        final List<BlockDownloadEntity> blockDownloadEntities = new ArrayList<>();

        if (blobSize <= blockSize) {
            final String blockId = Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));

            BlockDownloadEntity blockDownloadEntity = new BlockDownloadEntity(blockId,
                0,
                (int) blobSize);

            blockDownloadEntities.add(blockDownloadEntity);
        } else {
            long remainingLength = blobSize;
            long blobOffset = 0;
            int blocksCount = (int) Math.ceil(remainingLength / (double) blockSize);

            for (int i = 0; i < blocksCount; i++) {
                final String blockId = Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
                final long currentBlockLength = Math.min(blockSize, remainingLength);

                BlockDownloadEntity blockDownloadEntity = new BlockDownloadEntity(blockId,
                    blobOffset,
                    currentBlockLength);

                blockDownloadEntities.add(blockDownloadEntity);

                blobOffset += currentBlockLength;
                remainingLength -= currentBlockLength;
            }
        }

        return blockDownloadEntities;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(" key:").append(this.key)
            .append(" blobKey:").append(this.blobKey)
            .append(" filePath:").append(this.filePath)
            .append(" blockOffset:").append(this.blockOffset)
            .append(" blockSize:").append(this.blockSize)
            .append(" state:").append(this.state);

        if (this.downloadError != null) {
            builder.append(" downloadError:").append(this.downloadError.getMessage());
        }

        return builder.toString();
    }
}
