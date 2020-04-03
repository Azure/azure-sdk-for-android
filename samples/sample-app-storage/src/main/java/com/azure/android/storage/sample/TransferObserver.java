// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.s

package com.azure.android.storage.sample;

import androidx.lifecycle.Observer;

import com.azure.android.storage.blob.transfer.TransferInfo;

/**
 * A convenient LiveData Observer for transfer state and progress changes.
 * The callback methods in this Observer will be invoked on the main thread.
 */
public interface TransferObserver extends Observer<TransferInfo> {
    /**
     * Called when transfer is accepted by system.
     *
     * @param transferId the transfer id
     */
    void onStart(long transferId);

    /**
     * Called when transfer made some progress by transferring by bytes.
     *
     * @param transferId the transfer id
     * @param totalBytes the total bytes to transfer
     * @param bytesTransferred the bytes transferred so far
     */
    void onProgress(long transferId, long totalBytes, long bytesTransferred);

    /**
     * Called when the transfer is paused by the system.
     *
     * @param transferId the transfer id
     */
    void onSystemPaused(long transferId);

    /**
     * Called when the paused transfer is resumed.
     *
     * @param transferId the transfer id
     */
    void onResume(long transferId);

    /**
     * Called when the transfer is completed.
     *
     * @param transferId the transfer id
     */
    void onComplete(long transferId);

    /**
     * Called when an error happens.
     *
     * @param transferId the transfer id
     * @param errorMessage the error message
     */
    void onError(long transferId, String errorMessage);

    @Override
    default void onChanged(TransferInfo transferInfo) {
        @TransferInfo.State int state = transferInfo.getState();
        switch (state) {
            case TransferInfo.State.STARTED:
                this.onStart(transferInfo.getId());
                break;
            case TransferInfo.State.RECEIVED_PROGRESS:
                TransferInfo.Progress progress = transferInfo.getProgress();
                this.onProgress(transferInfo.getId(), progress.getTotalBytes(), progress.getBytesTransferred());
                break;
            case TransferInfo.State.SYSTEM_PAUSED:
                this.onSystemPaused(transferInfo.getId());
                break;
            case TransferInfo.State.RESUMED:
                this.onResume(transferInfo.getId());
                break;
            case TransferInfo.State.COMPLETED:
                this.onComplete(transferInfo.getId());
                break;
            case TransferInfo.State.FAILED:
                this.onError(transferInfo.getId(), "Transfer failed.");
                break;
        }
    }
}
