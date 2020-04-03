package com.azure.android.storage.blob.transfer;

import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

final class TransferConstants {
    /**
     * The default block upload parallelism.
     */
    static final int DEFAULT_BLOCKS_UPLOAD_CONCURRENCY = 3;

    /**
     * Identifies an entry in {@link WorkerParameters} input to {@link UploadWorker} that
     * holds blocksUploadConcurrency value.
     */
    static final String INPUT_BLOCKS_UPLOAD_CONCURRENCY_KEY = "ick";

    /**
     * Identifies an entry {@link Data} passed to {@link ListenableWorker#setProgressAsync(Data)}, holding the
     * total bytes to download.
     */
    static final String PROGRESS_TOTAL_BYTES = "TOTAL_BYTES";
    /**
     * Identifies an entry {@link Data} passed to {@link ListenableWorker#setProgressAsync(Data)}, holding the
     * total bytes downloaded so far.
     */
    static final String PROGRESS_BYTES_TRANSFERRED = "BYTES_TRANSFERRED";
}
