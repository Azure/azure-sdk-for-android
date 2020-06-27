// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.azure.android.core.util.CoreUtil;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * {@link ListenableWorker} for performing a single file upload using {@link UploadHandler}.
 */
public class UploadWorker extends ListenableWorker {
    private static final String TAG = UploadWorker.class.getSimpleName();
    // The number of blob blocks to be uploaded in parallel.
    private int blocksUploadConcurrency;
    // The key of the blob upload metadata entity describing the file to be uploaded.
    private String blobUploadId;
    // A token to signal {@link UploadHandler} that it should be stopped.
    private TransferStopToken transferStopToken;

    /**
     * Create the upload worker.
     *
     * {@link TransferClient} won't call this constructor directly instead it enqueue the work along
     * with criteria to execute it. The {@link androidx.work.WorkManager} call this Ctr when it
     * identifies that the criteria are satisfied (e.g network is available).
     *
     * If criteria configured for the Worker is no longer met while it is executing, then WorkManager
     * will stop the Worker by calling {@link ListenableWorker#stop()}. Such stopped Worker instance
     * will be eventually GC-ed. When constraints are again satisfied and if the Worker was stopped
     * without completion then WorkManager will create and starts new Worker instance.
     *
     * @param appContext the context
     * @param workerParams the input parameters to the worker
     */
    public UploadWorker(@NonNull Context appContext,
                        @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.blobUploadId
            = getInputData().getString(Constants.INPUT_BLOB_UPLOAD_ID_KEY);
        if (this.blobUploadId == null) {
            throw new IllegalArgumentException("Worker created with null input blobUploadId.");
        }
        this.blocksUploadConcurrency
            = getInputData().getInt(TransferConstants.INPUT_BLOCKS_UPLOAD_CONCURRENCY_KEY,
            TransferConstants.DEFAULT_BLOCKS_UPLOAD_CONCURRENCY);
        if (this.blocksUploadConcurrency <= 0) {
            this.blocksUploadConcurrency = TransferConstants.DEFAULT_BLOCKS_UPLOAD_CONCURRENCY;
        }
    }

    /**
     * WorkManager calls startWork() on main-thread, so it is important that the work this method
     * does is light weight. This method creates an {@link UploadHandler} and delegate the
     * file upload work.
     *
     * @return the future representing the async upload.
     */
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.v(TAG, "startWork() called." + this);
        ListenableFuture<Result> listenableFuture = CallbackToFutureAdapter.getFuture(completer -> {
            TransferHandlerListener transferHandlerListener = new TransferHandlerListener() {
                @Override
                public void onTransferProgress(long totalBytes, long bytesTransferred) {
                    setProgressAsync(new Data.Builder()
                        .putLong(TransferConstants.PROGRESS_TOTAL_BYTES, totalBytes)
                        .putLong(TransferConstants.PROGRESS_BYTES_TRANSFERRED, bytesTransferred)
                        .build());
                }

                @Override
                public void onUserPaused() {
                    // TODO: anuchan - use this once decide on public non-live-data listener.
                }

                @Override
                public void onSystemPaused() {
                    // TODO: anuchan - use this once decide on public non-live-data listener.
                }

                @Override
                public void onComplete() {
                    completer.set(Result.success());
                }

                @Override
                public void onError(Throwable t) {
                    String errorMessage = null;
                    if (t instanceof BlobStorageException) {
                        errorMessage = Util.tryGetNormalizedError((BlobStorageException) t);
                    }
                    if (CoreUtil.isNullOrEmpty(errorMessage)) {
                        errorMessage = t.getMessage();
                    }
                    int lenToTrim = errorMessage.length() - Data.MAX_DATA_BYTES;
                    if (lenToTrim > 0) {
                        errorMessage = errorMessage.substring(0, errorMessage.length() - lenToTrim);
                    }
                    Data errorOutput = new Data.Builder()
                        .putString(TransferConstants.OUTPUT_ERROR_MESSAGE_KEY, errorMessage)
                        .build();
                    completer.set(Result.failure(errorOutput));
                }
            };
            UploadHandler handler = UploadHandler.create(getApplicationContext(),
                this.blocksUploadConcurrency,
                this.blobUploadId);
            this.transferStopToken = handler.beginUpload(transferHandlerListener);
            return transferHandlerListener;
        });
        return listenableFuture;
    }

    /**
     * Called by WorkManager to stop the work, this can happen if the constraints for execution
     * are no longer satisfied or user explicitly stopped the worker (e.g. cancel or pause upload)
     * or Worker executed for maximum time it is allowed to run.
     */
    @Override
    public void onStopped() {
        Log.v(TAG, "onStopped() called." + this);
        this.transferStopToken.stop();
    }

    static class Constants {
        /**
         * Identifies an entry in {@link WorkerParameters} input to {@link UploadWorker} that
         * holds blob uploadId.
         * An uploadId is the key to a blob upload metadata entity in local store.
         */
        static final String INPUT_BLOB_UPLOAD_ID_KEY = "ibuik";
    }
}
