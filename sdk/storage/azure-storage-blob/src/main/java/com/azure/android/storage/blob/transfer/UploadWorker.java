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

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Package private.
 *
 * {@link ListenableWorker} for performing a single file upload using {@link UploadHandler}.
 */
class UploadWorker extends ListenableWorker {
    private static final String TAG = UploadWorker.class.getSimpleName();
    // The number of blob blocks to be uploaded in parallel.
    private int blocksUploadConcurrency;
    // The key of the blob upload metadata entity describing the file to be uploaded.
    private long blobUploadId;
    // A token to signal {@link UploadHandler} that it should be stopped.
    private UploadStopToken uploadStopToken;

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
            = getInputData().getLong(Constants.INPUT_BLOB_UPLOAD_ID_KEY, -1);
        if (this.blobUploadId <= -1) {
            throw new IllegalArgumentException("Worker created with no or non-positive input blobUploadId.");
        }
        this.blocksUploadConcurrency
            = getInputData().getInt(Constants.INPUT_BLOCKS_UPLOAD_CONCURRENCY_KEY,
            Constants.DEFAULT_BLOCKS_UPLOAD_CONCURRENCY);
        if (this.blocksUploadConcurrency <= 0) {
            this.blocksUploadConcurrency = Constants.DEFAULT_BLOCKS_UPLOAD_CONCURRENCY;
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
            UploadHandlerListener uploadHandlerListener = new UploadHandlerListener() {
                @Override
                public void onUploadProgress(long totalBytes, long bytesUploaded) {
                    setProgressAsync(new Data.Builder()
                        .putLong(Constants.PROGRESS_TOTAL_BYTES, totalBytes)
                        .putLong(Constants.PROGRESS_BYTES_UPLOADED, bytesUploaded)
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
                    completer.setException(t);
                }
            };
            UploadHandler handler = UploadHandler.create(getApplicationContext(),
                this.blocksUploadConcurrency,
                this.blobUploadId);
            this.uploadStopToken = handler.beginUpload(uploadHandlerListener);
            return uploadHandlerListener;
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
        this.uploadStopToken.stop();
    }

    static class Constants {
        /**
         * Identifies an entry in {@link WorkerParameters} input to {@link UploadWorker} that
         * holds blocksUploadConcurrency value.
         */
        static final String INPUT_BLOCKS_UPLOAD_CONCURRENCY_KEY = "ick";
        /**
         * Identifies an entry in {@link WorkerParameters} input to {@link UploadWorker} that
         * holds blob uploadId.
         * An uploadId is the key to a blob upload metadata entity in local store.
         */
        static final String INPUT_BLOB_UPLOAD_ID_KEY = "ibuik";
        /**
         * The default block upload parallelism.
         */
        static final int DEFAULT_BLOCKS_UPLOAD_CONCURRENCY = 3;
        /**
         * Identifies an entry {@link Data} passed to {@link ListenableWorker#setProgressAsync(Data)},
         * holding the total bytes to upload.
         */
        static final String PROGRESS_TOTAL_BYTES = "TOTAL_BYTES";
        /**
         * Identifies an entry {@link Data} passed to {@link ListenableWorker#setProgressAsync(Data)},
         * holding the total bytes uploaded so far.
         */
        static final String PROGRESS_BYTES_UPLOADED = "BYTES_UPLOADED";
    }
}
