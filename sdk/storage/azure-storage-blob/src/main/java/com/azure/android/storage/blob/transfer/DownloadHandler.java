// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.android.storage.blob.models.BlobRange;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

// TODO: Apply parallelism and keep track of the progress. Figure how to retry downloading from a given point.
/**
 * Package private.
 *
 * Handler that manages a single blob download.
 *
 * Handler is a state machine, {@link DownloadHandlerMessage.Type} represents various stages that the state machine
 * goes through. Handler reacts to each stage appropriately. Reacting to a stage includes: initialization, starting
 * async block download operations, handling failure in operations, parking the work if the handler reaches stop state.
 *
 * Additionally Handler is responsible for notifying the {@link TransferHandlerListener} on various events. Calls to
 * this listener methods are serialized, i.e. these methods won't be called concurrently.
 */
final class DownloadHandler extends Handler {
    private static final String TAG = DownloadHandler.class.getSimpleName();
    private static final int BUFFER_SIZE = 2048;

    private final Context appContext;
    private final long downloadId;
    private final TransferStopToken transferStopToken;

    private TransferHandlerListener transferHandlerListener;
    private TransferDatabase db;
    private BlobDownloadEntity blob;
    private StorageBlobClient blobClient;

    /**
     * Creates and initializes a {@link DownloadHandler}.
     *
     * @param looper The looper to react on messages describing various blob download stages.
     * @param appContext The context.
     * @param downloadId The identifier to a {@link BlobDownloadEntity} in the local store, describing the blob to be
     *                  downloaded.
     */
    private DownloadHandler(Looper looper, Context appContext, long downloadId) {
        super(looper);

        this.appContext = appContext;
        this.downloadId = downloadId;
        transferStopToken = new TransferStopToken(DownloadHandlerMessage.createStopMessage(this));
    }

    /**
     * Creates {@link DownloadHandler} for handling a single blob download.
     *
     * @param appContext The context.
     * @param downloadId The identifier to a {@link BlobDownloadEntity} in the local store, describing the blob to be
     *                  downloaded.
     * @return The DownloadHandler.
     */
    static DownloadHandler create(@NonNull Context appContext, long downloadId) {
        Objects.requireNonNull(appContext, "Application Context is null.");
        final HandlerThread handlerThread = new HandlerThread("DownloadHandlerThread");

        handlerThread.start();

        return new DownloadHandler(handlerThread.getLooper(), appContext, downloadId);
    }

    /**
     * Begin downloading the blob.
     *
     * @param transferHandlerListener The listener to send the download events.
     * @return The token to stop the download.
     */
    TransferStopToken beginDownload(TransferHandlerListener transferHandlerListener) {
        this.transferHandlerListener =
            Objects.requireNonNull(transferHandlerListener, "transferHandlerListener is null.");

        Log.i(TAG, "beginDownload(): downloadId: " + downloadId);

        Message message = DownloadHandlerMessage.createInitMessage(this);
        message.sendToTarget();

        return transferStopToken;
    }

    @Override
    public void handleMessage(@NonNull Message message) {
        finalizeIfStopped();

        int msgType = DownloadHandlerMessage.getMessageType(message);

        switch (msgType) {
            case DownloadHandlerMessage.Type.INIT:
                Log.v(TAG, "handleMessage(): received message: INIT");

                handleInit();

                break;
            case DownloadHandlerMessage.Type.DOWNLOAD_COMPLETED:
                Log.v(TAG, "handleMessage(): received message: DOWNLOAD_COMPLETED");

                handleDownloadCompleted();

                break;
            case DownloadHandlerMessage.Type.DOWNLOAD_FAILED:
                Log.v(TAG, "handleMessage(): received message: DOWNLOAD_FAILED");

                handleDownloadFailed();

                break;
            case DownloadHandlerMessage.Type.STOP:
                Log.v(TAG, "handleMessage(): received message: STOP");

                finalizeIfStopped();

                break;
            default:
        }
    }

    /**
     * Handles blob download initialization message received by the handler.
     *
     * This stage acquires the DB, storage client and other resources required through out the life time of the
     * Handler. This stage also starts a download operation.
     */
    private void handleInit() {
        db = TransferDatabase.get(appContext);
        blob = db.downloadDao().getBlob(downloadId);

        if (blob.interruptState == TransferInterruptState.PURGE) {
            transferHandlerListener.onError(new RuntimeException("Download operation with id '" + downloadId +
                "' is already CANCELLED and cannot be RESTARTED or RESUMED."));
            getLooper().quit();
        } else if (blob.state == BlobDownloadState.COMPLETED) {
            transferHandlerListener.onTransferProgress(blob.blobSize, blob.blobSize);
            transferHandlerListener.onComplete();
            getLooper().quit();
        } else {
            blobClient = StorageBlobClientsMap.get(downloadId);

            downloadBlob();
        }
    }

    /**
     * Handles the blob download completion message received by the looper.
     *
     * This stage notifies the completion of blob download to {@link TransferHandlerListener} and terminates the
     * handler.
     */
    private void handleDownloadCompleted() {
        transferHandlerListener.onTransferProgress(blob.blobSize, blob.blobSize);
        transferHandlerListener.onComplete();
        getLooper().quit();
    }

    /**
     * Handles the blocks commit failed message received by the looper.
     *
     * This stage notifies the failure to {@link TransferHandlerListener} and terminates
     * the handler.
     */
    private void handleDownloadFailed() {
        transferHandlerListener.onError(blob.getDownloadError());
        getLooper().quit();
    }

    /**
     * Check whether stop token is signalled, if so park the work and quit the looper.
     */
    private void finalizeIfStopped() {
        if (transferStopToken.isStopped()) {
            Log.v(TAG, "finalizeIfStopped(): Stop request received, finalizing");

            TransferInterruptState interruptState = db.downloadDao().getDownloadInterruptState(downloadId);

            Log.v(TAG, "finalizeIfStopped: Stop request reason (NONE == Stop requested by SYSTEM): " + interruptState);

            switch (interruptState) {
                case NONE:
                    transferHandlerListener.onSystemPaused();
                    break;
                case USER_PAUSED:
                    transferHandlerListener.onUserPaused();
                    break;
                case USER_CANCELLED:
                    db.downloadDao().updateDownloadInterruptState(downloadId, TransferInterruptState.PURGE);
                    transferHandlerListener.onError(new TransferCancelledException(downloadId));
            }

            getLooper().quit();
        }
    }

    /**
     * Starts the blob download operation.
     */
    private void downloadBlob() {
        this.finalizeIfStopped();

        Log.v(TAG, "downloadBlob(): Downloading blob: " + blob.blobName + getThreadName());

        db.downloadDao().updateBlobState(blob.key, BlobDownloadState.IN_PROGRESS);
        blobClient.downloadWithResponse(blob.containerName,
            blob.blobName,
            null,
            null,
            new BlobRange(blob.blobOffset),
            null,
            null,
            null,
            null,
            null,
            null,
            new com.azure.android.core.http.Callback<BlobDownloadAsyncResponse>() {
                @Override
                public void onResponse(BlobDownloadAsyncResponse response) {
                    Log.v(TAG, "downloadBlob(): Downloading blob: " + blob.blobName + getThreadName());

                    blob.blobSize = response.getDeserializedHeaders().getContentLength();

                    Log.v(TAG, "downloadBlob(): Blob size: " + blob.blobSize);

                    File file = new File(blob.filePath);
                    boolean appendToFile = blob.blobOffset != 0;
                    long totalBytesDownloaded = 0;

                    try (BufferedInputStream in = new BufferedInputStream(response.getValue().byteStream(),
                            BUFFER_SIZE);
                         FileOutputStream fileOutputStream = new FileOutputStream(file, appendToFile)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead = 0;

                        while (bytesRead != -1) {
                             bytesRead = in.read(buffer);
                             fileOutputStream.write(buffer);
                             totalBytesDownloaded += bytesRead;
                             transferHandlerListener.onTransferProgress(blob.blobSize, totalBytesDownloaded);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "downloadBlob(): Error when downloading blob: " + blob.containerName + "/" +
                            blob.blobName, e);

                        blob.blobOffset = totalBytesDownloaded;

                        Log.d(TAG, "downloadBlob(): Total bytes downloaded: " + blob.blobOffset);

                        onFailure(e);
                    }

                    db.downloadDao().updateBlobState(blob.key, BlobDownloadState.COMPLETED);

                    Message nextMessage = DownloadHandlerMessage.createDownloadCompletedMessage(DownloadHandler.this);

                    nextMessage.sendToTarget();
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "downloadBlob(): Blob download failed: " + blob.containerName + "/" + blob.blobName +
                        getThreadName(), t);

                    db.downloadDao().updateBlobState(blob.key, BlobDownloadState.FAILED);
                    blob.setDownloadError(t);

                    Message nextMessage = DownloadHandlerMessage.createDownloadFailedMessage(DownloadHandler.this);

                    nextMessage.sendToTarget();
                }
            });
    }

    // For Debugging, will be removed (TODO: vcolin7)
    private static String getThreadName() {
        return " Thread:" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")";
    }
}
