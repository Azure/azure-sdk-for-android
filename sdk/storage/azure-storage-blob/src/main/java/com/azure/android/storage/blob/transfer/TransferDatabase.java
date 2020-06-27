// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Ignore;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Package private.
 *
 * Defines the local store for transfer (upload, download) metadata.
 *
 * @see BlobUploadEntity
 * @see BlobDownloadEntity
 * @see BlockUploadEntity
 */
@Database(entities = {BlobUploadEntity.class, BlockUploadEntity.class, BlobDownloadEntity.class,
    BlockDownloadEntity.class}, version = 1)
@TypeConverters(ColumnConverter.class)
abstract class TransferDatabase extends RoomDatabase {
    /**
     * A singleton instance of {@link TransferDatabase}.
     */
    @Ignore
    private static TransferDatabase INSTANCE;
    // An object to synchronize the creation of the singleton TransferDatabase.
    @Ignore
    private static final Object INIT_LOCK = new Object();

    /**
     * Get the Data Access Object that exposes operations to store and retrieve upload
     * metadata.
     *
     * @return Data Access Object for upload
     */
    public abstract UploadDao uploadDao();

    /**
     * Get the Data Access Object that exposes operations to store and retrieve download metadata.
     *
     * @return Data Access Object for download.
     */
    public abstract DownloadDao downloadDao();

    /**
     * Get a singleton instance of {@link TransferDatabase}.
     *
     * @param context the context
     * @return a shared {@link TransferDatabase} instance
     */
    @Ignore
    static TransferDatabase getInstance(Context context) {
        synchronized (INIT_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context,
                    TransferDatabase.class, "transfersDB").build();
            }
            return INSTANCE;
        }
    }
}
