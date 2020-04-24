// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.Embedded;

/**
 * A type that composes blob and blocks metadata for a file download.
 *
 * @see BlobDownloadEntity
 */
final class DownloadRecord {
    /**
     * The blob download metadata.
     */
    @Embedded
    public BlobDownloadEntity blob;
}
