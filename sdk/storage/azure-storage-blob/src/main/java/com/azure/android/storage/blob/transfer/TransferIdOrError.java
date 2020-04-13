// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.annotation.NonNull;

/**
 * Package private.
 *
 * The type used by the methods in {@link TransferClient} (e.g. upload, download, resume) to
 * communicate data from background {@link SerialExecutor} to {@link TransferIdInfoLiveData}.
 *
 * The data includes the operation type, the actual transfer id of a transfer or an error indicating
 * the failure detected by TransferClient. Note that this type is used only to channel the error that
 * TransferClient methods encountered or could detect, this type is not used to channel the real time
 * error happened in the background worker.
 */
public final class TransferIdOrError {
    // the transfer id.
    private final long id;
    // the error happened in TransferClient method when processing a transfer request.
    private final Throwable error;

    /**
     * Create a TransferIdOrError object indicating a valid transfer id.
     *
     * @param id the transfer id of a transfer
     * @return TransferIdOrError composing a valid transfer id
     */
    public static TransferIdOrError id(long id) {
        return new TransferIdOrError(id, null);
    }

    /**
     * Create a TransferIdOrError object indicating an error when performing
     * a transfer operation otherwise would have yielded a transfer id.
     *
     * @param error the error happened in a TransferClient operation
     * @return TransferIdOrError composing an error
     */
    static TransferIdOrError error(@NonNull Throwable error) {
        return new TransferIdOrError(-1, error);
    }

    /**
     * Get the transfer id.
     *
     * @return the transfer id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Check whether this id indicate an error.
     *
     * @return true for error, false otherwise
     */
    public boolean isError() {
        return this.error != null;
    }

    /**
     * Get the error object.
     *
     * @param <T> the error type
     * @return the error object
     */
    @SuppressWarnings({"unchecked"})
    <T> T getError() {
        if (isError()) {
            return (T) this.error;
        }
        return null;
    }

    /**
     * Get the error message describing book keeping error.
     *
     * @return the error message, null if there is no error
     */
    String getErrorMessage() {
        if (isError()) {
            return this.error.getMessage();
        }
        return null;
    }

    /**
     * Create a TransferIdOrError object indicating an error when performing a transfer
     * operation identified by the given id.
     *
     * @param id the id
     * @param error the error happened in a TransferClient operation
     * @return TransferIdOrError composing an error
     */
    private static TransferIdOrError error(long id, @NonNull Throwable error) {
        return new TransferIdOrError(id, error);
    }

    /**
     * Create a TransferIdOrError.
     *
     * @param id the transfer id of a transfer
     * @param error the error happened during book keeping
     */
    private TransferIdOrError(long id, Throwable error) {
        this.id = id;
        this.error = error;
    }
}
