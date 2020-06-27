// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import java.util.Collections;
import java.util.List;

/**
 * Package private.
 *
 * Type to enumerate collection of {@link BlockUploadEntity} describing the blocks of a file to upload.
 */
final class BlockUploadRecordsEnumerator {
    private List<BlockUploadEntity> blocks;
    private int size;
    private int cursor;
    private final TransferDatabase db;
    private final String blobUploadId;
    private final List<BlockTransferState> skipStates;

    /**
     * Create {@link BlockUploadRecordsEnumerator} to enumerate the set of {@link BlockUploadEntity}
     * for the given upload id.
     *
     * @param db the local store to read the entities from
     * @param uploadId the upload id
     * @param skipStates the state of the entities to be skipped from enumeration
     */
    BlockUploadRecordsEnumerator(TransferDatabase db, String uploadId, List<BlockTransferState> skipStates) {
        this.cursor = 0;
        this.db = db;
        this.blobUploadId = uploadId;
        this.skipStates = skipStates;
    }

    /**
     * Retrieve {@code count} entities.
     *
     * @param count the number of {@link BlockUploadEntity} to retrieve
     * @return the retrieved entities or empty list if there are no more entities
     */
    List<BlockUploadEntity> getNext(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("requestedCount cannot be zero or negative, received:" + count);
        }
        if (this.blocks == null) {
            initBlocks();
        }
        if (this.cursor == this.size) {
            return Collections.emptyList();
        } else {
            int remainingCount = this.size - this.cursor;
            int fromIndex = this.cursor;
            int toIndex = remainingCount >= count
                ? this.cursor + remainingCount
                : this.cursor + remainingCount;
            this.cursor = toIndex;
            return this.blocks.subList(fromIndex, toIndex);
        }
    }

    /**
     * Query all the block upload metadata of a file upload from the local store.
     */
    private void initBlocks() {
        if (skipStates == null || skipStates.size() == 0) {
            this.blocks = this.db.uploadDao().getBlocks(this.blobUploadId);
        } else  {
            this.blocks = this.db.uploadDao().getBlocks(this.blobUploadId, this.skipStates);
        }
        this.size = this.blocks.size();
    }
}
