// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.room.TypeConverter;

/**
 * Package private.
 *
 * Converter method used by Room library to read from and write to enums in local store.
 */
final class ColumnConverter {
    @TypeConverter
    public int fromBlobUploadState(BlobUploadState state) {
        return state.ordinal();
    }

    @TypeConverter
    public BlobUploadState toBlobUploadState(int ordinal) {
        return BlobUploadState.values()[ordinal];
    }

    @TypeConverter
    public int fromBlobDownloadState(BlobDownloadState state) {
        return state.ordinal();
    }

    @TypeConverter
    public BlobDownloadState toBlobDownloadState(int ordinal) {
        return BlobDownloadState.values()[ordinal];
    }

    @TypeConverter
    public int fromBlockUploadState(BlockUploadState state) {
        return state.ordinal();
    }

    @TypeConverter
    public BlockUploadState toBlockUploadState(int ordinal) {
        return BlockUploadState.values()[ordinal];
    }

    @TypeConverter
    public int fromTransferInterruptState(TransferInterruptState state) {
        return state.ordinal();
    }

    @TypeConverter
    public TransferInterruptState toTransferInterruptState(int ordinal) {
        return TransferInterruptState.values()[ordinal];
    }
}
