// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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
    private final Constraints constraints;
    // the executor for internal book keeping.
    private SerialExecutor serialTaskExecutor;
    // reference to the database holding transfer entities.
    private final TransferDatabase db;
    // track the active (not collected by GC) Transfers.
    private final static TransferIdInfoLiveDataCache TRANSFER_ID_INFO_CACHE = new TransferIdInfoLiveDataCache();

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
        // UI_Thread
        final MutableLiveData<TransferOperationResult> transferOpResultLiveData = new MutableLiveData<>();
        this.serialTaskExecutor.execute(() -> {
            // BG_Thread
            try {
                BlobUploadEntity blob = new BlobUploadEntity(containerName, blobName, file);
                List<BlockUploadEntity> blocks
                    = BlockUploadEntity.createEntitiesForFile(file, Constants.DEFAULT_BLOCK_SIZE);
                long transferId = db.uploadDao().createUploadRecord(blob, blocks);
                Log.v(TAG, "upload(): upload record created: " + transferId);

                StorageBlobClientsMap.put(transferId, blobClient);

                Data inputData = new Data.Builder()
                    .putLong(UploadWorker.Constants.INPUT_BLOB_UPLOAD_ID_KEY, transferId)
                    .build();
                OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest
                    .Builder(UploadWorker.class)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build();

                Log.v(TAG, "upload(): enqueuing UploadWorker: " + transferId);
                WorkManager.getInstance(context)
                    .beginUniqueWork(toTransferUniqueWorkName(transferId),
                        ExistingWorkPolicy.KEEP,
                        uploadWorkRequest)
                    .enqueue();
                transferOpResultLiveData
                    .postValue(TransferOperationResult.id(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, transferId));
            } catch (Exception e) {
                transferOpResultLiveData
                    .postValue(TransferOperationResult.error(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, e));
            }
        });
        // UI_Thread
        return toCachedTransferInfoLiveData(transferOpResultLiveData, false);
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
            long blobSize = blobClient.getBlobProperties(containerName, blobName).getContentLength();
            List<BlockDownloadEntity> blocks
                = BlockDownloadEntity.createEntitiesForBlob(file, blobSize, Constants.DEFAULT_BLOCK_SIZE);
            long downloadId = db.downloadDao().createDownloadRecord(blob, blocks);

            db.downloadDao().updateBlobSize(downloadId, blobSize);

            Log.v(TAG, "download(): Download record created: " + downloadId);

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
     * Pause a transfer identified by the given transfer id. The pause operation
     * is a best-effort, and a transfer that is already executing may continue to
     * transfer.
     *
     * Upon successful scheduling of the pause, any observer observing on
     * {@link LiveData<TransferInfo>} for this transfer receives a {@link TransferInfo}
     * event with state {@link TransferInfo.State#USER_PAUSED}.
     *
     * @param transferId the transfer id identifies the transfer to pause.
     */
    // P2: Currently no return value, evaluate any possible return value later.
    public void pause(long transferId) {
        // UI_Thread
        final TransferIdInfoLiveData.TransferFlags transferFlags = TRANSFER_ID_INFO_CACHE.getTransferFlags(transferId);
        this.serialTaskExecutor.execute(() -> {
            // BG_Thread
            try {
                final PauseCheck pauseCheck = checkPauseable(transferId);
                if (pauseCheck.canPause) {
                    if (pauseCheck.isUpload) {
                        db.uploadDao().updateUploadInterruptState(transferId, UploadInterruptState.USER_PAUSED);
                    } else {
                        throw new RuntimeException("Download::pause() NotImplemented");
                    }
                    if (transferFlags != null) {
                        transferFlags.setUserPaused();
                    }
                    WorkManager
                        .getInstance(context)
                        .cancelUniqueWork(toTransferUniqueWorkName(transferId));
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to schedule pause for the transfer:" + transferId, e);
            }
        });
    }

    /**
     * Resume a paused transfer.
     *
     * @param transferId the transfer id identifies the transfer to resume.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the transfer
     */
    public LiveData<TransferInfo> resume(long transferId) {
        // UI_Thread
        final MutableLiveData<TransferOperationResult> transferOpResultLiveData = new MutableLiveData<>();
        this.serialTaskExecutor.execute(() -> {
            // BG_Thread
            try {
                final ResumeCheck resumeCheck = checkResumeable(transferId, transferOpResultLiveData);
                if (resumeCheck.canResume) {
                    StorageBlobClientsMap.put(transferId, blobClient);
                    final OneTimeWorkRequest workRequest;
                    if (resumeCheck.isUpload) {
                        Data inputData = new Data.Builder()
                            .putLong(UploadWorker.Constants.INPUT_BLOB_UPLOAD_ID_KEY, transferId)
                            .build();
                        workRequest = new OneTimeWorkRequest
                            .Builder(UploadWorker.class)
                            .setConstraints(constraints)
                            .setInputData(inputData)
                            .build();
                        Log.v(TAG, "Upload::resume() Enqueuing UploadWorker: " + transferId);
                    } else {
                        throw new RuntimeException("Download::resume() NotImplemented");
                    }
                    // resume() will resubmit the work to WorkManager with the policy as KEEP.
                    // With this policy, if the work is already running, then this resume() call is NO-OP,
                    // we return the LiveData to the caller that streams the TransferInfo events of
                    // the already running work.
                    WorkManager.getInstance(context)
                        .beginUniqueWork(toTransferUniqueWorkName(transferId),
                            ExistingWorkPolicy.KEEP,
                            workRequest)
                        .enqueue();
                    transferOpResultLiveData
                        .postValue(TransferOperationResult.id(TransferOperationResult.Operation.RESUME, transferId));
                }
            } catch (Exception e) {
                transferOpResultLiveData
                    .postValue(TransferOperationResult.error(TransferOperationResult.Operation.RESUME, e));
            }
        });
        // UI_Thread
        return toCachedTransferInfoLiveData(transferOpResultLiveData, true);
    }

    /**
     * Cancel a transfer.
     *
     * @param transferId Unique identifier of the operation to cancel.
     */
    public LiveData<Boolean> cancel(long transferId) {
        MutableLiveData<Boolean> cancelLiveData = new MutableLiveData<>();

        this.serialTaskExecutor.execute(() -> {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(toTransferUniqueWorkName(transferId));

                BlobDownloadEntity blob = db.downloadDao().getDownloadRecord(transferId).blob;

                db.downloadDao().updateDownloadInterruptState(blob.key, TransferInterruptState.USER_CANCELLED);

                cancelLiveData.postValue(true);
            } catch (Exception e) {
                Log.w(TAG, "Could not cancel transfer with ID: " + transferId, e);

                cancelLiveData.postValue(false);
            }
        });

        return cancelLiveData;
    }

    /**
     * Get unique name for a transfer work.
     *
     * @param transferId the transfer id
     * @return name for the transfer work
     */
    static String toTransferUniqueWorkName(long transferId) {
        return "azure_transfer_" + transferId;
    }

    /**
     * Subscribe to a TransferOperationResult LiveData and transform that to TransferInfo LiveData.
     *
     * This method caches or uses cached {@link LiveData<TransferInfo>} to stream TransferInfo.
     * If provided TransferOperationResult LiveData emits an error, then cache won't be used.
     *
     * @param transferOpResultLiveData the LiveData to channel transfer operation initiation result
     * @param isResume true if the transfer id emitted by the transferOpResultLiveData LiveData
     *   identifies a transfer to be resumed, false for a new upload or download transfer.
     * @return the TransferInfo LiveData
     */
    @MainThread
    private LiveData<TransferInfo> toCachedTransferInfoLiveData(LiveData<TransferOperationResult> transferOpResultLiveData,
                                                                boolean isResume) {
        // UI_Thread
        return Transformations.switchMap(transferOpResultLiveData, transferOpResult -> {
            if (transferOpResult.isError()) {
                final TransferIdInfoLiveData.Result result = TransferIdInfoLiveData.create(context);
                final TransferIdInfoLiveData.LiveDataPair pair = result.getLiveDataPair();
                pair.getTransferOpResultLiveData().setValue(transferOpResult);
                return pair.getTransferInfoLiveData();
            } else {
                if (isResume) {
                    // If the application process already has a cached LiveData pair for the same transfer,
                    // then use it, otherwise create, cache, and use.
                    final TransferIdInfoLiveData.LiveDataPair pair
                        = TRANSFER_ID_INFO_CACHE.getOrCreate(transferOpResult.getId(), context);
                    pair.getTransferOpResultLiveData().setValue(transferOpResult);
                    return pair.getTransferInfoLiveData();
                } else {
                    // For a new upload or download transfer, create a transfer LiveData pair
                    // (TransferOperationResult, TransferInfo) cache entry and use them.
                    // Any future resume operation on the same transfer will use this pair
                    // as long as:
                    //     1. both upload or download transfer, and the corresponding resume happens
                    //        in the same application process
                    //     2. and the cache entry is not GC-ed.
                    final TransferIdInfoLiveData.LiveDataPair pair
                        = TRANSFER_ID_INFO_CACHE.create(transferOpResult.getId(), context);
                    pair.getTransferOpResultLiveData().setValue(transferOpResult);
                    return pair.getTransferInfoLiveData();
                }
            }
        });
    }

    /**
     * Do pre-validations to see a transfer can be resumed.
     *
     * @param transferId identifies the transfer to check for resume eligibility
     * @param transferOpResultLiveData the LiveData to post the error if the transfer cannot be resumed
     * @return result of check
     */
    private ResumeCheck checkResumeable(long transferId,
                                        MutableLiveData<TransferOperationResult> transferOpResultLiveData) {
        // Check for Upload Record
        BlobUploadEntity uploadBlob = db.uploadDao().getBlob(transferId);
        if (uploadBlob != null) {
            if (uploadBlob.state == BlobUploadState.FAILED) {
                transferOpResultLiveData.postValue(TransferOperationResult.alreadyInFailedStateError(transferId));
                return new ResumeCheck(false, true);
            } else if (uploadBlob.state == BlobUploadState.COMPLETED) {
                transferOpResultLiveData.postValue(TransferOperationResult.alreadyInCompletedStateError(transferId));
                return new ResumeCheck(false, true);
            }
            return new ResumeCheck(true, true);
        }
        // TODO: Check for Download Record

        // No upload or download transfer found.
        transferOpResultLiveData.postValue(TransferOperationResult.notFoundError(transferId));
        return new ResumeCheck(false, false);
    }

    /** Result of {@link this#checkResumeable(long, MutableLiveData)}} **/
    private static final class ResumeCheck {
        // flag indicating whether transfer is resume-able or not.
        final boolean canResume;
        // if resume-able then this flag indicates the transfer type (upload|download)
        final boolean isUpload;

        ResumeCheck(boolean canResume, boolean isUpload) {
            this.canResume = canResume;
            this.isUpload = isUpload;
        }
    }

    /**
     * Do pre-validations to see a transfer can be paused.
     *
     * @param transferId identifies the transfer to check for pause eligibility
     * @return result of check
     */
    private PauseCheck checkPauseable(long transferId) {
        // Check for Upload Record
        BlobUploadEntity blob = db.uploadDao().getBlob(transferId);
        if (blob != null) {
            if (blob.state == BlobUploadState.FAILED) {
                return new PauseCheck(false, true);
            } else if (blob.state == BlobUploadState.COMPLETED) {
                return new PauseCheck(false, true);
            }
            return new PauseCheck(true, true);
        }
        // TODO: Check for Download Record

        // No upload or download transfer found.
        return new PauseCheck(false, false);
    }

    /** Result of {@link this#checkPauseable(long)}} **/
    private static final class PauseCheck {
        // flag indicating whether transfer is pause-able or not.
        private final boolean canPause;
        // if pause-able then this flag indicates the transfer type (upload|download)
        final boolean isUpload;

        private PauseCheck(boolean canPause, boolean isUpload) {
            this.canPause = canPause;
            this.isUpload = isUpload;
        }
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
         * The default network type that {@link TransferClient} uses is {@link NetworkType#CONNECTED}.
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

    private static final class Constants {
        static final int KB = 1024;
        static final int MB = 1024 * KB;
        static final int DEFAULT_BLOCK_SIZE = 10 * Constants.MB;
    }
}
