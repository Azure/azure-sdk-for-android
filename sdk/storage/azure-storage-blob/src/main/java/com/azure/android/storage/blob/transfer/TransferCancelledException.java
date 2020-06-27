// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

/**
 * Exception indicating that user cancelled the transfer operation.
 */
final class TransferCancelledException extends Exception {
    private final String transferId;

    /**
     * Creates TransferCancelledException.
     *
     * @param transferId The ID of the cancelled transfer.
     */
    TransferCancelledException(String transferId) {
        super();
        this.transferId = transferId;
    }

    /**
     * Get the ID of the cancelled transfer.
     *
     * @return The transfer ID.
     */
    public String getTransferId() {
        return this.transferId;
    }
}
