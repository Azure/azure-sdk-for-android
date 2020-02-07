package com.azure.android.storage.blob.upload;

import com.azure.android.core.util.Base64Util;
import com.azure.android.storage.blob.implementation.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BlobUploadRecord {
    private final String uploadId;
    private final int fileSize;
    private final String containerName;
    private final String blobName;
    private final String contentType;
    private final List<BlockUploadRecord> blockUploadRecords;
    private volatile BlobUploadState state;
    private volatile int bytesUploaded;
    private int commitRetryCount = 0;

    private BlobUploadRecord(int fileSize,
                             String containerName,
                             String blobName,
                             String contentType,
                             List<BlockUploadRecord> blockUploadRecords) {
        this.uploadId = UUID.randomUUID().toString();
        this.fileSize = fileSize;
        this.containerName = containerName;
        this.blobName = blobName;
        this.contentType = contentType;
        this.blockUploadRecords = blockUploadRecords;
        this.bytesUploaded = 0;
    }

    public static BlobUploadRecord create(String containerName,
                                          String blobName,
                                          String contentType,
                                          File file) {

        BlobUploadRecord record = new BlobUploadRecord((int) file.length(),
                containerName,
                blobName,
                contentType,
                createBlockUploadRecords(file));
        record.state = BlobUploadState.WAIT_TO_BEGIN;
        return record;
    }

    public static BlobUploadRecord createFromDBCursor() {
        throw new RuntimeException("createFromDBCursor not implemented.");
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public String getContainerName() {
        return this.containerName;
    }

    public String getBlobName() {
        return this.blobName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public List<BlockUploadRecord> getBlockUploadRecords() {
        return blockUploadRecords;
    }

    public BlobUploadState getState() {
        return this.state;
    }

    public void setState(BlobUploadState state) {
        this.state = state;
    }

    public int getAndIncrementCommitRetryCount() {
        int r = this.commitRetryCount;
        this.commitRetryCount++;
        return r;
    }


    public int addToBytesUploaded(int count) {
        // Note: not an atomic given access to this in synchronized in BlobUploader
        bytesUploaded += count;
        return bytesUploaded;
    }

    private static List<BlockUploadRecord> createBlockUploadRecords(File file) {
        final int BLOCK_SIZE = 10 * Constants.MB;
        final String filePath = file.getAbsolutePath();
        final List<BlockUploadRecord> blockUploadRecords = new ArrayList<>();
        if (file.length() <= BLOCK_SIZE) {
            BlockUploadRecord blockUploadRecord = BlockUploadRecord.create(
                    Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8)),
                    filePath,
                    0,
                    (int) file.length());
            blockUploadRecords.add(blockUploadRecord);
        } else {
            long remainingLength = file.length();
            int fileOffset = 0;
            int blocksCount = (int) Math.ceil(remainingLength / (double) BLOCK_SIZE);
            for (int i = 0; i < blocksCount; i++) {
                final int currentBlockLength = (int) Math.min(BLOCK_SIZE, remainingLength);
                BlockUploadRecord blockUploadRecord = BlockUploadRecord.create(
                        Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8)),
                        filePath,
                        fileOffset,
                        currentBlockLength);
                blockUploadRecords.add(blockUploadRecord);
                fileOffset += currentBlockLength;
                remainingLength -= currentBlockLength;
            }
        }
        return blockUploadRecords;
    }
}
