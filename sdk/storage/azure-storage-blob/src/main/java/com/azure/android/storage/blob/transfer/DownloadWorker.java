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
 * <p>
 * {@link ListenableWorker} for performing a single blob download using {@link DownloadHandler}.
 */
public class DownloadWorker extends ListenableWorker {
    private static final String TAG = DownloadWorker.class.getSimpleName();

    /**
     * The key of the blob download metadata entity describing the blob to be downloaded.
     */
    private long blobDownloadId;
    /**
     * A token to signal {@link DownloadHandler} that it should be stopped.
     */
    private TransferStopToken transferStopToken;

    /**
     * Create the download worker.
     * <p>
     * {@link TransferClient} won't call this constructor directly, instead it will enqueue the work along with
     * criteria to execute it. The {@link androidx.work.WorkManager} call this constructor when it identifies that
     * the criteria are satisfied (e.g network is available).
     * <p>
     * If criteria configured for the Worker is no longer met while it is executing, then WorkManager will stop the
     * Worker by calling {@link ListenableWorker#stop()}. Such stopped Worker instance will be eventually GC-ed. When
     * constraints are again satisfied and if the Worker was stopped without completion then WorkManager will create
     * and starts new Worker instance.
     *
     * @param appContext   The context.
     * @param workerParams The input parameters to the worker.
     */
    public DownloadWorker(@NonNull Context appContext,
                          @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);

        this.blobDownloadId = getInputData().getLong(Constants.INPUT_BLOB_DOWNLOAD_ID_KEY, -1);

        if (this.blobDownloadId <= -1) {
            throw new IllegalArgumentException("Worker created with no or non-positive input blobDownloadId.");
        }
    }

    /**
     * WorkManager calls startWork() on the main thread, so it is important that the work this method does is
     * lightweight. This method creates a {@link DownloadHandler} and delegate the blob download work.
     *
     * @return The future representing the async download.
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
                    completer.setException(t);
                }
            };

            DownloadHandler handler = DownloadHandler.create(getApplicationContext(), this.blobDownloadId);
            this.transferStopToken = handler.beginDownload(transferHandlerListener);

            return transferHandlerListener;
        });

        return listenableFuture;
    }

    /**
     * Called by WorkManager to stop the work. This can happen if the constraints for execution are no longer
     * satisfied or the user explicitly stopped the worker (e.g. cancel or pause download) or the Worker is executed
     * for maximum time it is allowed to run.
     */
    @Override
    public void onStopped() {
        Log.v(TAG, "onStopped() called." + this);

        this.transferStopToken.stop();
    }

    static class Constants {
        /**
         * Identifies an entry in {@link WorkerParameters} input to {@link DownloadWorker} that holds the blob
         * downloadId.
         * <p>
         * A downloadId is the key to a blob download metadata entity in local store.
         */
        static final String INPUT_BLOB_DOWNLOAD_ID_KEY = "ibdik";
    }
}
