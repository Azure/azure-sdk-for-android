// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 * <p>
 * Describes the blob download state.
 */
enum BlobDownloadState {
    /**
     * Blob is yet to be downloaded.
     */
    WAIT_TO_BEGIN,
    /**
     * Blob download is in progress.
     */
    IN_PROGRESS,
    /**
     * Blob download failed.
     */
    FAILED,
    /**
     * Blob download completed.
     */
    COMPLETED
}
