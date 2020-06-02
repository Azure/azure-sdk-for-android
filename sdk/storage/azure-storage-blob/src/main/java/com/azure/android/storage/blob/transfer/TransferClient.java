// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
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
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.impl.WorkManagerImpl;

import com.azure.android.storage.blob.StorageBlobClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A type that exposes blob transfer APIs.
 */
public class TransferClient {
    // the static shared map of StorageBlobClient instances for transfers, with package
    // scoped access. StorageBlobClient instances are added from TransferClient.Builder
    // and used by Upload|Download handlers.
    static final StorageBlobClientMap STORAGE_BLOB_CLIENTS = new StorageBlobClientMap();
    private static final String TAG = TransferClient.class.getSimpleName();
    // the application context.
    private final Context context;
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
     * @param constraints the constraints to meet to run transfers
     * @param serialTaskExecutor the executor for all internal book keeping purposes
     * @param storageBlobClients the blob storage clients for transfers
     */
    private TransferClient(Context context,
                           Constraints constraints,
                           SerialExecutor serialTaskExecutor,
                           Map<String, StorageBlobClient> storageBlobClients) {
        this.context = context;
        this.constraints = constraints;
        this.serialTaskExecutor = serialTaskExecutor;
        this.db = TransferDatabase.get(context);
        STORAGE_BLOB_CLIENTS.putAll(storageBlobClients);
    }

    /**
     * Upload the content of a file.
     *
     * @param storageBlobClientId the identifier of the blob storage client to use for the upload
     * @param containerName the container to upload the file to
     * @param blobName the name of the target blob holding uploaded file
     * @param file the local file to upload
     * @return LiveData that streams {@link TransferInfo} describing current state of the transfer
     */
    public LiveData<TransferInfo> upload(String storageBlobClientId, String containerName, String blobName, File file) {
        // UI_Thread
        return upload(storageBlobClientId, containerName, blobName,
            new ReadableContent(this.context, Uri.fromFile(file), false));
    }

    /**
     * Upload content identified by a given Uri.
     *
     * @param storageBlobClientId the identifier of the blob storage client to use for the upload
     * @param containerName the container to upload the file to
     * @param blobName the name of the target blob holding uploaded file
     * @param contentUri URI to the Content to upload, the contentUri is resolved using
     *   {@link android.content.ContentResolver#openAssetFileDescriptor(Uri, String)}
     *   with mode as "r". The supported URI schemes are: 'content://', 'file://' and 'android.resource://'
     * @return LiveData that streams {@link TransferInfo} describing current state of the transfer
     */
    public LiveData<TransferInfo> upload(String storageBlobClientId, String containerName, String blobName, Uri contentUri) {
        // UI_Thread
        return upload(storageBlobClientId, containerName, blobName,
            new ReadableContent(this.context, contentUri, true));
    }

    /**
     * Upload the content described by the given {@link ReadableContent}.
     *
     * @param storageBlobClientId the identifier of the blob storage client to use for the upload
     * @param containerName the container to upload the file to
     * @param blobName the name of the target blob holding uploaded file
     * @param readableContent describes the Content to read and upload
     * @return LiveData that streams {@link TransferInfo} describing current state of the transfer
     */
    private LiveData<TransferInfo> upload(String storageBlobClientId,
                                          String containerName,
                                          String blobName,
                                          ReadableContent readableContent) {
        // UI_Thread
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
            // BG_Thread
            try {
                if (!TransferClient.STORAGE_BLOB_CLIENTS.contains(storageBlobClientId)) {
                    transferOpResultLiveData.postValue(TransferOperationResult
                        .unresolvedStorageClientIdError(TransferOperationResult.Operation.UPLOAD_DOWNLOAD,
                            storageBlobClientId));
                    return;
                }
                BlobUploadEntity blob = new BlobUploadEntity(storageBlobClientId,
                    containerName,
                    blobName,
                    readableContent);
                List<BlockUploadEntity> blocks
                    = BlockUploadEntity.createBlockEntities(readableContent.getLength(), Constants.DEFAULT_BLOCK_SIZE);
                long transferId = db.uploadDao().createUploadRecord(blob, blocks);
                Log.v(TAG, "upload(): upload record created: " + transferId);

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
            } catch (Throwable e) {
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
     * @param storageBlobClientId the identifier of the blob storage client to use for the download
     * @param containerName The container to download the blob from.
     * @param blobName The name of the target blob to download.
     * @param file The local file to download to.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(String storageBlobClientId, String containerName, String blobName, File file) {
        return download(storageBlobClientId,
            containerName,
            blobName,
            new WritableContent(this.context, Uri.fromFile(file), false));
    }

    /**
     * Download a blob.
     *
     * @param storageBlobClientId the identifier of the blob storage client to use for the download
     * @param containerName The container to download the blob from.
     * @param blobName The name of the target blob to download.
     * @param contentUri The URI to the local content where the downloaded blob will be stored.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(String storageBlobClientId, String containerName, String blobName, Uri contentUri) {
        return download(storageBlobClientId,
            containerName,
            blobName,
            new WritableContent(this.context, contentUri, true));
    }

    /**
     * Download a blob.
     *
     * @param storageBlobClientId the identifier of the blob storage client to use for the download
     * @param containerName The container to download the blob from.
     * @param blobName The name of the target blob to download.
     * @param writableContent describes the Content in the device to store the downloaded blob.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(String storageBlobClientId,
                                           String containerName,
                                           String blobName,
                                           WritableContent writableContent) {
        // UI_Thread
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
                StorageBlobClient blobClient = TransferClient.STORAGE_BLOB_CLIENTS.get(storageBlobClientId);
                if (blobClient == null) {
                    transferOpResultLiveData.postValue(TransferOperationResult
                        .unresolvedStorageClientIdError(TransferOperationResult.Operation.UPLOAD_DOWNLOAD,
                            storageBlobClientId));
                    return;
                }

                long blobSize = blobClient.getBlobProperties(containerName, blobName).getContentLength();
                BlobDownloadEntity blob = new BlobDownloadEntity(storageBlobClientId,
                    containerName,
                    blobName,
                    blobSize,
                    writableContent);
                List<BlockDownloadEntity> blocks
                    = BlockDownloadEntity.createBlockEntities(blobSize, Constants.DEFAULT_BLOCK_SIZE);
                long transferId = db.downloadDao().createDownloadRecord(blob, blocks);

                Log.v(TAG, "download(): Download record created: " + transferId);

                Data inputData = new Data.Builder()
                    .putLong(DownloadWorker.Constants.INPUT_BLOB_DOWNLOAD_ID_KEY, transferId)
                    .build();
                OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest
                    .Builder(DownloadWorker.class)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build();

                Log.v(TAG, "download(): enqueuing DownloadWorker: " + transferId);

                WorkManager.getInstance(context)
                    .beginUniqueWork(toTransferUniqueWorkName(transferId),
                        ExistingWorkPolicy.KEEP,
                        downloadWorkRequest)
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
                        .setConstraints(constraints)
                        .setInputData(inputData)
                        .build();

                    Log.v(TAG, logMessage);

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
     * Cancel a transfer identified by the given transfer ID. The cancel operation is a best-effort, and a transfer
     * that is already executing may continue to transfer.
     *
     * Upon successful scheduling of the cancellation, any observer observing on {@link LiveData<TransferInfo>} for
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

                    WorkManager
                        .getInstance(context)
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
        // Check for transfer record
        BlobUploadEntity uploadBlob = db.uploadDao().getBlob(transferId);
        BlobTransferState blobTransferState = null;
        String storageBlobClientId = null;

        if (uploadBlob != null) {
            blobTransferState = uploadBlob.state;
            storageBlobClientId = uploadBlob.storageBlobClientId;
        } else {
            BlobDownloadEntity downloadBlob = db.downloadDao().getBlob(transferId);

            if (downloadBlob != null) {
                blobTransferState = downloadBlob.state;
                storageBlobClientId = downloadBlob.storageBlobClientId;
            }
        }

        if (blobTransferState == null) {
            // No upload or download transfer found.
            transferOpResultLiveData.postValue(TransferOperationResult.notFoundError(transferId));

            return new ResumeCheck(false, false);
        } else {
            if (blobTransferState == BlobTransferState.FAILED) {
                transferOpResultLiveData.postValue(TransferOperationResult.alreadyInFailedStateError(transferId));

                return new ResumeCheck(false, true);
            } else if (blobTransferState == BlobTransferState.COMPLETED) {
                transferOpResultLiveData.postValue(TransferOperationResult.alreadyInCompletedStateError(transferId));

                return new ResumeCheck(false, true);
            } else if (!TransferClient.STORAGE_BLOB_CLIENTS.contains(storageBlobClientId)) {
                transferOpResultLiveData
                    .postValue(TransferOperationResult
                        .unresolvedStorageClientIdError(TransferOperationResult.Operation.UPLOAD_DOWNLOAD,
                            uploadBlob.storageBlobClientId));

                return new ResumeCheck(false, true);
            }

            return new ResumeCheck(true, true);
        }
    }

    /** Result of {@link this#checkResumeable(long, MutableLiveData)}} **/
    private static final class ResumeCheck {
        // Flag indicating whether transfer can be resumed.
        final boolean canResume;
        // If the transfer can be resumed then this flag indicates the transfer type (upload|download).
        final boolean isUpload;

        ResumeCheck(boolean canResume, boolean isUpload) {
            this.canResume = canResume;
            this.isUpload = isUpload;
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

    /**
     * A builder to configure and build a {@link TransferClient}.
     */
    public static final class Builder {
        // the application context.
        private Context context;
        // a map of storage clients to use for transfers.
        private Map<String, StorageBlobClient> storageBlobClients = new HashMap<>();
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
         * Add a {@link StorageBlobClient} to be used for transfers.
         *
         * @param storageBlobClientId the unique name or id for the blob storage client.
         *   This identifier is used to associate the given blob storage client with transfers
         *   that {@link TransferClient} creates. When a transfer is reloaded from disk (e.g. after an
         *   application crash), it can only be resumed once a client with the same storageBlobClientId
         *   has been initialized. If your application only uses a single {@link StorageBlobClient},
         *   it is recommended to use a value unique to your application (e.g. "MyApplication").
         *   If your application uses multiple clients with different configurations, use a value unique
         *   to both your application and the configuration (e.g. "MyApplication.userClient").
         * @param storageBlobClient the blob storage client
         * @return Builder with the provided blob storage client set
         */
        public Builder addStorageBlobClient(@NonNull String storageBlobClientId,
                                            @NonNull StorageBlobClient storageBlobClient) {
            this.storageBlobClients.put(storageBlobClientId, storageBlobClient);
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
            if (this.storageBlobClients.isEmpty()) {
                throw new IllegalArgumentException("At least one storageBlobClient must be set.");
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
                constraintsBuilder.build(),
                this.serialTaskExecutor,
                this.storageBlobClients);
        }
    }

    private static final class Constants {
        static final int KB = 1024;
        static final int MB = 1024 * KB;
        static final int DEFAULT_BLOCK_SIZE = 10 * Constants.MB;
    }
}
