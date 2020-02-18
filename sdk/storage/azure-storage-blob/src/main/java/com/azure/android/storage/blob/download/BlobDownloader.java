package com.azure.android.storage.blob.download;

import android.os.Environment;

import androidx.annotation.NonNull;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobDownloadAsyncResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlobDownloader {
    private final StorageBlobClient storageBlobClient;
    private final int downloadMaxRetry;

    public BlobDownloader(StorageBlobClient storageBlobClient, int downloadMaxRetry) {
        this.storageBlobClient = storageBlobClient;
        this.downloadMaxRetry = downloadMaxRetry;
    }

    public File download(@NonNull String containerName, @NonNull String blobName) throws IOException {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, blobName);

        download(containerName, blobName, file);

        return file;
    }

    public File download(@NonNull String containerName, @NonNull String blobName, @NonNull File file) throws IOException {
        BlobDownloadAsyncResponse response = storageBlobClient.downloadWithRestResponse(containerName, blobName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(response.getValue().bytes());
        }

        return file;
    }
}
