package com.azure.android.storage.blob.transfer;

import androidx.lifecycle.LiveData;

import java.util.UUID;

public class TransferTask {
    private final String transferId;
    private final TransferClient transferClient;
    private final UploadRequest uploadRequest;
    private final DownloadRequest downloadRequest;
    private final ResumeRequest resumeRequest;
    private boolean isCancelled;

    // ctr will be package internal later
    public TransferTask(TransferClient transferClient, UploadRequest uploadRequest) {
        this.transferId = UUID.randomUUID().toString();
        this.transferClient = transferClient;
        this.uploadRequest = uploadRequest;
        this.downloadRequest = null;
        this.resumeRequest = null;
    }

    // ctr will be package internal later
    public TransferTask(TransferClient transferClient, DownloadRequest downloadRequest) {
        this.transferId = UUID.randomUUID().toString();
        this.transferClient = transferClient;
        this.downloadRequest = downloadRequest;
        this.uploadRequest = null;
        this.resumeRequest = null;
    }

    // ctr will be package internal later
    public TransferTask(TransferClient transferClient, ResumeRequest resumeRequest) {
        this.transferId = resumeRequest.getTransferId();
        this.transferClient = transferClient;
        this.resumeRequest = resumeRequest;
        this.downloadRequest = null;
        this.uploadRequest = null;
    }

    public String getTransferId() {
        return this.transferId;
    }

    public LiveData<TransferInfo> enqueue() {
        if (this.uploadRequest != null) {
            return this.transferClient.upload(this.transferId, this.uploadRequest);
        } else if (downloadRequest != null) {
            return this.transferClient.download(this.transferId, this.downloadRequest);
        } else if (this.resumeRequest != null) {
            return this.transferClient.resume(this.transferId);
        }
        throw new RuntimeException("unknown transfer request type.");
    }

    public void cancel() {
        this.isCancelled = true;
        this.transferClient.cancel(this.transferId);
    }

    public boolean isCanceled() {
        return this.isCancelled;
    }

//    /**
//     * "FEATURE TO CONSIDER AFTER PREVIEW-1"
//     *
//     * Attach an observer for the transfer.
//     *
//     * <p>
//     * The Android LiveData from {@link this#getLiveData()} by design notify transfer
//     * statuses in the UI Thread. The enqueue(observer) enables observing in back-ground
//     * executors.
//     *
//     * @param observer the observer
//     */
//    public void enqueue(@NonNull TransferObserver observer) {
//        throw new RuntimeException("NotImplemented");
//    }
//
//    interface TransferObserver {
//        void onTransferProgress(long totalBytes, long bytesTransferred);
//        void onUserPaused();
//        void onSystemPaused();
//        void onComplete();
//        void onError(Throwable t);
//    }
}
