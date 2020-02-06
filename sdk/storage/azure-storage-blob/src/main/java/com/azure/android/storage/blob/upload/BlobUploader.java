package com.azure.android.storage.blob.upload;

import android.util.Log;

import com.azure.android.core.http.Callback;
import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlockBlobItem;

import java.util.ArrayList;
import java.util.List;

public class BlobUploader {
    private final StorageBlobClient blobClient;
    private final int uploadMaxRetry;

    public BlobUploader(StorageBlobClient blobClient, int uploadMaxRetry) {
        this.blobClient = blobClient;
        this.uploadMaxRetry = uploadMaxRetry;
    }

    public void upload(BlobUploadRecord blobUploadRecord, Listener listener) {
        listener = listener == null ? noOpListener() : listener;
        for (BlockUploadRecord blockUploadRecord : blobUploadRecord.getBlockUploadRecords()) {
            uploadBlock(blobUploadRecord, blockUploadRecord, listener);
        }
    }

    private boolean uploadBlock(BlobUploadRecord blobUploadRecord,
                                BlockUploadRecord blockUploadRecord,
                                Listener listener) {
        if (blobUploadRecord.getState() == BlobUploadState.FAILED) {
            Log.v("uploadBlock",
                    "NOP: BlobUpload is in FAILED state, block staging won't be performed:" + blockUploadRecord.getBlockId());
            blockUploadRecord.setState(BlockUploadState.CANCELLED);
            return false;
        }

        if (blockUploadRecord.getState() == BlockUploadState.COMPLETED
                || blockUploadRecord.getState() == BlockUploadState.FAILED) {
            Log.e("uploadBlock", "NOP: BlockUpload is in FAILED|COMPLETED state:" + blockUploadRecord.getBlockId());
            return false;
        }

        blockUploadRecord.setState(BlockUploadState.IN_PROGRESS);
        this.blobClient.stageBlock(blobUploadRecord.getContainerName(),
                blobUploadRecord.getBlobName(),
                blockUploadRecord.getBlockId(),
                blockUploadRecord.getBlockContent(),
                null, new Callback<Void>() {
                    @Override
                    public void onResponse(Void response) {
                        Log.v("uploadBlock", "BlockUpload succeeded. id:" + blockUploadRecord.getBlockId());
                        blockUploadRecord.setState(BlockUploadState.COMPLETED);
                        postUploadBlock(blobUploadRecord, blockUploadRecord, listener);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (blockUploadRecord.getAndIncrementRetryCount() < uploadMaxRetry) {
                            Log.v("uploadBlock",  "BlockUpload failed, retrying.", t);
                            blockUploadRecord.setState(BlockUploadState.RETRY_IN_PROGRESS);
                            uploadBlock(blobUploadRecord, blockUploadRecord, listener);
                        } else {
                            Log.e("uploadBlock",  "BlockUpload failed.", t);
                            blockUploadRecord.setState(BlockUploadState.FAILED);
                            postUploadBlock(blobUploadRecord, blockUploadRecord, listener);
                        }
                    }
                });
        return true;
    }

    private synchronized void postUploadBlock(BlobUploadRecord blobUploadRecord,
                                              BlockUploadRecord blockUploadRecord,
                                              Listener listener) {
        Log.v("postUploadBlock", "From:" + blockUploadRecord.getBlockId());
        if (blobUploadRecord.getState() == BlobUploadState.FAILED) {
            Log.v("postUploadBlock", "NOP: BlobUpload is in FAILED state:" + blockUploadRecord.getBlockId());
            return;
        }
        if (blockUploadRecord.getState() == BlockUploadState.FAILED) {
            Log.v("postUploadBlock","BlockUpload is in FAILED state, marking BlobUpload as FAILED:" + blockUploadRecord.getBlockId());
            blobUploadRecord.setState(BlobUploadState.FAILED);
            listener.onError(blockUploadRecord.getUploadError());
            return;
        }
        if (blockUploadRecord.getState() == BlockUploadState.COMPLETED) {
            int uploadedBytes = blobUploadRecord.addToBytesUploaded(blockUploadRecord.getBlockSize());
            listener.onUploadProgress(blobUploadRecord.getFileSize(), uploadedBytes);
        }
        boolean anyStagingFailed = false;
        boolean allStagingSucceeded = true;
        for (BlockUploadRecord record : blobUploadRecord.getBlockUploadRecords()) {
            if (record.getState() == BlockUploadState.FAILED) {
                anyStagingFailed = true;
            }
            if (record.getState() != BlockUploadState.COMPLETED) {
                allStagingSucceeded = false;
            }
        }
        if (anyStagingFailed) {
            Log.v("postUploadBlock",
                    "At least one BlockUpload is in FAILED state, marking BlobUpload as FAILED:" + blockUploadRecord.getBlockId());
            blobUploadRecord.setState(BlobUploadState.FAILED);
            listener.onError(null); // TODO
        }
        if (allStagingSucceeded) {
            Log.v("postUploadBlock", "All BlockUpload is COMPLETED state, committing.");
            blobUploadRecord.setState(BlobUploadState.COMMIT_IN_PROGRESS);
            commitBlocks(blobUploadRecord, listener);
        }
    }

    private void commitBlocks(BlobUploadRecord blobUploadRecord, Listener listener) {
        List<String> base64BlockIds = new ArrayList<>();
        for (BlockUploadRecord record : blobUploadRecord.getBlockUploadRecords()) {
            base64BlockIds.add(record.getBlockId());
        }
        this.blobClient.commitBlockList(blobUploadRecord.getContainerName(),
                blobUploadRecord.getBlobName(),
                base64BlockIds,
                true, new Callback<BlockBlobItem>() {
                    @Override
                    public void onResponse(BlockBlobItem response) {
                        Log.v("commitBlocks", "BlockCommit succeeded.");
                        blobUploadRecord.setState(BlobUploadState.COMPLETED);
                        listener.onUploadProgress(blobUploadRecord.getFileSize(), blobUploadRecord.getFileSize());
                        listener.onCompleted();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (blobUploadRecord.getAndIncrementCommitRetryCount() < uploadMaxRetry) {
                            Log.e("commitBlocks",  "BlockCommit failed. Retrying", t);
                            blobUploadRecord.setState(BlobUploadState.COMMIT_RETRY_IN_PROGRESS);
                            commitBlocks(blobUploadRecord, listener);
                        } else {
                            Log.e("commitBlocks",  "BlockCommit failed", t);
                            blobUploadRecord.setState(BlobUploadState.FAILED);
                            listener.onError(t);
                        }
                    }
                });
    }

    public interface Listener {
        void onUploadProgress(int totalBytes, int bytesUploaded);
        void onError(Throwable t);
        void onCompleted();
    }

    private static Listener noOpListener() {
        return new Listener() {
            @Override
            public void onUploadProgress(int totalBytes, int bytesUploaded) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
