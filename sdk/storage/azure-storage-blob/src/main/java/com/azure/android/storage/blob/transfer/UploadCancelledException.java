// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Exception indicating that user cancelled the file upload operation.
 */
public final class UploadCancelledException extends Exception {
    private final long uploadId;

    /**
     * Creates UploadCancelledException.
     *
     * @param uploadId the id of the cancelled upload
     */
    UploadCancelledException(long uploadId) {
        super();
        this.uploadId = uploadId;
    }

    /**
     * Get the id of the cancelled upload.
     *
     * @return the upload id
     */
    public long getUploadId() {
        return this.uploadId;
    }
}
