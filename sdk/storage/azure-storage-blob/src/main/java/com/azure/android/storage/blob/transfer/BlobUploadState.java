// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 *
 * Describes blob upload state.
 */
enum BlobUploadState {
    /**
     * Blob is yet to be uploaded.
     */
    WAIT_TO_BEGIN,
    /**
     * Blob upload failed.
     */
    FAILED,
    /**
     * Blob upload completed.
     */
    COMPLETED
}
