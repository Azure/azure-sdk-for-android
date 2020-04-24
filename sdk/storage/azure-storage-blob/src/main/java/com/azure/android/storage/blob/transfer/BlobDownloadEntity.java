// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.File;
import java.util.Objects;

/**
 * Package private.
 *
 * Represents metadata for a single blob download.
 *
 * The Data Access Object type {@link DownloadDao} exposes the DB store and read methods on this model.
 *
 * @see TransferDatabase
 */
@Entity(tableName = "blobdownloads")
final class BlobDownloadEntity {
    /**
     * A unique key for the blob download metadata.
     *
     * Also referred as downloadId that users use to identify and manage the download operation.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "key")
    public Long key;

    /**
     * The name of the Azure Storage Container to download the blob from.
     */
    @ColumnInfo(name = "container_name")
    public String containerName;

    /**
     * The name of the Azure Storage blob to download.
     */
    @ColumnInfo(name = "blob_name")
    public String blobName;

    /**
     * The blob size in bytes.
     */
    @ColumnInfo(name = "blob_size")
    public long blobSize;

    /**
     * The absolute path to the file to be uploaded as blob.
     */
    @ColumnInfo(name = "file_path")
    public String filePath;

    /**
     * The current state of the blob download operation.
     */
    @ColumnInfo(name = "blob_download_state")
    @TypeConverters(ColumnConverter.class)
    public volatile BlobTransferState state;

    /**
     * Indicate the reason for interrupting (stopping) blob download.
     */
    @ColumnInfo(name = "transfer_interrupt_state")
    @TypeConverters(ColumnConverter.class)
    public TransferInterruptState interruptState;

    /**
     * Holds the exception indicating the reason for download failure.
     *
     * This is not persisted.
     */
    @Ignore
    private Throwable downloadError;

    /**
     * Creates BlobDownloadEntity, this constructor is used by Room library
     * when re-hydrating metadata from local store.
     */
    public BlobDownloadEntity() {}

    /**
     * Create a new BlobDownloadEntity to persist in local store.
     *
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param file The local file.
     */
    @Ignore
    BlobDownloadEntity(String containerName,
                       String blobName,
                       File file) {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        Objects.requireNonNull(file);

        this.containerName = containerName;
        this.blobName = blobName;
        filePath = file.getAbsolutePath();
        state = BlobTransferState.WAIT_TO_BEGIN;
        interruptState = TransferInterruptState.NONE;
    }

    /**
     * Set the download failure error.
     *
     * @param t The error
     */
    void setDownloadError(Throwable t) {
        downloadError = t;
    }

    /**
     * Get the download failure error.
     *
     * @return The download failure error or null if there is no error.
     */
    Throwable getDownloadError() {
        return downloadError;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(" key:").append(key)
            .append(" containerName:").append(containerName)
            .append(" blobName:").append(blobName)
            .append(" filePath:").append(filePath)
            .append(" state:").append(state)
            .append(" interruptState:").append(interruptState);

        if (downloadError != null) {
            builder.append(" downloadError:").append(downloadError.getMessage());
        }

        return builder.toString();
    }
}
