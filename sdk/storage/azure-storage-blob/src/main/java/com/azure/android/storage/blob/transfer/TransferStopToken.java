// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.os.Message;

/**
 * A notification to indicate that an executing {@link UploadHandler} or {@link DownloadHandler} should be stopped.
 *
 * Used by {@link UploadWorker} and {@link DownloadWorker} to send a stop signal to an {@link UploadHandler} or
 * {@link DownloadHandler}.
 */
final class TransferStopToken {
    private volatile boolean isStopped;
    private Message stopMessage;

    /**
     * Creates TransferStopToken.
     *
     * @param stopMessage the stop message
     * @see UploadHandlerMessage#createStopMessage(UploadHandler)
     * @see DownloadHandlerMessage#createStopMessage(DownloadHandler)
     */
    TransferStopToken(Message stopMessage) {
        this.stopMessage = stopMessage;
    }

    /**
     * Gets whether stop has been requested for this token.
     *
     * @return true if stop is requested, false otherwise
     */
    boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Communicates a request for stop.
     */
    void stop() {
        if (!this.isStopped) {
            this.isStopped = true;
            this.stopMessage.sendToTarget();
        }
    }
}
