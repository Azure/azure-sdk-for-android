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
 * Represents blob upload metadata for a single file upload.
 *
 * The Data Access Object type {@link UploadDao} exposes DB store and read methods on this model.
 *
 * @see TransferDatabase
 */
@Entity(tableName = "blobuploads")
final class BlobUploadEntity {
    /**
     * A unique key for the blob upload metadata.
     *
     * Also referred as uploadId that users use to identify and manage the upload operation.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "key")
    public Long key;
    /**
     * The URI to the content to be uploaded as a blob.
     */
    @ColumnInfo(name = "content_uri")
    public String contentUri;
    /**
     * The content size in bytes (i.e. the total size of the blob once uploaded).
     */
    @ColumnInfo(name = "content_size")
    public long contentSize;
    /**
     * Indicate whether android.content.ContentResolver should be used to resolve the contentUri.
     */
    @ColumnInfo(name = "use_content_resolver")
    public boolean useContentResolver;
    /**
     * Identifies the {@link com.azure.android.storage.blob.StorageBlobAsyncClient}
     * to be used for the file upload.
     * @see StorageBlobClientMap
     */
    @ColumnInfo(name = "storage_blob_client_id")
    public String storageBlobClientId;
    /**
     * The name of the Azure Storage Container to upload the file to.
     */
    @ColumnInfo(name = "container_name")
    public String containerName;
    /**
     * The name of the Azure Storage blob holding uploaded file.
     */
    @ColumnInfo(name = "blob_name")
    public String blobName;
    /**
     * Whether or not the library should calculate the md5 and send it for the service to verify.
     */
    @ColumnInfo(name = "compute_md5")
    public Boolean computeMd5;
    /**
     * The current state of the blob upload operation.
     */
    @ColumnInfo(name = "blob_upload_state")
    @TypeConverters(ColumnConverter.class)
    public volatile BlobTransferState state;
    /**
     * Indicate the reason for interrupting (stopping) blob upload.
     */
    @ColumnInfo(name = "transfer_interrupt_state")
    @TypeConverters(ColumnConverter.class)
    public TransferInterruptState interruptState;
    /**
     * The constraints to be satisfied to run the upload operation.
     */
    @Embedded
    @NonNull
    public ConstraintsColumn constraintsColumn = ConstraintsColumn.NONE;
    /**
     * holds the exception indicating the reason for commit (the last
     * stage of upload) failure.
     *
     * This is not persisted
     */
    @Ignore
    private Throwable commitError;

    /**
     * Creates BlobUploadEntity, this constructor is used by Room library
     * when re-hydrating metadata from local store.
     */
    public BlobUploadEntity() {}

    /**
     * Create a new BlobUploadEntity to persist in local store.
     *
     * @param storageBlobClientId identifies the blob storage client to be used
     * @param containerName the container name
     * @param blobName the blob name
     * @param computeMd5 whether or not the library should calculate the md5 and send it for the service to verify.
     * @param content describes the content to be read while uploading
     * @param constraints The constraints to be satisfied to run the upload operation.
     */
    @Ignore
    BlobUploadEntity(String storageBlobClientId,
                     String containerName,
                     String blobName,
                     Boolean computeMd5,
                     ReadableContent content,
                     Constraints constraints) throws Throwable {
        Objects.requireNonNull(storageBlobClientId);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);
        Objects.requireNonNull(content);
        Objects.requireNonNull(constraints);

        this.contentUri = content.getUri().toString();
        this.contentSize = content.getLength();
        this.useContentResolver = content.isUsingContentResolver();

        this.storageBlobClientId = storageBlobClientId;
        this.containerName = containerName;
        this.blobName = blobName;
        this.computeMd5 = computeMd5;
        this.state = BlobTransferState.WAIT_TO_BEGIN;
        this.interruptState = TransferInterruptState.NONE;
        this.constraintsColumn = ConstraintsColumn.fromConstraints(constraints);
    }

    /**
     * Set the commit (the last stage of upload) failure error.
     *
     * @param t the error
     */
    void setCommitError(Throwable t) {
        this.commitError = t;
    }

    /**
     * Get the commit failure error.
     *
     * @return the commit failure error or null if there is no error
     */
    Throwable getCommitError() {
        return this.commitError;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" key:" + this.key);
        builder.append(" contentUri:" + this.contentUri);
        builder.append(" contentSize:" + this.contentSize);
        builder.append(" useContentResolver:" + this.useContentResolver);
        builder.append(" containerName:" + this.containerName);
        builder.append(" blobName:" + this.blobName);
        builder.append(" computeMd5:" + this.computeMd5);
        builder.append(" state:" + this.state);
        builder.append(" interruptState:" + this.interruptState);
        if (this.commitError != null) {
            builder.append(" commitError:" + this.commitError.getMessage());
        }
        return builder.toString();
    }
}
