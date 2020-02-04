package com.azure.android.storage.blob.upload;

public enum BlobUploadState {
    WAIT_TO_BEGIN,
    STAGING_IN_PROGRESS,
    COMMIT_IN_PROGRESS,
    COMMIT_RETRY_IN_PROGRESS,
    FAILED,
    COMPLETED
}
