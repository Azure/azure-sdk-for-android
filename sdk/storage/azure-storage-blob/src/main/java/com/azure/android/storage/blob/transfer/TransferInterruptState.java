// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Describes the reason for stopping (interrupting) the {@link UploadHandler}.
 */
enum TransferInterruptState {
    /**
     * When {@link UploadHandler} or {@link DownloadHandler} receives stop signal and sees that transfer interrupt state
     * is NONE then it indicates system requested the stop.
     */
    NONE,
    /**
     * When {@link UploadHandler} or {@link DownloadHandler} receives stop signal and sees that transfer interrupt state
     * is USER_CANCELLED then it indicates that user explicitly requested transfer cancellation.
     */
    USER_CANCELLED,
    /**
     * When {@link UploadHandler} or {@link DownloadHandler}receives stop signal and sees that transfer interrupt state
     * is USER_PAUSED then it indicates that user explicitly requested transfer pause.
     */
    USER_PAUSED,
    /**
     * Indicate that the blob and it's blocks transfer metadata is marked for purging.
     */
    PURGE,
}
