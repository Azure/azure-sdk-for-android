package com.azure.android.storage.blob.transfer;

import androidx.work.Data;
import androidx.work.ListenableWorker;

final class TransferConstants {
    /**
     * Identifies an entry {@link Data} passed to {@link ListenableWorker#setProgressAsync(Data)}, holding the
     * total bytes to download.
     */
    static final String PROGRESS_TOTAL_BYTES = "TOTAL_BYTES";
    /**
     * Identifies an entry {@link Data} passed to {@link ListenableWorker#setProgressAsync(Data)}, holding the
     * total bytes downloaded so far.
     */
    static final String PROGRESS_BYTES_TRANSFERRED = "BYTES_DOWNLOADED";
}
