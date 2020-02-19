// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * A type that composes blob and blocks metadata for a file upload.
 *
 * @see BlobUploadEntity
 * @see BlockUploadEntity
 */
final class UploadRecord {
    /**
     * The blob upload metadata.
     */
    @Embedded
    public BlobUploadEntity blob;
    /**
     * The blocks upload metadata describing the blocks belonging to the blob.
     */
    @Relation(
        parentColumn = "key",
        entityColumn = "blob_key"
    )
    public List<BlockUploadEntity> blocks;
}
