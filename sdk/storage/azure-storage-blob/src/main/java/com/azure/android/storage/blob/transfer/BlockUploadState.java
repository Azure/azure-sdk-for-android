// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 *
 * Describes block upload state.
 */
enum BlockUploadState {
    /**
     * Block is yet to be uploaded.
     */
    WAIT_TO_BEGIN,
    /**
     * Block upload is in progress.
     */
    IN_PROGRESS,
    /**
     * Block is uploaded.
     */
    COMPLETED,
    /**
     * Block upload is failed.
     */
    FAILED,
}
