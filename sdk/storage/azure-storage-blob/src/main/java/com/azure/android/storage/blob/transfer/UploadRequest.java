// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.net.Uri;

import androidx.work.Constraints;

import com.azure.android.core.util.CoreUtil;

import java.io.File;
import java.util.Objects;

/**
 * A type specifying parameters for an upload transfer that should be enqueued in {@link TransferClient}.
 */
public final class UploadRequest {
    private final String storageClientId;
    private final String containerName;
    private final String blobName;
    private Boolean computeMd5;
    private final ReadableContent readableContent;
    private final Constraints constraints;

    /**
     * Create UploadRequest.
     *
     * @param storageClientId Identifies the {@link com.azure.android.storage.blob.StorageBlobAsyncClient} for the upload.
     * @param containerName   The name of the container to upload the content to.
     * @param blobName        The name of the target blob holding the uploaded content.
     * @param computeMd5      Whether or not the library should calculate the md5 and send it for the service to verify.
     * @param readableContent The object describing the content in the device that needs to be uploaded.
     * @param constraints     The constraints to be satisfied to execute the upload.
     */
    private UploadRequest(String storageClientId,
                          String containerName,
                          String blobName,
                          Boolean computeMd5,
                          ReadableContent readableContent,
                          Constraints constraints) {
        this.storageClientId = storageClientId;
        this.containerName = containerName;
        this.blobName = blobName;
        this.computeMd5 = computeMd5;
        this.readableContent = readableContent;
        this.constraints = constraints;
    }

    /**
     * Get the unique identifier of the blob storage client to be used for the upload.
     *
     * @return The unique identifier of the {@link com.azure.android.storage.blob.StorageBlobAsyncClient}.
     */
    String getStorageClientId() {
        return this.storageClientId;
    }

    /**
     * Get the name of the container to upload the file to.
     *
     * @return The container name.
     */
    String getContainerName() {
        return this.containerName;
    }

    /**
     * Get the name of the target blob holding uploaded file.
     *
     * @return The blob name.
     */
    String getBlobName() {
        return this.blobName;
    }

    /**
     * Get whether or not the library should calculate the md5 and send it for the service to verify.
     *
     * @return Whether or not the library should calculate the md5 and send it for the service to verify.
     */
    Boolean isComputeMd5() {
        return this.computeMd5;
    }

    /**
     * Get the object describing the content in the device that needs to be uploaded.
     *
     * @return The content description.
     */
    ReadableContent getReadableContent() {
        return this.readableContent;
    }

    /**
     * Get the constraints to be satisfied to execute the upload.
     *
     * @return The constraints.
     */
    Constraints getConstraints() {
        return this.constraints;
    }

    /**
     * Builder for {@link UploadRequest}.
     */
    public static final class Builder {
        private String storageClientId;
        private String containerName;
        private String blobName;
        private Boolean computeMd5;
        private ReadableContent readableContent;
        private Constraints constraints;

        /**
         * Creates a {@link Builder}.
         */
        public Builder() {
        }

        /**
         * Set the unique identifier of the blob storage client to be used for the upload.
         *
         * @param storageClientId The blob storage client ID.
         * @return Builder with the provided blob storage client ID set.
         */
        public Builder storageClientId(String storageClientId) {
            this.storageClientId = storageClientId;
            return this;
        }

        /**
         * Set the name of the container to upload the file to.
         *
         * @param containerName The container name.
         * @return Builder with the provided container name set.
         */
        public Builder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        /**
         * Set the name of the target blob holding the uploaded file.
         *
         * @param blobName The blob name.
         * @return Builder with the provided blob name set.
         */
        public Builder blobName(String blobName) {
            this.blobName = blobName;
            return this;
        }

        /**
         * Set whether or not the library should calculate the md5 and send it for the service to verify.
         *
         * @param computeMd5 Whether or not the library should calculate the md5 and send it for the service to verify.
         * @return Builder with the provided computeMd5 value set.
         */
        public Builder computeMd5(Boolean computeMd5) {
            this.computeMd5 = computeMd5;
            return this;
        }

        /**
         * Set the local file to upload.
         *
         * @param file The file.
         * @return Builder with the provided file set.
         */
        public Builder file(File file) {
            Objects.requireNonNull(file, "'file' cannot be null.");
            if (this.readableContent != null && this.readableContent.isUsingContentResolver()) {
                throw new IllegalArgumentException("Both the contentUri and file cannot be set for the same request.");
            }
            this.readableContent = new ReadableContent(null, Uri.fromFile(file), false);
            return this;
        }

        /**
         * Set the content in the device to upload from.
         *
         * @param context The application context.
         * @param uri     The URI to the local content to upload.
         * @return Builder with the provided content description set.
         */
        public Builder contentUri(Context context, Uri uri) {
            Objects.requireNonNull(context, "'context' cannot be null.");
            Objects.requireNonNull(uri, "'uri' cannot be null.");
            if (this.readableContent != null && !this.readableContent.isUsingContentResolver()) {
                throw new IllegalArgumentException("Both the contentUri and file cannot be set for the same request.");
            }
            this.readableContent = new ReadableContent(context, uri, true);
            return this;
        }

        /**
         * Set the constraints to be satisfied to execute the upload.
         *
         * @param constraints The constraints.
         * @return Builder with the provided constraints set.
         */
        public Builder constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        /**
         * Builds a {@link UploadRequest} based on this {@link Builder}'s configuration.
         *
         * @return An {@link UploadRequest}.
         */
        public UploadRequest build() {
            if (CoreUtil.isNullOrEmpty(this.storageClientId)) {
                throw new IllegalArgumentException("'storageClientId' is required and cannot be null or empty.");
            }
            if (CoreUtil.isNullOrEmpty(this.containerName)) {
                throw new IllegalArgumentException("'containerName' is required and cannot be null or empty.");
            }
            if (CoreUtil.isNullOrEmpty(this.blobName)) {
                throw new IllegalArgumentException("'blobName' is required and cannot be null or empty.");
            }
            Objects.requireNonNull(this.readableContent, "either 'file' or 'contentUri' must be set.");
            Objects.requireNonNull(this.constraints, "'constraints' cannot be null.");
            return new UploadRequest(this.storageClientId,
                this.containerName,
                this.blobName,
                this.computeMd5,
                this.readableContent,
                this.constraints);
        }
    }
}
