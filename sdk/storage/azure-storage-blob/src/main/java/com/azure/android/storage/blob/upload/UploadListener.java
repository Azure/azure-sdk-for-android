package com.azure.android.storage.blob.upload;

public interface UploadListener {
    void onUploadProgress(int totalBytes, int bytesUploaded);
    void onError(Throwable t);
    void onCompleted();
}
