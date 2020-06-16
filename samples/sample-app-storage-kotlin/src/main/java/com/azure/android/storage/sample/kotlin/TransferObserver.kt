package com.azure.android.storage.sample.kotlin

import androidx.lifecycle.Observer
import com.azure.android.storage.blob.transfer.TransferInfo

/**
 * A convenient LiveData Observer for transfer state and progress changes.
 * The callback methods in this Observer will be invoked on the main thread.
 */
interface TransferObserver : Observer<TransferInfo?> {
    /**
     * Called when transfer is accepted by system.
     *
     * @param transferId the transfer id
     */
    fun onStart(transferId: Long)

    /**
     * Called when transfer made some progress by transferring by bytes.
     *
     * @param transferId the transfer id
     * @param totalBytes the total bytes to transfer
     * @param bytesTransferred the bytes transferred so far
     */
    fun onProgress(transferId: Long, totalBytes: Long, bytesTransferred: Long)

    /**
     * Called when the transfer is paused by the system.
     *
     * @param transferId the transfer id
     */
    fun onSystemPaused(transferId: Long)

    /**
     * Called when the paused transfer is resumed.
     *
     * @param transferId the transfer id
     */
    fun onResume(transferId: Long)

    /**
     * Called when the transfer is completed.
     *
     * @param transferId the transfer id
     */
    fun onComplete(transferId: Long)

    /**
     * Called when an error happens.
     *
     * @param transferId the transfer id
     * @param errorMessage the error message
     */
    fun onError(transferId: Long, errorMessage: String?)
    override fun onChanged(transferInfo: TransferInfo?) {
        when (transferInfo?.state) {
            TransferInfo.State.STARTED -> onStart(transferInfo.id)
            TransferInfo.State.RECEIVED_PROGRESS -> {
                val progress = transferInfo.progress
                onProgress(transferInfo.id, progress.totalBytes, progress.bytesTransferred)
            }
            TransferInfo.State.SYSTEM_PAUSED -> onSystemPaused(transferInfo.id)
            TransferInfo.State.RESUMED -> onResume(transferInfo.id)
            TransferInfo.State.COMPLETED -> onComplete(transferInfo.id)
            TransferInfo.State.FAILED -> onError(transferInfo.id, "Transfer failed.")
        }
    }
}
