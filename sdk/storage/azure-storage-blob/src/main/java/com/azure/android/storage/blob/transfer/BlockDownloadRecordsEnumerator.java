// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import java.util.Collections;
import java.util.List;

/**
 * Package private.
 *
 * Type to enumerate collection of {@link BlockDownloadEntity} describing the blocks of a file to download.
 */
final class BlockDownloadRecordsEnumerator {
    private final TransferDatabase db;
    private final long downloadId;
    private final List<BlockTransferState> skipStates;
    private List<BlockDownloadEntity> blocks;
    private int cursor;
    private int size;

    /**
     * Create {@link BlockDownloadRecordsEnumerator} to enumerate the set of {@link BlockDownloadEntity} for the
     * given download ID.
     *
     * @param db The local store to read the entities from.
     * @param downloadId The download ID.
     * @param skipStates The state of the entities to be skipped from enumeration.
     */
    BlockDownloadRecordsEnumerator(TransferDatabase db, long downloadId, List<BlockTransferState> skipStates) {
        cursor = 0;
        this.db = db;
        this.downloadId = downloadId;
        this.skipStates = skipStates;
    }

    /**
     * Retrieve {@code count} entities.
     *
     * @param count The number of {@link BlockDownloadEntity} to retrieve.
     * @return The retrieved entities or empty list if there are no more entities.
     */
    List<BlockDownloadEntity> getNext(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("RequestedCount cannot be zero or negative, received: " + count);
        }

        if (blocks == null) {
            initBlocks();
        }

        if (cursor == size) {
            return Collections.emptyList();
        } else {
            int remainingCount = size - cursor;
            int fromIndex = cursor;
            int toIndex = remainingCount >= count
                ? cursor + remainingCount
                : cursor + remainingCount;
            cursor = toIndex;
            return blocks.subList(fromIndex, toIndex);
        }
    }

    /**
     * Query all the block download metadata of a file download from the local store.
     */
    private void initBlocks() {
        if (skipStates == null || skipStates.size() == 0) {
            blocks = db.downloadDao().getBlocks(downloadId);
        } else  {
            blocks = db.downloadDao().getBlocks(downloadId, skipStates);
        }

        size = blocks.size();
    }
}
