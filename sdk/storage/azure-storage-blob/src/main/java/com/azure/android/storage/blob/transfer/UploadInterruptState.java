// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Describes the reason for stopping (interrupting) the {@link UploadHandler}.
 */
enum UploadInterruptState {
    /**
     * When {@link UploadHandler} receives stop signal and sees that upload interrupt state
     * is NONE then it indicates system requested the stop.
     */
    NONE,
    /**
     * When {@link UploadHandler} receives stop signal and sees that upload interrupt state
     * is USER_CANCELLED then it indicates that user explicitly requested upload cancellation.
     */
    USER_CANCELLED,
    /**
     * When {@link UploadHandler} receives stop signal and sees that upload interrupt state
     * is USER_PAUSED then it indicates that user explicitly requested upload pause.
     */
    USER_PAUSED,
    /**
     * Indicate that the blob and it's blocks upload metadata is marked for purging.
     */
    PURGE,
}
