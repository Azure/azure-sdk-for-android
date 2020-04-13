// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.s

package com.azure.android.storage.blob.transfer;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Information about a particular transfer identified by {@link TransferInfo#getId()}.
 *
 * Information include the current {@link State}, progress of the transfer.
 * The state can be retrieved by calling {@link TransferInfo#getState()}.
 * The progress can be retrieved by calling {@link TransferInfo#getProgress()}.
 *
 * Note that progress is only available for the state ({@link State#RECEIVED_PROGRESS},
 * getProgress() returns {@code null} for all other states.
 */
public final class TransferInfo {
    // the transfer id.
    private final long id;
    // the transfer state.
    private final @State int state;
    // the transfer progress.
    private final Progress progress;
    // the string describing transfer failure reason
    private final String errorMessage;

    /**
     * Create TransferInfo for a given state.
     *
     * @param id the transfer id
     * @param state the transfer state
     */
    private TransferInfo(long id, @State int state) {
        this.id = id;
        this.state = state;
        this.progress = null;
        this.errorMessage = null;
    }

    /**
     * Create TransferInfo for {@link State#RECEIVED_PROGRESS} state.
     *
     * @param id the transfer id
     * @param progress the transfer progress description
     */
    private TransferInfo(long id, @NonNull Progress progress) {
        this.id = id;
        this.state = State.RECEIVED_PROGRESS;
        this.progress = progress;
        this.errorMessage = null;
    }

    /**
     * Create TransferInfo for {@link State#FAILED} state.
     *
     * @param id the transfer id
     * @param errorMessage the string describing transfer failure reason
     */
    private TransferInfo(long id, String errorMessage) {
        this.id = id;
        this.state = State.FAILED;
        this.progress = null;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a {@link TransferInfo} indicating that transfer is accepted by system.
     *
     * @param transferId the transfer id
     * @return {@link TransferInfo}
     */
    static TransferInfo createStarted(long transferId) {
        return new TransferInfo(transferId, State.STARTED);
    }

    /**
     * Create a {@link TransferInfo} indicating that previously paused transfer
     * is now resumed.
     *
     * @param transferId the transfer id
     * @return {@link TransferInfo}
     */
    static TransferInfo createResumed(long transferId) {
        return new TransferInfo(transferId, State.RESUMED);
    }

    /**
     * Create a {@link TransferInfo} indicating that transfer made some progress.
     *
     * @param transferId the transfer id
     * @param progress the progress description
     * @return {@link TransferInfo}
     */
    static TransferInfo createProgress(long transferId, @NonNull Progress progress) {
        return new TransferInfo(transferId, progress);
    }

    /**
     * Create a {@link TransferInfo} indicating that transfer is now paused by the system.
     *
     * @param transferId the transfer id
     * @return {@link TransferInfo}
     */
    static TransferInfo createSystemPaused(long transferId) {
        return new TransferInfo(transferId, State.SYSTEM_PAUSED);
    }

    /**
     * Create a {@link TransferInfo} indicating the transfer is completed.
     *
     * @param transferId the transfer id
     * @return {@link TransferInfo}
     */
    static TransferInfo createCompleted(long transferId) {
        return new TransferInfo(transferId, State.COMPLETED);
    }

    /**
     * Create a {@link TransferInfo} indicating the transfer is failed.
     *
     * @param transferId the transfer id
     * @param errorMessage the string describing transfer failure reason
     * @return {@link TransferInfo}
     */
    static TransferInfo createFailed(long transferId, String errorMessage) {
        return new TransferInfo(transferId, errorMessage);
    }
    /**
     * Get the transfer id.
     *
     * @return transfer id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Get the transfer state.
     *
     * @return transfer state
     */
    public @State int getState() {
        return this.state;
    }

    /**
     * Get the current progress of the transfer. Note that progress is only available
     * for the state ({@link State#RECEIVED_PROGRESS}, for other states calling this
     * method returns {@code null}.
     *
     * @return the current transfer progress
     */
    public Progress getProgress() {
        return this.progress;
    }

    /**
     * Get the error message. Note that error message is only available
     * for the state ({@link State#FAILED}, for other states calling this
     * method returns {@code null}.
     *
     * @return the string describing transfer failure reason
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * The current lifecycle state of the transfer.
     */
    @IntDef({
        State.STARTED,
        State.RECEIVED_PROGRESS,
        State.SYSTEM_PAUSED,
        State.RESUMED,
        State.COMPLETED,
        State.FAILED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        /**
         * Used to indicate that the transfer request is accepted by system.
         */
        int STARTED = 1;
        /**
         * Used to indicate that a part of transfer is completed and the current
         * progress can be retrieved from {@link TransferInfo#getProgress()}.
         */
        int RECEIVED_PROGRESS = 2;
        /**
         * Used to indicate that the transfer is paused by the system because transfer
         * constraints are not met or resources are not available. System will
         * resume the transfer once constraints, resource requirements are satisfied.
         */
        int SYSTEM_PAUSED = 3;
        /**
         * Used to indicate that the transfer that was paused is resumed.
         */
        int RESUMED = 4;
        /**
         * Used to indicate that the transfer has been completed.
         */
        int COMPLETED = 5;
        /**
         * Used to indicate that the transfer has been failed.
         */
        int FAILED = 6;
    }

    /**
     * Type to describe progress of a transfer.
     *
     * When {@link TransferInfo#getState()} returns {@link State#RECEIVED_PROGRESS},
     * calling {@link TransferInfo#getProgress()} will return an instance
     * of this type describing current progress.
     */
    public final static class Progress {
        private final long totalBytes;
        private final long bytesTransferred;

        /**
         *
         * Package private Ctr.
         *
         * Create Progress.
         *
         * @param totalBytes the total bytes to transfer
         * @param bytesTransferred the bytes transferred so far
         */
        Progress(long totalBytes, long bytesTransferred) {
            this.totalBytes = totalBytes;
            this.bytesTransferred = bytesTransferred;
        }

        /**
         * Get the total bytes to be transferred.
         *
         * @return the total bytes to be transferred
         */
        public long getTotalBytes() {
            return this.totalBytes;
        }

        /**
         * Get the bytes transferred so far.
         *
         * @return the bytes transferred so far
         */
        public long getBytesTransferred() {
            return this.bytesTransferred;
        }

        @Override
        public String toString() {
            return "(" + this.totalBytes + ", " + this.bytesTransferred + ")";
        }
    }
}
