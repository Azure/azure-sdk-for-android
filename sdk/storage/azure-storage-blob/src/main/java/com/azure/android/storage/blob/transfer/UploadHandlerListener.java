// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Package private.
 *
 * Contract to listen for various events from {@link UploadHandler} when handler
 * upload a single file.
 */
interface UploadHandlerListener {
    /**
     * Notify that a part of the file is successfully uploaded.
     *
     * @param totalBytes the total bytes to be uploaded by the upload operation
     * @param bytesUploaded the total bytes uploaded so far
     */
    void onUploadProgress(long totalBytes, long bytesUploaded);
    /**
     * Notify that user paused the upload operation.
     *
     * This can be the last notification if the upload operation is never
     * resumed explicitly by the user.
     */
    void onUserPaused();
    /**
     * Notify that system paused the upload operation.
     *
     * This can happen if the network criteria set for the upload is
     * no longer met.
     */
    void onSystemPaused();
    /**
     * Notify that the upload operation is successfully completed.
     * This is a terminal notification.
     */
    void onComplete();
    /**
     * Notify that upload operation is failed.
     * This is a terminal notification.
     *
     * @param t the reason for upload failure
     */
    void onError(Throwable t);
}
