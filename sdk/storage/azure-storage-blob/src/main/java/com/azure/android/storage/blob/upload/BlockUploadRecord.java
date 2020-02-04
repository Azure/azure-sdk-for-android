package com.azure.android.storage.blob.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class BlockUploadRecord {
    private String filePath;
    private int fileOffset;
    private int blockSize;
    private String blockId;
    private volatile BlockUploadState state;
    private Throwable uploadError;
    private int retryCount = 0;

    private BlockUploadRecord(String blockId, String filePath, int fileOffset, int blockSize) {
        Objects.requireNonNull(blockId);
        Objects.requireNonNull(filePath);
        this.blockId = blockId;
        this.filePath = filePath;
        this.fileOffset = fileOffset;
        this.blockSize = blockSize;
    }

    public static BlockUploadRecord create(String blockId, String filePath, int fileOffset, int blockSize) {
        BlockUploadRecord record = new BlockUploadRecord(blockId, filePath, fileOffset, blockSize);
        record.state = BlockUploadState.WAIT_TO_BEGIN;
        return record;
    }

    public static BlockUploadRecord createFromDBCursor() {
        throw new RuntimeException("createFromDBCursor not implemented.");
    }

    public String getBlockId() {
        return this.blockId;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public BlockUploadState getState() {
        return this.state;
    }

    public void setState(BlockUploadState state) {
        this.state = state;
    }

    public void setErrorState(Throwable t) {
        this.setState(BlockUploadState.FAILED);
        this.uploadError = t;
    }

    public Throwable getUploadError() {
        return this.uploadError;
    }

    public int getAndIncrementRetryCount() {
        int r = this.retryCount;
        this.retryCount++;
        return r;
    }

    public byte[] getBlockContent() {
        File file = new File(this.filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            seek(fileInputStream, this.fileOffset);
            byte [] blockContent = new byte[this.blockSize];
            read(fileInputStream, blockContent);
            return blockContent;
        } catch (FileNotFoundException ffe) {
            throw new RuntimeException(ffe);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static void seek(FileInputStream stream, long n) throws IOException {
        int skipped = 0;
        while(skipped < n) {
            long m = stream.skip(n - skipped);
            if (m < 0) {
                throw new IOException("FileInputStream::seek returns negative value.");
            }
            if (m == 0) {
                if (stream.read() == -1) {
                    return;
                } else {
                    skipped++;
                }
            } else {
                skipped += m;
            }
        }
    }

    private static int read(FileInputStream stream, byte [] buffer) throws IOException {
        int bytesToRead = buffer.length;
        int bytesRead = 0;
        while (bytesRead < bytesToRead) {
            int m = stream.read(buffer, bytesRead, bytesToRead - bytesRead);
            if (m == -1) {
                break;
            }
            bytesRead += m;
        }
        return bytesRead;
    }
}
