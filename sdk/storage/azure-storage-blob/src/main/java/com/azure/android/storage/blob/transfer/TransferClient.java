// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.impl.WorkManagerImpl;

import com.azure.android.storage.blob.StorageBlobClient;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A type that exposes blob transfer APIs.
 */
public class TransferClient {
    private static final String TAG = TransferClient.class.getSimpleName();
    // the application context.
    private final Context context;
    // the default storage client for all transfers.
    private final StorageBlobClient blobClient;
    // the constraints to meet to run the transfers.
    // currently hold the network type required for transfers.
    private final Constraints constraints;
    // the executor for internal book keeping.
    private SerialExecutor serialTaskExecutor;
    // reference to the database holding transfer entities.
    private final TransferDatabase db;

    /**
     * Creates a {@link TransferClient} that uses provided {@link StorageBlobClient}
     * for transfers.
     *
     * @param context the context
     * @param blobClient the blob storage client
     * @param constraints the constraints to meet to run transfers
     * @param serialTaskExecutor the executor for all internal book keeping purposes
     */
    private TransferClient(Context context,
                           StorageBlobClient blobClient,
                           Constraints constraints,
                           SerialExecutor serialTaskExecutor) {
        this.context = context;
        this.blobClient = blobClient;
        this.constraints = constraints;
        this.serialTaskExecutor = serialTaskExecutor;
        this.db = TransferDatabase.get(context);
    }

    /**
     * Upload a file.
     *
     * @param containerName the container to upload the file to
     * @param blobName the name of the target blob holding uploaded file
     * @param file the local file to upload
     * @return LiveData that streams {@link TransferInfo} describing current state of the transfer
     */
    public LiveData<TransferInfo> upload(String containerName, String blobName, File file) {
        MutableLiveData<Long> transferIdLiveData = new MutableLiveData<>();
        this.serialTaskExecutor.execute(() -> {
            BlobUploadEntity blob = new BlobUploadEntity(containerName, blobName, file);
            List<BlockUploadEntity> blocks
                = BlockUploadEntity.createEntitiesForFile(file, Constants.DEFAULT_BLOCK_SIZE);
            long uploadId = db.uploadDao().createUploadRecord(blob, blocks);
            Log.v(TAG, "upload(): upload record created: " + uploadId);

            StorageBlobClientsMap.put(uploadId, blobClient);

            Data inputData = new Data.Builder()
                .putLong(UploadWorker.Constants.INPUT_BLOB_UPLOAD_ID_KEY, uploadId)
                .build();
            OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest
                .Builder(UploadWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();

            Log.v(TAG, "upload(): enqueuing UploadWorker: " + uploadId);
            WorkManager.getInstance(context)
                .beginUniqueWork(toTransferUniqueWorkName(uploadId),
                    ExistingWorkPolicy.KEEP,
                    uploadWorkRequest)
                .enqueue();
            transferIdLiveData.postValue(uploadId);
        });
        return new TransferIdMappedToTransferInfo()
            .getTransferInfoLiveData(context, transferIdLiveData);
    }

    /**
     * Download a blob.
     *
     * @param containerName The container to download the blob from.
     * @param blobName The name of the target blob to download.
     * @param file The local file to download to.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(String containerName, String blobName, File file) {
        MutableLiveData<Long> transferIdLiveData = new MutableLiveData<>();

        this.serialTaskExecutor.execute(() -> {
            BlobDownloadEntity blob = new BlobDownloadEntity(containerName, blobName, file);
            long downloadId = db.downloadDao().createDownloadRecord(blob);

            Log.v(TAG, "download(): download record created: " + downloadId);

            StorageBlobClientsMap.put(downloadId, blobClient);

            Data inputData = new Data.Builder()
                .putLong(DownloadWorker.Constants.INPUT_BLOB_DOWNLOAD_ID_KEY, downloadId)
                .build();
            OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest
                .Builder(DownloadWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();

            Log.v(TAG, "download(): enqueuing DownloadWorker: " + downloadId);

            WorkManager.getInstance(context)
                .beginUniqueWork(toTransferUniqueWorkName(downloadId),
                    ExistingWorkPolicy.KEEP,
                    downloadWorkRequest)
                .enqueue();
            transferIdLiveData.postValue(downloadId);
        });

        return new TransferIdMappedToTransferInfo().getTransferInfoLiveData(context, transferIdLiveData);
    }

    /**
     * Get the name for a unique transfer work.
     *
     * @param transferId the transfer id
     * @return name for the transfer work
     */
    static String toTransferUniqueWorkName(long transferId) {
        return "azure_transfer_" + transferId;
    }

    /**
     * A builder to configure and build a {@link TransferClient}.
     */
    public static final class Builder {
        // the application context.
        private Context context;
        // the default storage client for all transfers.
        private StorageBlobClient storageBlobClient;
        // indicate whether the device should be charging for running the transfers.
        private boolean requiresCharging = false;
        // indicate whether the device should be idle for running the transfers.
        private boolean requiresDeviceIdle = false;
        // indicate whether the device battery should be at an acceptable level for running the transfers
        private boolean requiresBatteryNotLow = false;
        // indicate whether the device's available storage should be at an acceptable level for running
        // the transfers
        private boolean requiresStorageNotLow = false;
        // the network type required for transfers.
        private NetworkType networkType = NetworkType.CONNECTED;
        // the executor for internal book keeping.
        private SerialExecutor serialTaskExecutor;

        /**
         * Create a new {@link TransferClient} builder.
         */
        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * Set the storage blob client to use for all transfers.
         *
         * @param storageBlobClient the storage blob client
         * @return Builder with provided Storage Blob Client set
         */
        public Builder setStorageClient(@NonNull StorageBlobClient storageBlobClient) {
            this.storageBlobClient = storageBlobClient;
            return this;
        }

        /**
         * Sets whether device should be charging for running the transfers.
         * The default value is {@code false}.
         *
         * @param requiresCharging {@code true} if device must be charging for the transfer to run
         * @return Builder with provided charging requirement set
         */
        public Builder setRequiresCharging(boolean requiresCharging) {
            this.requiresCharging = requiresCharging;
            return this;
        }

        /**
         * Sets whether device should be idle for running the transfers.
         * The default value is {@code false}.
         *
         * @param requiresDeviceIdle {@code true} if device must be idle for transfers to run
         * @return Builder with provided idle requirement set
         */
        @RequiresApi(23)
        public Builder setRequiresDeviceIdle(boolean requiresDeviceIdle) {
            this.requiresDeviceIdle = requiresDeviceIdle;
            return this;
        }

        /**
         * Sets the particular {@link NetworkType} the device should be in for running
         * the transfers.
         *
         * @param networkType The type of network required for transfers to run
         * @return Builder with provided network type set
         */
        public Builder setRequiredNetworkType(@NonNull NetworkType networkType) {
            this.networkType = networkType;
            return this;
        }

        /**
         * Sets whether device battery should be at an acceptable level for running the transfers.
         * The default value is {@code false}.
         *
         * @param requiresBatteryNotLow {@code true} if the battery should be at an acceptable level
         *                              for the transfers to run
         * @return Builder with provided battery requirement set
         */
        public Builder setRequiresBatteryNotLow(boolean requiresBatteryNotLow) {
            this.requiresBatteryNotLow = requiresBatteryNotLow;
            return this;
        }

        /**
         * Sets whether the device's available storage should be at an acceptable level for running
         * the transfers. The default value is {@code false}.
         *
         * @param requiresStorageNotLow {@code true} if the available storage should not be below a
         *                              a critical threshold for the transfer to run
         * @return Builder with provided storage requirement set
         */
        public Builder setRequiresStorageNotLow(boolean requiresStorageNotLow) {
            this.requiresStorageNotLow = requiresStorageNotLow;
            return this;
        }

        /**
         * Set the {@link Executor} used by {@link TransferClient} for all its internal
         * book keeping, which includes creating DB entries for transfer workers, querying DB
         * for status, submitting transfer request to {@link WorkManager}.
         *
         * TransferClient will enqueue a maximum of two commands to the taskExecutor at any time.
         *
         * @param executor the executor for internal book keeping
         * @return Builder with provided taskExecutor set
         */
        public Builder setTaskExecutor(@NonNull Executor executor) {
            this.serialTaskExecutor = new SerialExecutor(executor);
            return this;
        }

        /**
         * @return A {@link TransferClient} configured with settings applied through this builder
         */
        // Following annotation is added to suppress library restricted WorkManager
        // androidx.work.Configuration Object access
        @SuppressLint("RestrictedApi")
        public TransferClient build() {
            if (this.storageBlobClient == null) {
                throw new IllegalArgumentException("storageBlobClient must be set.");
            }
            final Constraints.Builder constraintsBuilder = new Constraints.Builder();
            constraintsBuilder.setRequiresCharging(this.requiresCharging);
            if (Build.VERSION.SDK_INT >= 23) {
                constraintsBuilder.setRequiresDeviceIdle(this.requiresDeviceIdle);
            }
            if (this.networkType == null) {
                throw new IllegalArgumentException("networkType must be set.");
            } else if (this.networkType == NetworkType.NOT_REQUIRED) {
                throw new IllegalArgumentException(
                    "The network type NOT_REQUIRED is not a valid transfer configuration.");
            }
            constraintsBuilder.setRequiredNetworkType(this.networkType);
            constraintsBuilder.setRequiresBatteryNotLow(this.requiresBatteryNotLow);
            constraintsBuilder.setRequiresStorageNotLow(this.requiresStorageNotLow);
            if (this.serialTaskExecutor == null) {
                try {
                    // Reference: https://github.com/Azure/azure-sdk-for-android/pull/203#discussion_r384854043
                    //
                    // Try to re-use the existing taskExecutor shared by WorkManager and Room.
                    WorkManagerImpl wmImpl = (WorkManagerImpl)WorkManager.getInstance(context);
                    this.serialTaskExecutor = new SerialExecutor(wmImpl.getConfiguration().getTaskExecutor());
                } catch (Exception ignored) {
                    // Create our own small ThreadPoolExecutor if we can't.
                    this.serialTaskExecutor = new SerialExecutor(Executors.newFixedThreadPool(2));
                }
            }
            return new TransferClient(this.context,
                this.storageBlobClient,
                constraintsBuilder.build(),
                this.serialTaskExecutor);
        }
    }

    private final class Constants {
        static final int KB = 1024;
        static final int MB = 1024 * KB;
        static final int DEFAULT_BLOCK_SIZE = 10 * Constants.MB;
    }
}
