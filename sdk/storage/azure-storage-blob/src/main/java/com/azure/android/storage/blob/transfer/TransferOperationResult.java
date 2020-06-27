// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Package private.
 *
 * The type used by the operations in {@link TransferClient} (e.g. upload, download, resume) to
 * communicate initial result of the async operation from background {@link SerialExecutor}
 * to {@link TransferIdInfoLiveData}.
 *
 * The operation result includes the operation type, the actual transfer id of a transfer or an error
 * indicating the failure detected by TransferClient. Note that this type is used only to channel the
 * error that TransferClient methods encountered or could detect, this type is not used to channel any
 * error happened in the background worker executing wire transfer.
 */
final class TransferOperationResult {
    // the transfer operation type (upload_download, resume)
    private final @Operation int operation;
    // the transfer id.
    private final String id;
    // the error happened in TransferClient method when processing a transfer request.
    private final Throwable error;

    /**
     * Create a {@link TransferOperationResult} object indicating a valid transfer id.
     *
     * @param operation the transfer operation type - upload_download, resume
     * @param id the transfer id of a transfer
     * @return {@link TransferOperationResult} composing a valid transfer id
     */
    static TransferOperationResult id(@NonNull @Operation int operation, String id) {
        return new TransferOperationResult(operation, id, null);
    }

    /**
     * Create a {@link TransferOperationResult} object indicating an error when performing
     * a transfer operation otherwise would have yielded a transfer id.
     *
     * @param operation the transfer operation type - upload_download, resume
     * @param error the error happened in a TransferClient operation
     * @return {@link TransferOperationResult} composing an error
     */
    static TransferOperationResult error(@NonNull @Operation int operation, @NonNull Throwable error) {
        return new TransferOperationResult(operation, null, error);
    }

    /**
     * Create a TransferOperationResult object indicating that transfer operation cannot be performed
     * because there is no StorageBlobClient with the provided id.
     *
     * @param operation the transfer operation type - upload_download, resume
     * @param storageClientId the id of the storage blob client that could not be resolved
     * @return {@link TransferOperationResult} composing the error
     */
    static TransferOperationResult unresolvedStorageClientIdError(@NonNull @Operation int operation,
                                                                  String storageClientId) {
        return error(operation, new UnresolvedStorageBlobClientIdException(storageClientId));
    }

    /**
     * Create a {@link TransferOperationResult} object indicating that resume operation cannot be performed
     * because provided id is not identifying a transfer.
     *
     * @param id the non-existing transfer id
     * @return {@link TransferOperationResult} composing the error
     */
    static TransferOperationResult notFoundError(String id) {
        return error(Operation.RESUME, id, new TransferNotFoundError(id));
    }

    /**
     * Create a {@link TransferOperationResult} object indicating that a resume operation cannot be performed because
     * the transfer with given id is is already Completed.
     *
     * @param id the transfer id
     * @return {@link TransferOperationResult} composing the error
     */
    static TransferOperationResult alreadyInCompletedStateError(String id) {
        return error(Operation.RESUME, id,
            new TransferInTerminatedStateError(id, TransferInTerminatedStateError.State.COMPLETED));
    }

    /**
     * Create a {@link TransferOperationResult} object indicating that the resume operation cannot be performed because
     * transfer with given id is is already Failed.
     *
     * @param id the transfer id
     * @return {@link TransferOperationResult} composing the error
     */
    static TransferOperationResult alreadyInFailedStateError(String id) {
        return error(Operation.RESUME, id,
            new TransferInTerminatedStateError(id, TransferInTerminatedStateError.State.FAILED));
    }

    /**
     * Create a {@link TransferOperationResult} object indicating that the resume operation cannot be performed because
     * transfer with given id is is already Cancelled.
     *
     * @param id the transfer id
     * @return {@link TransferOperationResult} composing the error
     */
    static TransferOperationResult alreadyInCancelledStateError(String id) {
        return error(Operation.RESUME, id,
            new TransferInTerminatedStateError(id, TransferInTerminatedStateError.State.CANCELED));
    }

    /**
     * Get the transfer operation type.
     *
     * @return the operation type
     */
    @Operation int getOperation() {
        return this.operation;
    }

    /**
     * Get the transfer id.
     *
     * @return the transfer id
     */
    String getId() {
        return this.id;
    }

    /**
     * Check whether this id indicate an error.
     *
     * @return true for error, false otherwise
     */
    boolean isError() {
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
     * Check whether this id indicate that some operation couldn't performed
     * because the provided transfer id was not identifying a transfer.
     *
     * @return true for not-found error, false otherwise
     */
    boolean isNotFoundError() {
        return this.error instanceof TransferNotFoundError;
    }

    /**
     * Check whether this id indicate that some operation couldn't performed on a
     * transfer because the transfer was already in terminal state.
     *
     * @return true for error indicating terminal transfer state, false otherwise
     */
    boolean isTerminatedError() {
        return this.error instanceof TransferInTerminatedStateError;
    }

    /**
     * Create a {@link TransferOperationResult} object indicating an error when performing a transfer
     * operation identified by the given id.
     *
     * @param operation the transfer operation type - upload_download, resume
     * @param id the id
     * @param error the error happened in a TransferClient operation
     * @return {@link TransferOperationResult} composing an error
     */
    private static TransferOperationResult error(@NonNull @Operation int operation, String id, @NonNull Throwable error) {
        return new TransferOperationResult(operation, id, error);
    }

    /**
     * Create a {@link TransferOperationResult}.
     *
     * @param operation the transfer operation type - upload_download, resume
     * @param id the transfer id of a transfer
     * @param error the error happened during book keeping
     */
    private TransferOperationResult(@NonNull @Operation int operation, String id, Throwable error) {
        this.operation = operation;
        this.id = id;
        this.error = error;
    }

    @IntDef({
        Operation.UPLOAD_DOWNLOAD,
        Operation.RESUME,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Operation {
        int UPLOAD_DOWNLOAD = 1;
        int RESUME = 2;
    }

    /**
     * An internal exception indicating that a provided id is not identifying a transfer.
     */
    static class TransferNotFoundError extends Throwable {
        private final String id;

        /**
         * Creates TransferNotFoundError.
         *
         * @param id the id that failed to resolve to a valid transfer
         */
        private TransferNotFoundError(String id) {
            super("Transfer with id '" + id + "' is not found.");
            this.id = id;
        }

        /**
         * @return the id
         */
        String getId() {
            return this.id;
        }
    }

    /**
     * An internal exception indicating that a transfer cannot be performed because the transfer
     * is already in one of the terminal state.
     */
    static class TransferInTerminatedStateError extends Throwable {
        // the transfer id.
        private final String transferId;
        // the terminal state the transfer is in.
        private final @State int state;

        private TransferInTerminatedStateError(String transferId, @NonNull @State int state) {
            super("Transfer with id '" + transferId + "' is already in terminated stage.");
            this.transferId = transferId;
            this.state = state;
        }

        String getTransferId() {
            return this.transferId;
        }

        boolean isCompleted() {
            return this.state == State.COMPLETED;
        }

        boolean isFailed() {
            return this.state == State.FAILED;
        }

        boolean isCancelled() {
            return this.state == State.CANCELED;
        }

        @IntDef({
            State.COMPLETED,
            State.FAILED,
            State.CANCELED
        })
        @Retention(RetentionPolicy.SOURCE)
        private @interface State {
            int COMPLETED = 1;
            int FAILED = 2;
            int CANCELED = 3;
        }
    }
}
