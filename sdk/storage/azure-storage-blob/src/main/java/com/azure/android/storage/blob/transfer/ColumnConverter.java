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
    public int fromBlobTransferState(BlobTransferState state) {
        return state.ordinal();
    }

    @TypeConverter
    public BlobTransferState toBlobTransferState(int ordinal) {
        return BlobTransferState.values()[ordinal];
    }

    @TypeConverter
    public int fromBlockTransferState(BlockTransferState state) {
        return state.ordinal();
    }

    @TypeConverter
    public BlockTransferState toBlockTransferState(int ordinal) {
        return BlockTransferState.values()[ordinal];
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
