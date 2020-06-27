package com.azure.android.storage.blob.transfer;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.Objects;

public class TransferTask {
    private final TransferClient transferClient;
    private final String transferId;
    private final LiveData<TransferInfo> liveData;
    private boolean isCancelled;

    public TransferTask(TransferClient transferClient,
                        String transferId,
                        LiveData<TransferInfo> liveData) {
        this.transferClient = Objects.requireNonNull(transferClient);
        this.transferId = transferId;
        this.liveData = Objects.requireNonNull(liveData);
    }

    public String getTransferId() {
        return this.transferId;
    }

    public LiveData<TransferInfo> getLiveData() {
        return this.liveData;
    }

    public void cancel(Context appContext) {
        this.isCancelled = true;
        this.transferClient.cancel(this.transferId);
    }

    public boolean isCanceled() {
        return this.isCancelled ;
    }

//    /**
//     * "FEATURE TO CONSIDER AFTER PREVIEW-1"
//     *
//     * Attach an observer for the transfer.
//     *
//     * <p>
//     * The Android LiveData from {@link this#getLiveData()} by design notify transfer
//     * statuses in the UI Thread. The addObserver enables observing in back-ground
//     * executors.
//     *
//     * @param observer the observer
//     */
//    public void addObserver(@NonNull TransferObserver observer) {
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
