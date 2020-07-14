// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.azure.android.core.http.CallbackSimple;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobDownloadOptions;
import com.azure.android.storage.blob.models.BlobRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Package private.
 * <p>
 * Handler that manages a single blob download.
 * <p>
 * Handler is a state machine, {@link DownloadHandlerMessage.Type} represents various stages that the state machine
 * goes through. Handler reacts to each stage appropriately. Reacting to a stage includes: initialization, starting
 * async block download operations, handling failure in operations, parking the work if the handler reaches stop state.
 * <p>
 * Additionally Handler is responsible for notifying the {@link TransferHandlerListener} on various events. Calls to
 * this listener methods are serialized, i.e. these methods won't be called concurrently.
 */
final class DownloadHandler extends Handler {
    private static final String TAG = DownloadHandler.class.getSimpleName();

    private final Context appContext;
    private final int blocksDownloadConcurrency;
    private final long downloadId;
    private final HashMap<String, BlockDownloadEntity> runningBlockDownloads;
    private final TransferStopToken transferStopToken;
    private final CancellationToken cancellationToken;

    private TransferHandlerListener transferHandlerListener;
    private TransferDatabase db;
    private BlobDownloadEntity blob;
    private long totalBytesDownloaded;
    private BlockDownloadRecordsEnumerator blocksItr;
    // The content in the device to store the downloaded blob.
    private WritableContent content;
    private StorageBlobAsyncClient blobClient;

    /**
     * Creates and initializes a {@link DownloadHandler}.
     *
     * @param looper     The looper to react on messages describing various blob download stages.
     * @param appContext The context.
     * @param downloadId The identifier to a {@link BlobDownloadEntity} in the local store, describing the blob to be
     *                   downloaded.
     */
    private DownloadHandler(Looper looper, Context appContext, int blocksDownloadConcurrency, long downloadId) {
        super(looper);

        this.appContext = appContext;
        this.blocksDownloadConcurrency = blocksDownloadConcurrency;
        this.downloadId = downloadId;
        runningBlockDownloads = new HashMap<>(this.blocksDownloadConcurrency);
        transferStopToken = new TransferStopToken(DownloadHandlerMessage.createStopMessage(this));
        this.cancellationToken = CancellationToken.create();
    }

    /**
     * Creates {@link DownloadHandler} for handling a single blob download.
     *
     * @param appContext The context.
     * @param downloadId The identifier to a {@link BlobDownloadEntity} in the local store, describing the blob to be
     *                   downloaded.
     * @return The DownloadHandler.
     */
    static DownloadHandler create(@NonNull Context appContext, int blocksDownloadConcurrency, long downloadId) {
        Objects.requireNonNull(appContext, "Application Context is null.");
        final HandlerThread handlerThread = new HandlerThread("DownloadHandlerThread");

        handlerThread.start();

        return new DownloadHandler(handlerThread.getLooper(), appContext, blocksDownloadConcurrency, downloadId);
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

                handleDownloadCompleted(message);

                break;
            case DownloadHandlerMessage.Type.DOWNLOAD_FAILED:
                Log.v(TAG, "handleMessage(): received message: DOWNLOAD_FAILED");

                handleDownloadFailed(message);

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
     * <p>
     * This stage acquires the DB, storage client and other resources required through out the life time of the
     * Handler. This stage also starts a download operation.
     */
    private void handleInit() {
        this.db = TransferDatabase.getInstance(appContext);
        this.blob = db.downloadDao().getBlob(downloadId);

        if (this.blob.interruptState == TransferInterruptState.PURGE) {
            this.transferHandlerListener.onError(new RuntimeException("Download operation with id '" + downloadId +
                "' is already CANCELLED and cannot be RESTARTED or RESUMED."));
            getLooper().quit();
        } else if (this.blob.state == BlobTransferState.COMPLETED) {
            this.transferHandlerListener.onTransferProgress(blob.blobSize, blob.blobSize);
            this.transferHandlerListener.onComplete();
            this.getLooper().quit();
        } else {

            this.blobClient = TransferClient.STORAGE_BLOB_CLIENTS.get(this.blob.storageBlobClientId);
            if (this.blobClient == null) {
                this.transferHandlerListener
                    .onError(new UnresolvedStorageBlobClientIdException(this.blob.storageBlobClientId));
                this.getLooper().quit();
            } else {
                this.content = new WritableContent(appContext,
                    Uri.parse(this.blob.contentUri),
                    this.blob.useContentResolver);
                try {
                    this.content.openForWrite(appContext);
                } catch (Throwable t) {
                    this.transferHandlerListener.onError(new RuntimeException("Download operation with id '" + downloadId +
                        "' cannot be processed, failed to open the content to write.", t));
                    getLooper().quit();
                }
                this.totalBytesDownloaded = this.db.downloadDao().getDownloadedBytesCount(downloadId);
                this.transferHandlerListener.onTransferProgress(blob.blobSize, totalBytesDownloaded);

                List<BlockTransferState> skip = new ArrayList<>();
                skip.add(BlockTransferState.COMPLETED);
                this.blocksItr = new BlockDownloadRecordsEnumerator(db, downloadId, skip);
                List<BlockDownloadEntity> blocks = blocksItr.getNext(blocksDownloadConcurrency);

                if (blocks.size() != 0) {
                    downloadBlocks(blocks);
                } else {
                    Log.w(TAG, "All blocks have been downloaded.");
                }
            }
        }
    }

    /**
     * Handles the blob download completion message received by the looper.
     * <p>
     * This stage notifies the completion of blob download to {@link TransferHandlerListener} and terminates the
     * handler.
     */
    private void handleDownloadCompleted(Message message) {
        finalizeIfStopped();

        String blockId = DownloadHandlerMessage.getBlockIdFromMessage(message);
        BlockDownloadEntity downloadedBlock = runningBlockDownloads.remove(blockId);
        totalBytesDownloaded += downloadedBlock.blockSize;
        transferHandlerListener.onTransferProgress(blob.blobSize, totalBytesDownloaded);
        List<BlockDownloadEntity> blocks = blocksItr.getNext(1);

        if (blocks.isEmpty()) {
            if (runningBlockDownloads.isEmpty()) {
                db.downloadDao().updateBlobState(downloadId, BlobTransferState.COMPLETED);

                closeContent();

                transferHandlerListener.onTransferProgress(blob.blobSize, blob.blobSize);
                transferHandlerListener.onComplete();
                getLooper().quit();
            }
        } else {
            downloadBlocks(blocks);
        }
    }

    /**
     * Handles the blocks download failed message received by the looper.
     * <p>
     * This stage notifies the failure to {@link TransferHandlerListener} and terminates the handler.
     */
    private void handleDownloadFailed(Message message) {
        String blockId = DownloadHandlerMessage.getBlockIdFromMessage(message);
        BlockDownloadEntity failedBlock = runningBlockDownloads.remove(blockId);

        this.cancellationToken.cancel();

        closeContent();

        Throwable downloadError = failedBlock.getDownloadError();
        blob.setDownloadError(downloadError);

        transferHandlerListener.onError(downloadError);
        getLooper().quit();
    }

    /**
     * Check whether stop token is signalled, if so park the work and quit the looper.
     */
    private void finalizeIfStopped() {
        if (transferStopToken.isStopped()) {
            Log.v(TAG, "finalizeIfStopped(): Stop request received, finalizing");

            this.cancellationToken.cancel();

            closeContent();

            TransferInterruptState interruptState = db.downloadDao().getTransferInterruptState(downloadId);

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
    private void downloadBlocks(List<BlockDownloadEntity> blocks) {
        for (BlockDownloadEntity block : blocks) {
            finalizeIfStopped();

            Log.v(TAG, "downloadBlob(): Downloading block: " + block.blockId + getThreadName());

            BlobDownloadOptions options = new BlobDownloadOptions();
            options.setRange(new BlobRange(block.blockOffset, block.blockSize));
            options.setCancellationToken(this.cancellationToken);

            blobClient.rawDownload(blob.containerName,
                blob.blobName,
                options,
                new CallbackSimple<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody value, Response response) {
                        try {
                            byte[] blockContent = value.bytes();
                            content.writeBlock(block.blockOffset, blockContent);
                        } catch (Throwable t) {
                            onFailure(t, response);
                            return;
                        }

                        Log.v(TAG, "downloadBlock(): Block downloaded:" + block.blockId + getThreadName());

                        db.downloadDao().updateBlockState(blob.key, BlockTransferState.COMPLETED);

                        Message nextMessage =
                            DownloadHandlerMessage.createBlockDownloadCompletedMessage(DownloadHandler.this, block.blockId);
                        nextMessage.sendToTarget();
                    }

                    @Override
                    public void onFailure(Throwable t, Response response) {
                        Log.e(TAG, "downloadFailed(): Block download failed: " + block.blockId, t);

                        db.downloadDao().updateBlockState(blob.key, BlockTransferState.FAILED);
                        block.setDownloadError(t);

                        Message nextMessage =
                            DownloadHandlerMessage.createBlockDownloadFailedMessage(DownloadHandler.this, block.blockId);
                        nextMessage.sendToTarget();
                    }
                });
            runningBlockDownloads.put(block.blockId, block);
        }
    }

    private void closeContent() {
        try {
            this.content.close();
        } catch (Throwable t) {
            Log.i(TAG, "content::close()", t);
        }
    }

    // For Debugging, will be removed (TODO: vcolin7)
    private static String getThreadName() {
        return " Thread:" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")";
    }
}
