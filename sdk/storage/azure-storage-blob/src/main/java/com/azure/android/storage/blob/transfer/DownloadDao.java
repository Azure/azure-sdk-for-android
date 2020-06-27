// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * Package private.
 *
 * The Data Access Object exposing operations to store and retrieve download metadata.
 *
 * @see BlobDownloadEntity
 * @see BlockDownloadEntity
 */
@Dao
abstract class DownloadDao {
    /**
     * Create blob metadata for a download.
     *
     * @param blob The blob download metadata.
     * @param blocks The collection of block download metadata.
     * @return The blob download metadata key (a.k.a. downloadId).
     */
    @Transaction
    public String createDownloadRecord(BlobDownloadEntity blob, List<BlockDownloadEntity> blocks) {
        insert(blob);

        for (BlockDownloadEntity block : blocks) {
            block.setBlobKey(blob.key);
            insert(block);
        }

        return blob.key;
    }

    /**
     * Get the blob metadata for a download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @return {@link DownloadRecord} instance including blob metadata.
     */
    @Transaction
    @Query("SELECT * FROM blobdownloads where `key` = :blobKey limit 1")
    public abstract DownloadRecord getDownloadRecord(long blobKey);

    /**
     * Get the blob download metadata for a download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @return The blob download metadata.
     */
    @Query("SELECT * FROM blobdownloads where `key` = :blobKey limit 1")
    public abstract BlobDownloadEntity getBlob(String blobKey);

    /**
     * Get the collection of block download metadata for a blob download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @return The collection of block download metadata.
     */
    @Query("SELECT * FROM blockdownloads where `blob_key` = :blobKey")
    public abstract List<BlockDownloadEntity> getBlocks(String blobKey);

    /**
     * Get the filtered collection of block download metadata for a blob download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @param skipStates The state of the metadata entries to be skipped.
     * @return The collection of block download metadata.
     */
    @Query("SELECT * FROM blockdownloads where `blob_key` = :blobKey and block_download_state NOT IN (:skipStates)")
    public abstract List<BlockDownloadEntity> getBlocks(String blobKey, List<BlockTransferState> skipStates);

    /**
     * Get the collection of block IDs for a blob download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @return The base64 block IDs.
     */
    @Query("SELECT block_id FROM blockdownloads where `blob_key` = :blobKey")
    public abstract List<String> getBlockIds(String blobKey);

    /**
     * Insert a blob's download metadata.
     *
     * @param blobDownloadEntity The blob download metadata.
     */
    @Insert
    public abstract void insert(BlobDownloadEntity blobDownloadEntity);

    /**
     * Insert a block's download metadata.
     *
     * @param blockDownloadEntity The block download metadata.
     */
    @Insert
    public abstract void insert(BlockDownloadEntity blockDownloadEntity);

    /**
     * Update the download state in a block download metadata entity.
     *
     * @param blockKey The block download metadata entity key.
     * @param state The download state.
     */
    @Query("UPDATE blockdownloads SET block_download_state=:state WHERE `key` = :blockKey")
    public abstract void updateBlockState(String blockKey, BlockTransferState state);

    /**
     * Update the download state field of a blob download metadata entity.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @param state The download state.
     */
    @Query("UPDATE blobdownloads SET blob_download_state=:state WHERE `key` = :blobKey")
    public abstract void updateBlobState(String blobKey, BlobTransferState state);

    /**
     * Update the blob size field of a blob download metadata entity.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @param blobSize The size of the blob to download.
     */
    @Query("UPDATE blobdownloads SET blob_size=:blobSize WHERE `key` = :blobKey")
    public abstract void updateBlobSize(long blobKey, long blobSize);

    /**
     * Update the interrupted state field of a blob download metadata entity.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @param state The interrupted state.
     */
    @Query("UPDATE blobdownloads SET transfer_interrupt_state=:state WHERE `key` = :blobKey")
    public abstract void updateDownloadInterruptState(String blobKey, TransferInterruptState state);

    /**
     * Get the interrupted state from a blob download metadata entity.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @return The interrupted state.
     */
    @Query("SELECT transfer_interrupt_state FROM blobdownloads where `key` = :blobKey limit 1")
    public abstract TransferInterruptState getTransferInterruptState(String blobKey);

    /**
     * Get the total bytes downloaded so far for a blob download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @return The bytes downloaded.
     */
    public long getDownloadedBytesCount(String blobKey) {
        return getAggregatedBlockSize(blobKey, BlockTransferState.COMPLETED);
    }

    /**
     * Get the sum of size of blocks for a blob download.
     *
     * @param blobKey The blob download metadata key (a.k.a. downloadId).
     * @param state The state of the blocks to be considered for aggregation.
     * @return The aggregated block size.
     */
    @Query("SELECT SUM(block_size) FROM blockdownloads WHERE blob_key = :blobKey and block_download_state = :state")
    public abstract long getAggregatedBlockSize(String blobKey, BlockTransferState state);
}
