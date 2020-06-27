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
}
