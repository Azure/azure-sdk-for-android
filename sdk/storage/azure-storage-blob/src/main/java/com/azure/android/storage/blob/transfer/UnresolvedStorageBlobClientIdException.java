// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * The exception type to indicate that a transfer cannot be executed due to
 * a failure in resolving {@link com.azure.android.storage.blob.StorageBlobClient}
 * for the transfer.
 *
 * @see StorageBlobClientMap
 * @see UploadHandler
 * @see DownloadHandler
 */
final class UnresolvedStorageBlobClientIdException extends RuntimeException {
    private static final String MESSAGE_PREFIX = "UnresolvedStorageClientId:";

    /**
     * Creates {@link UnresolvedStorageBlobClientIdException}.
     *
     * @param storageBlobClientId the id for the storage blob client that could not be resolved
     */
    UnresolvedStorageBlobClientIdException(String storageBlobClientId) {
        super(MESSAGE_PREFIX + storageBlobClientId);
    }
}
