// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 *
 * Describes blob transfer state.
 */
enum BlobTransferState {
    /**
     * Blob is yet to be transferred.
     */
    WAIT_TO_BEGIN,
    /**
     * Blob transfer failed.
     */
    FAILED,
    /**
     * Blob transfer completed.
     */
    COMPLETED
}
