// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.os.Message;

/**
 * A notification to indicate that an executing {@link UploadHandler} should be stopped.
 *
 * Used by {@link UploadWorker} to send stop signal to {@link UploadHandler}.
 */
final class UploadStopToken {
    private volatile boolean isStopped;
    private Message stopMessage;

    /**
     * Creates UploadStopToken.
     *
     * @param stopMessage the stop message
     * @see UploadHandlerMessage#createStopMessage(UploadHandler)
     */
    UploadStopToken(Message stopMessage) {
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
