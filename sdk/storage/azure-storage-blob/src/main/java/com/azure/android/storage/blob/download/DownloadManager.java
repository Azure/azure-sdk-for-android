package com.azure.android.storage.blob.download;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.azure.android.storage.blob.StorageBlobClient;

import java.io.File;
import java.io.IOException;

public class DownloadManager {
    private final static int MAX_RETRY = 5;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private BlobDownloader blobDownloader;

    public DownloadManager(StorageBlobClient storageBlobClient) {
        this.blobDownloader = new BlobDownloader(storageBlobClient, MAX_RETRY);
    }

    public File download(String containerName,
                         String blobName,
                         File file,
                         DownloadListener listener) {
        try {
            if (file == null) {
                file = blobDownloader.download(containerName, blobName);
            } else {
                file = blobDownloader.download(containerName, blobName, file);
            }
        } catch (IOException e) {
            Log.e("DownloadManager", "Download failed: ", e);
        }

        return file;
    }
}
