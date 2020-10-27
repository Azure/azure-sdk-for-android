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

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Response;

/**
 * Package private.
 *
 * Handler that manages a single file upload.
 *
 * Handler is a state machine, {@link UploadHandlerMessage.Type} represents various stages
 * that the state machine goes through. Handler react to each stage appropriately.
 * Reacting to stage includes - initialization, starting async block upload operations and
 * blocks commit operation, handling failure in operations, parking the work if the handler
 * reaches stop state.
 *
 * Additionally Handler is responsible for notifying {@link TransferHandlerListener} on various events.
 * Calls to this listener methods are serialized, i.e. these methods won't be called concurrently.
 */
final class UploadHandler extends Handler {
    private static final String TAG = UploadHandler.class.getSimpleName();

    private final Context appContext;
    private final int blocksUploadConcurrency;
    private final long uploadId;
    private final HashMap<String, BlockUploadEntity> runningBlockUploads;
    private final TransferStopToken transferStopToken;
    private final CancellationToken cancellationToken;

    private TransferHandlerListener transferHandlerListener;
    private TransferDatabase db;
    private BlobUploadEntity blob;
    private long totalBytesUploaded;
    private BlockUploadRecordsEnumerator blocksItr;
    //  The content in the device representing the data to be read and uploaded.
    private ReadableContent content;
    private StorageBlobAsyncClient blobClient;

    /**
     * Create and initializes {@link UploadHandler}.
     *
     * @param looper the looper to react on messages describing various file upload stages
     * @param appContext the context
     * @param blocksUploadConcurrency the number of blocks to be uploaded in parallel
     * @param uploadId the identifier to a {@link BlobUploadEntity} in the local store, describing
     *     the file to be uploaded
     */
    private UploadHandler(Looper looper, Context appContext, int blocksUploadConcurrency, long uploadId) {
        super(looper);
        this.appContext = appContext;
        this.blocksUploadConcurrency = blocksUploadConcurrency;
        this.uploadId = uploadId;
        this.runningBlockUploads = new HashMap<>(this.blocksUploadConcurrency);
        this.transferStopToken = new TransferStopToken(UploadHandlerMessage.createStopMessage(this));
        this.cancellationToken = CancellationToken.create();
    }

    /**
     * Creates {@link UploadHandler} for handling a single file upload.
     *
     * @param appContext the context
     * @param blocksUploadConcurrency the number of blocks to be uploaded in parallel
     * @param uploadId the identifier to a {@link BlobUploadEntity} in the local store, describing the
     *     file to be uploaded
     * @return the UploadHandler
     */
    static UploadHandler create(@NonNull Context appContext, int blocksUploadConcurrency, long uploadId) {
        Objects.requireNonNull(appContext, "Application Context is null.");
        final HandlerThread handlerThread = new HandlerThread("UploadHandlerThread");
        handlerThread.start();
        return new UploadHandler(handlerThread.getLooper(), appContext, blocksUploadConcurrency, uploadId);
    }

    /**
     * Begin uploading the file.
     *
     * @param transferHandlerListener the listener to send the upload events
     * @return the token to stop the upload
     */
    TransferStopToken beginUpload(TransferHandlerListener transferHandlerListener) {
        this.transferHandlerListener =
            Objects.requireNonNull(transferHandlerListener, "transferHandlerListener is null.");
        Log.i(TAG, "beginUpload(): uploadId:" + this.uploadId);
        Message message = UploadHandlerMessage.createInitMessage(this);
        message.sendToTarget();
        return this.transferStopToken;
    }

    @Override
    public void handleMessage(Message msg) {
        this.finalizeIfStopped();
        int msgType = UploadHandlerMessage.getMessageType(msg);
        switch (msgType) {
            case UploadHandlerMessage.Type.INIT:
                Log.v(TAG, "handleMessage(): received message: INIT");
                this.handleInit();
                break;
            case UploadHandlerMessage.Type.STAGING_COMPLETED:
                Log.v(TAG, "handleMessage(): received message: STAGING_COMPLETED");
                this.handleStagingCompleted(msg);
                break;
            case UploadHandlerMessage.Type.STAGING_FAILED:
                Log.v(TAG, "handleMessage(): received message: STAGING_FAILED");
                this.handleStagingFailed(msg);
                break;
            case UploadHandlerMessage.Type.COMMIT_COMPLETED:
                Log.v(TAG, "handleMessage(): received message: COMMIT_COMPLETED");
                this.handleCommitCompleted();
                break;
            case UploadHandlerMessage.Type.COMMIT_FAILED:
                Log.v(TAG, "handleMessage(): received message: COMMIT_FAILED");
                this.handleCommitFailed();
                break;
            case UploadHandlerMessage.Type.STOP:
                Log.v(TAG, "handleMessage(): received message: STOP");
                this.finalizeIfStopped();
                break;
            default:
        }
    }

    /**
     * Handles file upload initialization message received by the handler.
     *
     * This stage acquires the db, storage client and other resources required through out
     * the life time of the Handler. This stage also starts a set of block upload async operations,
     * with the number of async operations equal to the configured blocksUploadConcurrency.
     */
    private void handleInit() {
        this.db = TransferDatabase.getInstance(this.appContext);
        this.blob = this.db.uploadDao().getBlob(uploadId);
        if (this.blob.interruptState == TransferInterruptState.PURGE) {
            this.transferHandlerListener.onError(new RuntimeException("Upload Operation with id '"
                + this.uploadId + "' is already CANCELLED and cannot be RESTARTED or RESUMED."));
            this.getLooper().quit();
        } else if (this.blob.state == BlobTransferState.COMPLETED) {
            this.transferHandlerListener.onTransferProgress(blob.contentSize, blob.contentSize);
            this.transferHandlerListener.onComplete();
            this.getLooper().quit();
        } else {
            this.blobClient = TransferClient.STORAGE_BLOB_CLIENTS.get(this.blob.storageBlobClientId);
            if (this.blobClient == null) {
                this.transferHandlerListener
                    .onError(new UnresolvedStorageBlobClientIdException(this.blob.storageBlobClientId));
                this.getLooper().quit();
            } else {
                this.content = new ReadableContent(appContext,
                    Uri.parse(this.blob.contentUri),
                    this.blob.useContentResolver);
                this.totalBytesUploaded = this.db.uploadDao().getUploadedBytesCount(this.uploadId);
                this.transferHandlerListener.onTransferProgress(blob.contentSize, totalBytesUploaded);
                List<BlockTransferState> skip = new ArrayList();
                skip.add(BlockTransferState.COMPLETED);
                this.blocksItr = new BlockUploadRecordsEnumerator(this.db, this.uploadId, skip);
                List<BlockUploadEntity> blocks = this.blocksItr.getNext(this.blocksUploadConcurrency);
                if (blocks.size() == 0) {
                    this.commitBlocks();
                } else {
                    this.stageBlocks(blocks);
                }
            }
        }
    }

    /**
     * Handles the block staging (upload) completion message received by the looper.
     * Such a completion message indicate that a single block is successfully uploaded.
     *
     * This stage notifies the progress to {@link TransferHandlerListener}. If there are more
     * blocks to be uploaded then it starts next block upload async operation, if there are no
     * more blocks to upload then it start a blocks commit async operation.
     *
     * @param message the message describing the block that completed staging
     */
    private void handleStagingCompleted(Message message) {
        this.finalizeIfStopped();
        String blockId = UploadHandlerMessage.getBlockIdFromMessage(message);
        BlockUploadEntity blockStaged = this.runningBlockUploads.remove(blockId);
        this.totalBytesUploaded += blockStaged.blockSize;
        this.transferHandlerListener.onTransferProgress(this.blob.contentSize, this.totalBytesUploaded);
        List<BlockUploadEntity> blocks = blocksItr.getNext(1);
        if (blocks.isEmpty()) {
            if (runningBlockUploads.isEmpty()) {
                this.commitBlocks();
            }
        } else {
            this.stageBlocks(blocks);
        }
    }

    /**
     * Handles the block staging (upload) failed message received by the looper.
     *
     * This stage cancel any running calls, notifies the failure to {@link TransferHandlerListener}
     * and terminates the handler.
     *
     * @param message the message describing the block that failed to stage
     */
    private void handleStagingFailed(Message message) {
        String blockId = UploadHandlerMessage.getBlockIdFromMessage(message);
        BlockUploadEntity failedBlock = this.runningBlockUploads.remove(blockId);
        this.transferHandlerListener.onError(failedBlock.getStagingError());
        this.getLooper().quit();
    }

    /**
     * Handles the blocks commit completion message received by the looper.
     *
     * This stage notifies the completion of file upload to {@link TransferHandlerListener}
     * and terminates the handler.
     */
    private void handleCommitCompleted() {
        this.transferHandlerListener.onTransferProgress(this.blob.contentSize, this.blob.contentSize);
        this.transferHandlerListener.onComplete();
        this.getLooper().quit();
    }

    /**
     * Handles the blocks commit failed message received by the looper.
     *
     * This stage notifies the failure to {@link TransferHandlerListener} and terminates
     * the handler.
     */
    private void handleCommitFailed() {
        this.transferHandlerListener.onError(this.blob.getCommitError());
        this.getLooper().quit();
    }

    /**
     * Check whether stop token is signalled, if so park the work and quit the looper.
     */
    private void finalizeIfStopped() {
        if (this.transferStopToken.isStopped()) {
            Log.v(TAG, "finalizeIfStopped(): Stop request received, finalizing");
            this.cancellationToken.cancel();
            TransferInterruptState interruptState = this.db.uploadDao().getTransferInterruptState(this.uploadId);
            Log.v(TAG, "finalizeIfStopped: Stop request reason (NONE == Stop requested by SYSTEM): " + interruptState);
            switch (interruptState) {
                case NONE:
                    this.transferHandlerListener.onSystemPaused();
                    break;
                case USER_PAUSED:
                    this.transferHandlerListener.onUserPaused();
                    break;
                case USER_CANCELLED:
                    this.db.uploadDao().updateUploadInterruptState(this.uploadId, TransferInterruptState.PURGE);
                    this.transferHandlerListener.onError(new TransferCancelledException(this.uploadId));
            }
            this.getLooper().quit();
        }
    }

    /**
     * Starts the block upload async operations.
     *
     * @param blocks the blocks to be staged (uploaded).
     */
    private void stageBlocks(List<BlockUploadEntity> blocks) {
        for (BlockUploadEntity block : blocks) {
            this.finalizeIfStopped();

            Log.v(TAG, "stageBlocks(): Uploading block:" + block.blockId + threadName());
            byte [] blockContent;
            try {
                blockContent = content.readBlock(block.blockOffset, block.blockSize);
            } catch (Throwable t) {
                Log.e(TAG,  "stageBlocks(): failure in reading content. Block id: " + block.blockId + ". Thread name: " + threadName(), t);
                db.uploadDao().updateBlockState(block.key, BlockTransferState.FAILED);
                block.setStagingError(t);
                Message nextMessage = UploadHandlerMessage
                    .createStagingFailedMessage(UploadHandler.this, block.blockId);
                nextMessage.sendToTarget();
                return;
            }

            this.blobClient.stageBlock(this.blob.containerName,
                this.blob.blobName,
                block.blockId,
                blockContent,
                null,
                null,
                this.blob.computeMd5,
                null,
                null,
                null,
                this.cancellationToken,
                new CallbackWithHeader<Void, BlockBlobStageBlockHeaders>() {
                    @Override
                    public void onSuccess(Void result, BlockBlobStageBlockHeaders header, Response response) {
                        Log.v(TAG, "stageBlocks(): Block uploaded:" + block.blockId + threadName());
                        db.uploadDao().updateBlockState(block.key, BlockTransferState.COMPLETED);
                        Message nextMessage = UploadHandlerMessage
                            .createStagingCompletedMessage(UploadHandler.this, block.blockId);
                        nextMessage.sendToTarget();
                    }

                    @Override
                    public void onFailure(Throwable throwable, Response response) {
                        Log.e(TAG,  "stageBlocks(): Block upload failed:" + block.blockId + threadName(), throwable);
                        db.uploadDao().updateBlockState(block.key, BlockTransferState.FAILED);
                        block.setStagingError(throwable);
                        Message nextMessage = UploadHandlerMessage
                            .createStagingFailedMessage(UploadHandler.this, block.blockId);
                        nextMessage.sendToTarget();
                    }
                });
            this.runningBlockUploads.put(block.blockId, block);
        }
    }

    /**
     * Starts the blocks commit async operation.
     */
    private void commitBlocks() {
        this.finalizeIfStopped();

        Log.v(TAG, "commitBlocks(): All blocks uploaded, committing them." + threadName());

        List<String> blockIds = this.db.uploadDao().getBlockIds(this.uploadId);

        this.blobClient.commitBlockList(blob.containerName,
            blob.blobName,
            blockIds,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            this.cancellationToken,
            new CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders>() {
                @Override
                public void onSuccess(BlockBlobItem result, BlockBlobCommitBlockListHeaders header, Response response) {
                    Log.v(TAG, "commitBlocks(): Blocks committed." + threadName());
                    db.uploadDao().updateBlobState(uploadId, BlobTransferState.COMPLETED);
                    Message nextMessage = UploadHandlerMessage
                        .createCommitCompletedMessage(UploadHandler.this);
                    nextMessage.sendToTarget();
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    Log.e(TAG,  "commitBlocks(): Blocks commit failed." + threadName(), throwable);
                    db.uploadDao().updateBlobState(uploadId, BlobTransferState.FAILED);
                    blob.setCommitError(throwable);
                    Message nextMessage = UploadHandlerMessage
                        .createCommitFailedMessage(UploadHandler.this);
                    nextMessage.sendToTarget();
                }
            });
    }

    // For Debugging, will be removed (TODO: anuchan)
    private static String threadName() {
        return " Thread:" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")";
    }
}
