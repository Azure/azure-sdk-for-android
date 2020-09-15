// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.impl.WorkManagerImpl;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;

import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.Response;

/**
 * A type that exposes blob transfer APIs.
 */
public final class TransferClient {
    private static final String TAG = TransferClient.class.getSimpleName();
    // the executor for internal book keeping.
    private SerialExecutor serialTaskExecutor;
    // reference to the database holding transfer entities.
    private final TransferDatabase db;
    // reference to the androidx work manager.
    private final WorkManager workManager;
    // track the active (not collected by GC) Transfers.
    private final TransferIdInfoLiveDataCache transferIdInfoCache = new TransferIdInfoLiveDataCache();
    // The singleton TransferClient.
    private static TransferClient INSTANCE = null;
    // An object to synchronize the creation of the singleton TransferClient.
    private static final Object INIT_LOCK = new Object();
    // the static shared map of StorageBlobClient instances for transfers, with package
    // scoped access. StorageBlobClient instances are added from TransferClient.Builder
    // and used by Upload|Download handlers.
    static final StorageBlobClientMap STORAGE_BLOB_CLIENTS;

    static {
        STORAGE_BLOB_CLIENTS = StorageBlobClientMap.getInstance();
    }

    /**
     * Retrieves the singleton instance of {@link TransferClient}.
     *
     * @param context A {@link Context} for on-demand initialization.
     * @return The singleton instance of {@link TransferClient}.
     * @throws IllegalStateException If underlying Database or {@link WorkManager} is not initialized properly.
     */
    public static @NonNull TransferClient getInstance(@NonNull Context context) throws IllegalStateException {
        synchronized (INIT_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new TransferClient(context.getApplicationContext());
            }
            return INSTANCE;
        }
    }

    /**
     * Create an instance of {@link TransferClient}.
     *
     * @param applicationContext The application {@link Context} for on-demand initialization.
     */
    @SuppressLint("RestrictedApi")
    private TransferClient(Context applicationContext) {
        this.db = TransferDatabase.getInstance(applicationContext);
        this.workManager = WorkManager.getInstance(applicationContext);
        try {
            // Reference: https://github.com/Azure/azure-sdk-for-android/pull/203#discussion_r384854043
            //
            // Try to re-use the existing taskExecutor shared by WorkManager.
            WorkManagerImpl wmImpl = (WorkManagerImpl)this.workManager;
            this.serialTaskExecutor = new SerialExecutor(wmImpl.getConfiguration().getTaskExecutor());
        } catch (Exception ignored) {
            // Create our own small ThreadPoolExecutor if we can't.
            this.serialTaskExecutor = new SerialExecutor(Executors.newFixedThreadPool(2));
        }
    }

    /**
     * Upload the content described by the given {@link ReadableContent}.
     *
     * @param uploadRequest Describes the upload request.
     * @return LiveData that streams {@link TransferInfo} describing current state of the transfer
     */
    public LiveData<TransferInfo> upload(UploadRequest uploadRequest) {
        final ReadableContent readableContent = uploadRequest.getReadableContent();
        final MutableLiveData<TransferOperationResult> transferOpResultLiveData = new MutableLiveData<>();
        try {
            // Take permission immediately in the UI_Thread (granting may require UI interaction).
            readableContent.takePersistableReadPermission();
        } catch (Throwable e) {
            transferOpResultLiveData
                .postValue(TransferOperationResult.error(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, e));
            return toCachedTransferInfoLiveData(transferOpResultLiveData, false);
        }
        this.serialTaskExecutor.execute(() -> {
            try {
                if (!TransferClient.STORAGE_BLOB_CLIENTS.contains(uploadRequest.getStorageClientId())) {
                    transferOpResultLiveData.postValue(TransferOperationResult
                        .unresolvedStorageClientIdError(TransferOperationResult.Operation.UPLOAD_DOWNLOAD,
                            uploadRequest.getStorageClientId()));
                    return;
                }
                BlobUploadEntity blob = new BlobUploadEntity(uploadRequest.getStorageClientId(),
                    uploadRequest.getContainerName(),
                    uploadRequest.getBlobName(),
                    readableContent,
                    uploadRequest.getConstraints());
                List<BlockUploadEntity> blocks
                    = BlockUploadEntity.createBlockEntities(readableContent.getLength(), Constants.DEFAULT_BLOCK_SIZE);
                long transferId = db.uploadDao().createUploadRecord(blob, blocks);
                Log.v(TAG, "upload(): upload record created: " + transferId);

                Data inputData = new Data.Builder()
                    .putLong(UploadWorker.Constants.INPUT_BLOB_UPLOAD_ID_KEY, transferId)
                    .build();
                OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest
                    .Builder(UploadWorker.class)
                    .setConstraints(uploadRequest.getConstraints())
                    .setInputData(inputData)
                    .build();

                Log.v(TAG, "upload(): enqueuing UploadWorker: " + transferId);
                workManager
                    .beginUniqueWork(toTransferUniqueWorkName(transferId),
                        ExistingWorkPolicy.KEEP,
                        uploadWorkRequest)
                    .enqueue();
                transferOpResultLiveData
                    .postValue(TransferOperationResult.id(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, transferId));
            } catch (Throwable e) {
                transferOpResultLiveData
                    .postValue(TransferOperationResult.error(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, e));
            }
        });
        return toCachedTransferInfoLiveData(transferOpResultLiveData, false);
    }

    /**
     * Download a blob.
     *
     * @param downloadRequest Describes the download request.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(DownloadRequest downloadRequest) {
        final WritableContent writableContent = downloadRequest.getWritableContent();
        final MutableLiveData<TransferOperationResult> transferOpResultLiveData = new MutableLiveData<>();
        try {
            // Take permission immediately in the UI_Thread (granting may require UI interaction).
            writableContent.takePersistableWritePermission();
        } catch (Throwable e) {
            transferOpResultLiveData
                .postValue(TransferOperationResult.error(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, e));
            return toCachedTransferInfoLiveData(transferOpResultLiveData, false);
        }
        this.serialTaskExecutor.execute(() -> {
            // BG_Thread
            try {
                StorageBlobAsyncClient blobClient = TransferClient.STORAGE_BLOB_CLIENTS.get(downloadRequest.getStorageClientId());
                if (blobClient == null) {
                    transferOpResultLiveData.postValue(TransferOperationResult
                        .unresolvedStorageClientIdError(TransferOperationResult.Operation.UPLOAD_DOWNLOAD,
                            downloadRequest.getStorageClientId()));
                    return;
                }
                blobClient.getBlobProperties(downloadRequest.getContainerName(), downloadRequest.getBlobName(),
                    new CallbackWithHeader<Void, BlobGetPropertiesHeaders>() {
                        @Override
                        public void onSuccess(Void result, BlobGetPropertiesHeaders header, Response response) {
                            final long blobSize = header.getContentLength();
                            BlobDownloadEntity blob = new BlobDownloadEntity(downloadRequest.getStorageClientId(),
                                downloadRequest.getContainerName(),
                                downloadRequest.getBlobName(),
                                blobSize,
                                writableContent,
                                downloadRequest.getConstraints());
                            List<BlockDownloadEntity> blocks
                                = BlockDownloadEntity.createBlockEntities(blobSize, Constants.DEFAULT_BLOCK_SIZE);
                            long transferId = db.downloadDao().createDownloadRecord(blob, blocks);

                            Log.v(TAG, "download(): Download record created: " + transferId);

                            Data inputData = new Data.Builder()
                                .putLong(DownloadWorker.Constants.INPUT_BLOB_DOWNLOAD_ID_KEY, transferId)
                                .build();
                            OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest
                                .Builder(DownloadWorker.class)
                                .setConstraints(downloadRequest.getConstraints())
                                .setInputData(inputData)
                                .build();

                            Log.v(TAG, "download(): enqueuing DownloadWorker: " + transferId);

                            workManager
                                .beginUniqueWork(toTransferUniqueWorkName(transferId),
                                    ExistingWorkPolicy.KEEP,
                                    downloadWorkRequest)
                                .enqueue();
                            transferOpResultLiveData
                                .postValue(TransferOperationResult.id(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, transferId));
                        }

                        @Override
                        public void onFailure(Throwable throwable, Response response) {
                            transferOpResultLiveData
                                .postValue(TransferOperationResult.error(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, throwable));
                        }
                    });
            } catch (Exception e) {
                transferOpResultLiveData
                    .postValue(TransferOperationResult.error(TransferOperationResult.Operation.UPLOAD_DOWNLOAD, e));
            }
        });

        // UI_Thread
        return toCachedTransferInfoLiveData(transferOpResultLiveData, false);
    }

    /**
     * Pause a transfer identified by the given transfer id. The pause operation
     * is a best-effort, and a transfer that is already executing may continue to
     * transfer.
     *
     * Upon successful scheduling of the pause, any observer observing on
     * the {@link LiveData} for this transfer receives a {@link TransferInfo}
     * event with state {@link TransferInfo.State#USER_PAUSED}.
     *
     * @param transferId The transfer id identifies the transfer to pause.
     */
    // P2: Currently no return value, evaluate any possible return value later.
    public void pause(long transferId) {
        // UI_Thread
        final TransferIdInfoLiveData.TransferFlags transferFlags = transferIdInfoCache.getTransferFlags(transferId);

        this.serialTaskExecutor.execute(() -> {
            // BG_Thread
            try {
                final StopCheck stopCheck = checkStoppable(transferId);

                if (stopCheck.canStop) {
                    if (stopCheck.isUpload) {
                        db.uploadDao().updateUploadInterruptState(transferId, TransferInterruptState.USER_PAUSED);
                    } else {
                        db.downloadDao().updateDownloadInterruptState(transferId, TransferInterruptState.USER_PAUSED);
                    }

                    if (transferFlags != null) {
                        transferFlags.setUserPaused();
                    }

                    workManager
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
     * @param transferId The transfer id identifies the transfer to resume.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the transfer.
     */
    public LiveData<TransferInfo> resume(long transferId) {
        // UI_Thread
        final MutableLiveData<TransferOperationResult> transferOpResultLiveData = new MutableLiveData<>();
        this.serialTaskExecutor.execute(() -> {
            // BG_Thread
            try {
                final ResumeCheck resumeCheck = checkResumeable(transferId, transferOpResultLiveData);

                if (resumeCheck.canResume) {
                    final OneTimeWorkRequest workRequest;
                    String blobTransferIdKey, logMessage;
                    Class<? extends ListenableWorker> workerClass;

                    if (resumeCheck.isUpload) {
                        blobTransferIdKey = UploadWorker.Constants.INPUT_BLOB_UPLOAD_ID_KEY;
                        logMessage = "Upload::resume() Enqueuing UploadWorker: " + transferId;
                        workerClass = UploadWorker.class;
                    } else { // Download
                        blobTransferIdKey = DownloadWorker.Constants.INPUT_BLOB_DOWNLOAD_ID_KEY;
                        logMessage = "Download::resume() Enqueuing DownloadWorker: " + transferId;
                        workerClass = DownloadWorker.class;
                    }

                    Data inputData = new Data.Builder()
                        .putLong(blobTransferIdKey, transferId)
                        .build();
                    workRequest = new OneTimeWorkRequest
                        .Builder(workerClass)
                        .setConstraints(resumeCheck.constraints)
                        .setInputData(inputData)
                        .build();

                    Log.v(TAG, logMessage);

                    // resume() will resubmit the work to WorkManager with the policy as KEEP.
                    // With this policy, if the work is already running, then this resume() call is NO-OP,
                    // we return the LiveData to the caller that streams the TransferInfo events of
                    // the already running work.
                    workManager
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
     * Cancel a transfer identified by the given transfer ID. The cancel operation is a best-effort, and a transfer
     * that is already executing may continue to transfer.
     *
     * Upon successful scheduling of the cancellation, any observer observing on {@link LiveData} for
     * this transfer receives a {@link TransferInfo} event with state {@link TransferInfo.State#CANCELLED}.
     *
     * @param transferId The transfer ID identifies the transfer to cancel.
     */
    // P2: Currently no return value, evaluate any possible return value later.
    public void cancel(long transferId) {
        this.serialTaskExecutor.execute(() -> {
            try {
                final StopCheck stopCheck = checkStoppable(transferId);

                if (stopCheck.canStop) {
                    if (stopCheck.isUpload) {
                        db.uploadDao().updateUploadInterruptState(transferId, TransferInterruptState.USER_CANCELLED);
                    } else {
                        db.downloadDao().updateDownloadInterruptState(transferId, TransferInterruptState.USER_CANCELLED);
                    }

                    workManager
                        .cancelUniqueWork(toTransferUniqueWorkName(transferId));
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to schedule cancellation for transfer with ID: " + transferId, e);
            }
        });
    }

    /**
     * Get unique name for a transfer work.
     *
     * @param transferId The transfer id.
     * @return The name for the transfer work.
     */
    static String toTransferUniqueWorkName(long transferId) {
        return "azure_transfer_" + transferId;
    }

    /**
     * Subscribe to a TransferOperationResult LiveData and transform that to TransferInfo LiveData.
     *
     * This method caches or uses cached {@link LiveData} to stream {@link TransferInfo}.
     * If provided TransferOperationResult LiveData emits an error, then cache won't be used.
     *
     * @param transferOpResultLiveData The LiveData to channel transfer operation initiation result
     * @param isResume True if the transfer id emitted by the transferOpResultLiveData LiveData
     *   identifies a transfer to be resumed, false for a new upload or download transfer.
     * @return The TransferInfo LiveData.
     */
    @MainThread
    private LiveData<TransferInfo> toCachedTransferInfoLiveData(LiveData<TransferOperationResult> transferOpResultLiveData,
                                                                boolean isResume) {
        // UI_Thread
        return Transformations.switchMap(transferOpResultLiveData, transferOpResult -> {
            if (transferOpResult.isError()) {
                final TransferIdInfoLiveData.Result result = TransferIdInfoLiveData.create(workManager);
                final TransferIdInfoLiveData.LiveDataPair pair = result.getLiveDataPair();
                pair.getTransferOpResultLiveData().setValue(transferOpResult);
                return pair.getTransferInfoLiveData();
            } else {
                if (isResume) {
                    // If the application process already has a cached LiveData pair for the same transfer,
                    // then use it, otherwise create, cache, and use.
                    final TransferIdInfoLiveData.LiveDataPair pair
                        = transferIdInfoCache.getOrCreate(transferOpResult.getId(), workManager);
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
                        = transferIdInfoCache.create(transferOpResult.getId(), workManager);
                    pair.getTransferOpResultLiveData().setValue(transferOpResult);
                    return pair.getTransferInfoLiveData();
                }
            }
        });
    }

    /**
     * Do pre-validations to see a transfer can be resumed.
     *
     * @param transferId Identifies the transfer to check for resume eligibility.
     * @param transferOpResultLiveData The LiveData to post the error if the transfer cannot be resumed.
     * @return Result of check.
     */
    private ResumeCheck checkResumeable(long transferId,
                                        MutableLiveData<TransferOperationResult> transferOpResultLiveData) {
        // Check for transfer record
        BlobUploadEntity uploadBlob = db.uploadDao().getBlob(transferId);
        BlobTransferState blobTransferState = null;
        String storageBlobClientId = null;
        Constraints constraints = null;

        if (uploadBlob != null) {
            blobTransferState = uploadBlob.state;
            storageBlobClientId = uploadBlob.storageBlobClientId;
            constraints = uploadBlob.constraintsColumn.toConstraints();
        } else {
            BlobDownloadEntity downloadBlob = db.downloadDao().getBlob(transferId);

            if (downloadBlob != null) {
                blobTransferState = downloadBlob.state;
                storageBlobClientId = downloadBlob.storageBlobClientId;
                constraints = downloadBlob.constraintsColumn.toConstraints();
            }
        }

        if (blobTransferState == null) {
            // No upload or download transfer found.
            transferOpResultLiveData.postValue(TransferOperationResult.notFoundError(transferId));

            return new ResumeCheck(false, false, constraints);
        } else {
            if (blobTransferState == BlobTransferState.FAILED) {
                transferOpResultLiveData.postValue(TransferOperationResult.alreadyInFailedStateError(transferId));

                return new ResumeCheck(false, true, constraints);
            } else if (blobTransferState == BlobTransferState.COMPLETED) {
                transferOpResultLiveData.postValue(TransferOperationResult.alreadyInCompletedStateError(transferId));

                return new ResumeCheck(false, true, constraints);
            } else if (!TransferClient.STORAGE_BLOB_CLIENTS.contains(storageBlobClientId)) {
                transferOpResultLiveData
                    .postValue(TransferOperationResult
                        .unresolvedStorageClientIdError(TransferOperationResult.Operation.UPLOAD_DOWNLOAD,
                            uploadBlob.storageBlobClientId));

                return new ResumeCheck(false, true, constraints);
            }

            return new ResumeCheck(true, true, constraints);
        }
    }

    /** Result of {@link this#checkResumeable(long, MutableLiveData)}} **/
    private static final class ResumeCheck {
        // Flag indicating whether transfer can be resumed.
        final boolean canResume;
        // If the transfer can be resumed then this flag indicates the transfer type (upload|download).
        final boolean isUpload;
        // The constraints to be satisfied to resume the transfer.
        final Constraints constraints;

        ResumeCheck(boolean canResume, boolean isUpload, Constraints constraints) {
            this.canResume = canResume;
            this.isUpload = isUpload;
            this.constraints = constraints;
        }
    }

    /**
     * Do pre-validations to see a transfer can be stopped (paused/cancelled).
     *
     * @param transferId Identifies the transfer to check for stopping eligibility.
     * @return Result of check.
     */
    private StopCheck checkStoppable(long transferId) {
        // Check for transfer record
        BlobUploadEntity uploadBlob = db.uploadDao().getBlob(transferId);
        BlobTransferState blobTransferState = null;

        if (uploadBlob != null) {
            blobTransferState = uploadBlob.state;
        } else {
            BlobDownloadEntity downloadBlob = db.downloadDao().getBlob(transferId);

            if (downloadBlob != null) {
                blobTransferState = downloadBlob.state;
            }
        }

        if (blobTransferState == null) {
            // No upload or download transfer found.
            return new StopCheck(false, false);
        } else {
            if (blobTransferState == BlobTransferState.FAILED) {
                return new StopCheck(false, true);
            } else if (blobTransferState == BlobTransferState.COMPLETED) {
                return new StopCheck(false, true);
            }

            return new StopCheck(true, true);
        }
    }

    /** Result of {@link this#checkStoppable(long)}} **/
    private static final class StopCheck {
        // Flag indicating whether transfer can be paused or cancelled.
        private final boolean canStop;
        // If the transfer can be paused or cancelled then this flag indicates the transfer type (upload|download).
        final boolean isUpload;

        private StopCheck(boolean canStop, boolean isUpload) {
            this.canStop = canStop;
            this.isUpload = isUpload;
        }
    }

    private static final class Constants {
        static final int KB = 1024;
        static final int MB = 1024 * KB;
        static final int DEFAULT_BLOCK_SIZE = 10 * Constants.MB;
    }
}
