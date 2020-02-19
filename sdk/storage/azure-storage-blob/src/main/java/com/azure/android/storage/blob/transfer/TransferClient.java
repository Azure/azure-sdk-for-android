// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.credentials.SasTokenCredential;
import com.azure.android.storage.blob.interceptor.SasTokenCredentialInterceptor;

import java.io.File;
import java.util.List;

/**
 * A type that exposes APIs for blob transfer.
 */
public class TransferClient {
    private static final String TAG = TransferClient.class.getSimpleName();
    private final Context context;
    private final TransferDatabase db;
    private final StorageBlobClient blobClient;

    /**
     * Creates a {@link TransferClient} that uses provided {@link StorageBlobClient}
     * for transfers.
     *
     * @param context the context
     * @param blobClient the blob storage client
     */
    public TransferClient(Context context, StorageBlobClient blobClient) {
        this.context = context;
        this.db = TransferDatabase.get(context);
        this.blobClient = blobClient;
    }

    /**
     * Creates a {@link TransferClient} that uses provided blob storage endpoint and SAS token.
     *
     * @param context the context
     * @param storageUrl the blob storage url
     * @param sasToken the SAS token
     */
    public TransferClient(Context context, String storageUrl, String sasToken) {
        this(context,
            new StorageBlobClient.Builder()
                .setBlobServiceUrl(storageUrl)
                .setCredentialInterceptor(new SasTokenCredentialInterceptor(new SasTokenCredential(sasToken)))
                .build());
    }

    /**
     * Upload a file.
     *
     * @param containerName the container to upload the file to
     * @param blobName the name of the target blob holding uploaded file
     * @param file the local file to upload
     * @return the upload id
     */
    public long upload(String containerName, String blobName, File file) {
        BlobUploadEntity blob = new BlobUploadEntity(containerName, blobName, file);
        List<BlockUploadEntity> blocks = BlockUploadEntity.createEntitiesForFile(file, Constants.DEFAULT_BLOCK_SIZE);
        long uploadId = this.db.uploadDao().createUploadRecord(blob, blocks);
        Log.v(TAG, "upload(): upload record created: " + uploadId);

        StorageBlobClientsMap.put(uploadId, this.blobClient);
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

       Data inputData = new Data.Builder()
            .putLong(UploadWorker.Constants.INPUT_BLOB_UPLOAD_ID_KEY, uploadId)
            .build();

        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest
            .Builder(UploadWorker.class)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build();
        Log.v(TAG, "upload(): enqueuing UploadWorker: " + uploadId);
        WorkManager.getInstance(this.context)
            .beginUniqueWork("file_upload_" + uploadId, ExistingWorkPolicy.KEEP, uploadWorkRequest)
            .enqueue();
        return uploadId;
    }

    private final class Constants {
        static final int KB = 1024;
        static final int MB = 1024 * KB;
        static final int DEFAULT_BLOCK_SIZE = 10 * Constants.MB;
    }
}
