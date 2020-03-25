// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 *
 * Contract to listen for various events from {@link UploadHandler} or {@link DownloadHandler} when handler upload a
 * single file.
 */
interface TransferHandlerListener {
    /**
     * Notify that a part of the transfer is successfully completed.
     *
     * @param totalBytes The total bytes to be transferred by the operation.
     * @param bytesTransferred The total bytes transferred so far.
     */
    void onTransferProgress(long totalBytes, long bytesTransferred);
    /**
     * Notify that user paused the transfer operation.
     *
     * This can be the last notification if the transfer operation is never resumed explicitly by the user.
     */
    void onUserPaused();
    /**
     * Notify that system paused the transfer operation.
     *
     * This can happen if the network criteria set for the transfer is no longer met.
     */
    void onSystemPaused();
    /**
     * Notify that the transfer operation is successfully completed. This is a terminal notification.
     */
    void onComplete();
    /**
     * Notify that transfer operation is failed. This is a terminal notification.
     *
     * @param t The reason for transfer failure.
     */
    void onError(Throwable t);
}
