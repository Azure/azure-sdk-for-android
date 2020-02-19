// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.azure.android.core.util.Base64Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static androidx.room.ForeignKey.CASCADE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Package private.
 *
 * Represents metadata for a block upload.
 *
 * There is one-to-many relationship between a {@link BlobUploadEntity} and a set
 * of {@link BlockUploadEntity}. Each {@link BlockUploadEntity} in the set
 * describes a chunk of the file referenced by the {@link BlobUploadEntity}
 *
 * The Data Access Object type {@link UploadDao} exposes DB store and read methods on this model.
 *
 * @see TransferDatabase
 */
@Entity(tableName = "blockuploads",
    foreignKeys = @ForeignKey(entity = BlobUploadEntity.class,
        parentColumns = "key",
        childColumns = "blob_key",
        onDelete = CASCADE),
    indices = {@Index("blob_key")})
final class BlockUploadEntity {
    /**
     * A unique key for the block upload metadata.
     *
     * This key identifies the metadata in the local store, which is different
     * from {@link BlockUploadEntity#blockId}. Block id is used by storage service
     * is uniquely identify the block.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "key")
    public Long key;
    /**
     * The key of {@link BlobUploadEntity} that this {@link BlockUploadEntity} belongs to.
     */
    @ColumnInfo(name = "blob_key")
    public long blobKey;
    /**
     * The absolute path to the file that the block is a part of.
     */
    @ColumnInfo(name = "file_path")
    public String filePath;
    /**
     * The offset in the file from which block contents starts.
     */
    @ColumnInfo(name = "file_offset")
    public int fileOffset;
    /**
     * The block size in bytes.
     */
    @ColumnInfo(name = "block_size")
    public int blockSize;
    /**
     * The base64 block id used by the storage service to uniquely identify the block.
     */
    @ColumnInfo(name = "block_id")
    public String blockId;
    /**
     * The current state of the block upload operation.
     */
    @ColumnInfo(name = "block_upload_state")
    @TypeConverters(ColumnConverter.class)
    public volatile BlockUploadState state;
    /**
     * holds the exception indicating the reason for block staging (upload) failure.
     *
     * This is not persisted
     */
    @Ignore
    private Throwable stagingError;

    /**
     * Creates BlockUploadEntity, this constructor is used by Room library
     * when re-hydrating metadata from local store.
     */
    public BlockUploadEntity() {}

    /**
     * Create a new BlockUploadEntity to persist in local store.
     *
     * @param blockId the base64 block id
     * @param filePath the absolute path to the file that the block is a part of
     * @param fileOffset the offset in the file from which block contents starts
     * @param blockSize the block size in bytes
     */
    private BlockUploadEntity(String blockId, String filePath, int fileOffset, int blockSize) {
        Objects.requireNonNull(blockId);
        Objects.requireNonNull(filePath);
        this.blockId = blockId;
        this.filePath = filePath;
        this.fileOffset = fileOffset;
        this.blockSize = blockSize;
        this.state = BlockUploadState.WAIT_TO_BEGIN;
    }

    /**
     * Set the {@link BlobUploadEntity#key} for this block. This is the foreign key referring
     * the BlobUploadEntity this block is a part of.
     *
     * @param blobKey the block key (aka uploadId)
     */
    void setBlobKey(long blobKey) {
        this.blobKey = blobKey;
    }

    /**
     * Set the block staging failure error.
     *
     * @param t the error
     */
    void setStagingError(Throwable t) {
        this.stagingError = t;
    }

    /**
     * Get the block staging (upload) failure error.
     *
     * @return the staging failure error or null if there is no error
     */
    Throwable getStagingError() {
        return this.stagingError;
    }

    /**
     * Factory method to create a collection of {@link BlockUploadEntity} for a file.
     *
     * @param file the file
     * @param blockSize block size in bytes
     * @return collection of {@link BlockUploadEntity} describing each block of the file
     */
    static List<BlockUploadEntity> createEntitiesForFile(File file, long blockSize) {
        final String filePath = file.getAbsolutePath();
        final List<BlockUploadEntity> blockUploadEntities = new ArrayList<>();
        if (file.length() <= blockSize) {
            final String blockId = Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
            BlockUploadEntity blockUploadEntity = new BlockUploadEntity(
                blockId,
                filePath,
                0,
                (int) file.length());
            blockUploadEntities.add(blockUploadEntity);
        } else {
            long remainingLength = file.length();
            int fileOffset = 0;
            int blocksCount = (int) Math.ceil(remainingLength / (double) blockSize);
            for (int i = 0; i < blocksCount; i++) {
                final String blockId = Base64Util.encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
                final int currentBlockLength = (int) Math.min(blockSize, remainingLength);
                BlockUploadEntity blockUploadEntity = new BlockUploadEntity(
                    blockId,
                    filePath,
                    fileOffset,
                    currentBlockLength);
                blockUploadEntities.add(blockUploadEntity);
                fileOffset += currentBlockLength;
                remainingLength -= currentBlockLength;
            }
        }
        return blockUploadEntities;
    }

    /**
     * Get the block content from the file in the local file system.
     *
     * @return the byte array holding block content
     */
    byte[] getBlockContent() {
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" key:" + this.key);
        builder.append(" blobKey:" + this.blobKey);
        builder.append(" filePath:" + this.filePath);
        builder.append(" fileOffset:" + this.fileOffset);
        builder.append(" blockSize:" + this.blockSize);
        builder.append(" state:" + this.state);
        if (this.stagingError != null) {
            builder.append(" stagingError:" + this.stagingError.getMessage());
        }
        return builder.toString();
    }

    /**
     * Seek the stream read cursor to the given position.
     *
     * @param stream the stream
     * @param seekTo the seek position
     * @throws IOException if seek fails
     */
    private static void seek(FileInputStream stream, long seekTo) throws IOException {
        int skipped = 0;
        while(skipped < seekTo) {
            long m = stream.skip(seekTo - skipped);
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

    /**
     * Read the stream content into a buffer starting from stream's read cursor position.
     *
     * @param stream the file stream
     * @param buffer the output buffer
     * @return the number of bytes read
     * @throws IOException if read fails
     */
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
