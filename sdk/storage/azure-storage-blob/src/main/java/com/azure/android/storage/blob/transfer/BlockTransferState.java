// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 *
 * Describes block transfer state.
 */
enum BlockTransferState {
    /**
     * Block is yet to be transferred.
     */
    WAIT_TO_BEGIN,
    /**
     * Block transfer is in progress.
     */
    IN_PROGRESS,
    /**
     * Block is transferred.
     */
    COMPLETED,
    /**
     * Block transfer is failed.
     */
    FAILED,
}
