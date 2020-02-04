package com.azure.android.storage.blob.upload;

public enum BlockUploadState {
    WAIT_TO_BEGIN,
    IN_PROGRESS,
    RETRY_IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED,
}
