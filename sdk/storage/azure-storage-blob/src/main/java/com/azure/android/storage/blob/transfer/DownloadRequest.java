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
 * A type specifying parameters for a download transfer that should be enqueued in {@link TransferClient}.
 */
public final class DownloadRequest {
    private final String storageClientId;
    private final String containerName;
    private final String blobName;
    private final WritableContent writableContent;
    private final Constraints constraints;

    /**
     * Create DownloadRequest.
     *
     * @param storageClientId Identifies the {@link com.azure.android.storage.blob.StorageBlobAsyncClient} for the download.
     * @param containerName   The name of the container holding the blob to download.
     * @param blobName        The name of the blob to download.
     * @param writableContent The object describing the content in the device to store the downloaded blob.
     * @param constraints     The constraints to be satisfied to execute the download.
     */
    private DownloadRequest(String storageClientId,
                            String containerName,
                            String blobName,
                            WritableContent writableContent,
                            Constraints constraints) {
        this.storageClientId = storageClientId;
        this.containerName = containerName;
        this.blobName = blobName;
        this.writableContent = writableContent;
        this.constraints = constraints;
    }

    /**
     * Get the unique identifier of the blob storage client to be used for the download.
     *
     * @return The unique identifier of the {@link com.azure.android.storage.blob.StorageBlobAsyncClient}.
     */
    String getStorageClientId() {
        return this.storageClientId;
    }

    /**
     * Get the name of the container holding the blob to download.
     *
     * @return The container name.
     */
    String getContainerName() {
        return this.containerName;
    }

    /**
     * Get the name of the blob to download.
     *
     * @return The blob name.
     */
    String getBlobName() {
        return this.blobName;
    }

    /**
     * Get the object describing the content in the device to store the downloaded blob.
     *
     * @return The content description.
     */
    WritableContent getWritableContent() {
        return this.writableContent;
    }

    /**
     * Get the constraints to be satisfied to execute the download.
     *
     * @return The constraints.
     */
    Constraints getConstraints() {
        return this.constraints;
    }

    /**
     * Builder for {@link DownloadRequest}.
     */
    public static final class Builder {
        private String storageClientId;
        private String containerName;
        private String blobName;
        private WritableContent writableContent;
        private Constraints constraints;

        /**
         * Creates a {@link Builder}.
         */
        public Builder() {
        }

        /**
         * Set the unique identifier of the blob storage client to be used for the download.
         *
         * @param storageClientId The blob storage client ID.
         * @return Builder with provided blob storage client ID set.
         */
        public Builder storageClientId(String storageClientId) {
            this.storageClientId = storageClientId;
            return this;
        }

        /**
         * Set the name of the container holding the blob to download.
         *
         * @param containerName The container name.
         * @return Builder with the provided container name set.
         */
        public Builder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        /**
         * Set the name of the blob to download.
         *
         * @param blobName The blob name.
         * @return Builder with the provided blob name set.
         */
        public Builder blobName(String blobName) {
            this.blobName = blobName;
            return this;
        }

        /**
         * Set the local file to download to.
         *
         * @param file The file.
         * @return Builder with the provided file set.
         */
        public Builder file(File file) {
            Objects.requireNonNull(file, "'file' cannot be null.");
            if (this.writableContent != null && this.writableContent.isUsingContentResolver()) {
                throw new IllegalArgumentException("Both the contentUri and file cannot be set for the same request.");
            }
            this.writableContent = new WritableContent(null, Uri.fromFile(file), false);
            return this;
        }

        /**
         * Set the content in the device where the downloaded blob will be stored.
         *
         * @param context The application context.
         * @param uri     The URI to the local content where the downloaded blob will be stored.
         * @return Builder with the provided content description set.
         */
        public Builder contentUri(Context context, Uri uri) {
            Objects.requireNonNull(context, "'context' cannot be null.");
            Objects.requireNonNull(uri, "'uri' cannot be null.");
            if (this.writableContent != null && !this.writableContent.isUsingContentResolver()) {
                throw new IllegalArgumentException("Both the contentUri and file cannot be set for the same request.");
            }
            this.writableContent = new WritableContent(context, uri, true);
            return this;
        }

        /**
         * Set the constraints to be satisfied to execute the download.
         *
         * @param constraints The constraints.
         * @return Builder with the provided constraints set.
         */
        public Builder constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        /**
         * Builds a {@link DownloadRequest} based on this {@link Builder}'s configuration.
         *
         * @return A {@link DownloadRequest}.
         */
        public DownloadRequest build() {
            if (CoreUtil.isNullOrEmpty(this.storageClientId)) {
                throw new IllegalArgumentException("'storageClientId' is required and cannot be null or empty.");
            }
            if (CoreUtil.isNullOrEmpty(this.containerName)) {
                throw new IllegalArgumentException("'containerName' is required and cannot be null or empty.");
            }
            if (CoreUtil.isNullOrEmpty(this.blobName)) {
                throw new IllegalArgumentException("'blobName' is required and cannot be null or empty.");
            }
            Objects.requireNonNull(this.writableContent, "either 'file' or 'contentUri' must be set.");
            Objects.requireNonNull(this.constraints, "'constraints' cannot be null.");
            return new DownloadRequest(this.storageClientId,
                this.containerName,
                this.blobName,
                this.writableContent,
                this.constraints);
        }
    }
}
