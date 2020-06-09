// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.work.Constraints;

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
     * The URI to the content where the downloaded blob will be stored.
     */
    @ColumnInfo(name = "content_uri")
    public String contentUri;

    /**
     * Indicate whether android.content.ContentResolver should be used to resolve the contentUri.
     */
    @ColumnInfo(name = "use_content_resolver")
    public boolean useContentResolver;

    /**
     * Identifies the {@link com.azure.android.storage.blob.StorageBlobClient}
     * to be used for the file download.
     * @see StorageBlobClientMap
     */
    @ColumnInfo(name = "storage_blob_client_id")
    public String storageBlobClientId;

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
     * The constraints to be satisfied to run the download operation.
     */
    @Embedded
    @NonNull
    public ConstraintsColumn constraintsColumn = ConstraintsColumn.NONE;
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
     * @param storageBlobClientId identifies the blob storage client to be used
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param blobSize The blob size.
     * @param content Describes the content where the downloaded blob will be stored.
     * @param constraints the constraints to be satisfied to run the download operation.
     */
    @Ignore
    BlobDownloadEntity(String storageBlobClientId,
                       String containerName,
                       String blobName,
                       long blobSize,
                       WritableContent content,
                       Constraints constraints) {
        Objects.requireNonNull(storageBlobClientId);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        Objects.requireNonNull(content);
        Objects.requireNonNull(constraints);

        this.storageBlobClientId = storageBlobClientId;
        this.containerName = containerName;
        this.blobName = blobName;
        this.blobSize = blobSize;
        this.contentUri = content.getUri().toString();
        this.useContentResolver = content.isUsingContentResolver();
        state = BlobTransferState.WAIT_TO_BEGIN;
        interruptState = TransferInterruptState.NONE;
        constraintsColumn = ConstraintsColumn.fromConstraints(constraints);
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
            .append(" blobSize:" + this.blobSize)
            .append(" contentUri:" + this.contentUri)
            .append(" useContentResolver:" + this.useContentResolver)
            .append(" state:").append(state)
            .append(" interruptState:").append(interruptState);

        if (downloadError != null) {
            builder.append(" downloadError:").append(downloadError.getMessage());
        }

        return builder.toString();
    }
}
