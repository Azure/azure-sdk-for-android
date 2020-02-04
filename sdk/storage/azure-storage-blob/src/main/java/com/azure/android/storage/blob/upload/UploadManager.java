package com.azure.android.storage.blob.upload;

import android.os.Handler;
import android.os.Looper;

import com.azure.android.storage.blob.StorageBlobClient;

import java.io.File;

public class UploadManager {
    private final static int MAX_RETRY = 5;
    private BlobUploader blobUploader;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UploadManager(StorageBlobClient blobClient) {
        this.blobUploader = new BlobUploader(blobClient, MAX_RETRY);
    }

    public void upload(String containerName, String blobName, File file, UploadListener listener) {
        BlobUploadRecord blobUploadRecord = BlobUploadRecord.create(containerName, blobName, file);
        this.blobUploader.upload(blobUploadRecord, new BlobUploader.Listener() {
            @Override
            public void onUploadProgress(int totalBytes, int bytesUploaded) {
                mainHandler.post(() -> listener.onUploadProgress(totalBytes, bytesUploaded));
            }

            @Override
            public void onError(Throwable t) {
                mainHandler.post(() -> listener.onError(t));
            }

            @Override
            public void onCompleted() {
                mainHandler.post(() -> listener.onCompleted());
            }
        });
    }
}
