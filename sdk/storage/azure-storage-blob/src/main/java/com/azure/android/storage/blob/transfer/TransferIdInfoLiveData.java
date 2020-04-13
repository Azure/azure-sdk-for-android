// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

/**
 * Package private.
 *
 * A type to create the LiveData pair, a {@link TransferIdOrError} LiveData and associated {@link TransferInfo}
 * LiveData. Each transfer will have such a unique LiveData pair.
 *
 * When transfer id is set to TransferIdOrError LiveData then TransferInfo LiveData streams {@link TransferInfo}
 * events describing current state of the transfer identified by the transfer id. Internally the source of the
 * TransferInfo LiveData queries {@link WorkManager} for a LiveData that streams {@link WorkInfo} and transform
 * {@link WorkInfo} events to {@link TransferInfo} events.
 *
 * Same transfer id can be set multiple times in the TransferIdOrError LiveData. Each such set results in querying
 * {@link WorkManager} as described above. There can be active Observes for an original transfer when it is paused
 * and later resumed by a different Worker. When possible we share the same LiveData pair between original transfer
 * and it's resume. In such cases, the ability to set transfer id on the TransferIdOrError LiveData multiple times
 * enables to transparently switch the Worker and keep any existing Observers of the original transfer to continue
 * to receive the {@link TransferInfo} events.
 *
 * @see TransferIdInfoLiveDataCache (for LiveData pair sharing)
 */
final class TransferIdInfoLiveData {
    private static final String TAG = TransferIdInfoLiveData.class.getSimpleName();
    // the input LiveData that receives the transfer id or error.
    private final MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData = new MutableLiveData<>();
    // the output TransferInfo LiveData.
    private final MediatorLiveData<TransferInfo> transferInfoLiveData = new MediatorLiveData<>();
    // the the object that TransferClient methods update any state that it want TransferInfo LiveData source
    // for a transfer to know.
    private final TransferFlags transferFlags = new TransferFlags();
    // the recent TransferIdOrError object emitted by the transferIdOrErrorLiveData object.
    private TransferIdOrError inputTransferIdOrError;
    // hold the state of the last WorkInfo received from Transfer Worker.
    private WorkInfo.State lastWorkInfoState;
    // flag to track whether current event about to emit from transferInfoLiveData is the first event.
    private boolean isFirstEvent = true;
    // flag to track whether the last emitted event from transferInfoLiveData was a pause event.
    private boolean wasPaused;

    private TransferIdInfoLiveData() {}

    @MainThread
    static TransferIdInfoLiveData.Result create(Context context) {
        TransferIdInfoLiveData transferIdInfoLiveData = new TransferIdInfoLiveData();
        return transferIdInfoLiveData.init(context);
    }

    private TransferIdInfoLiveData.Result init(@NonNull Context context) {
        // 1. Register mapping of transferId to LiveData<WorkInfo>
        LiveData<WorkInfo> workInfoLiveData = this.mapInputTransferIdToWorkInfoLiveData(context);
        // 2. Register mapping of LiveData<WorkInfo> to LiveData<TransferInfo>
        this.transferInfoLiveData.addSource(workInfoLiveData, workInfo -> {
            if (this.inputTransferIdOrError.isError()) {
                mapErrorFromTransferClient();
                return;
            }
            if (workInfo == null) {
                Log.v(TAG, "Skipping Null 'WorkInfo' from WorkManager.");
                return;
            }

            final WorkInfo.State lastWorkInfoState = getLastWorkInfoState();
            if (lastWorkInfoState != null) {
                if (lastWorkInfoState == WorkInfo.State.SUCCEEDED
                    || lastWorkInfoState == WorkInfo.State.FAILED
                    || (lastWorkInfoState == WorkInfo.State.CANCELLED && !this.wasPaused())) {
                    Log.e(TAG, "Received an unexpected 'WorkInfo' from WorkManager after terminal state:"
                        + workInfo.toString());
                    return;
                }
            }

            WorkInfo.State currentWorkInfoState = workInfo.getState();
            if (currentWorkInfoState == null) {
                Log.v(TAG, "Skipping the 'WorkInfo' from WorkManager with Null state.");
                return;
            }
            final long transferId = this.inputTransferIdOrError.getId();
            this.setLastWorkInfoState(currentWorkInfoState);
            if (this.isFirstEvent()) {
                this.setDoneFirstEvent();
                this.setWasPaused(false);
                this.transferInfoLiveData.setValue(TransferInfo.createStarted(transferId));
                if (currentWorkInfoState == WorkInfo.State.ENQUEUED) {
                    // the worker state can be WorkInfo.State.ENQUEUED in two cases -
                    // 1. When work is scheduled for the first time.
                    // 2. When work is paused by the system (e.g. no network).
                    //
                    // The #1 is mapped to TransferInfo.State.STARTED
                    // and #2 is mapped to TransferInfo.State.SYSTEM_PAUSED
                    //
                    // The outer block handled #1 by sending TransferInfo.State.STARTED
                    // hence returning.
                    return;
                }
            }
            if (currentWorkInfoState == WorkInfo.State.RUNNING) {
                if (this.wasPaused()) {
                    this.transferInfoLiveData.setValue(TransferInfo.createResumed(transferId));
                }
                this.setWasPaused(false);
                TransferInfo.Progress progress = tryGetWorkerProgress(workInfo);
                if (progress != null) {
                    this.transferInfoLiveData.setValue(TransferInfo.createProgress(transferId, progress));
                }
                return;
            }
            if (currentWorkInfoState == WorkInfo.State.CANCELLED) {
                if (this.transferFlags.isUserPaused()) {
                    this.setWasPaused(true);
                    this.transferInfoLiveData.setValue(TransferInfo.createUserPaused(transferId));
                } else {
                    this.setWasPaused(false);
                    this.transferInfoLiveData.setValue(TransferInfo.createCancelled(transferId));
                }
                return;
            }
            this.setWasPaused(currentWorkInfoState == WorkInfo.State.ENQUEUED);
            if (currentWorkInfoState == WorkInfo.State.ENQUEUED) {
                this.transferInfoLiveData.setValue(TransferInfo.createSystemPaused(transferId));
                return;
            }
            if (currentWorkInfoState == WorkInfo.State.SUCCEEDED) {
                this.transferInfoLiveData.setValue(TransferInfo.createCompleted(transferId));
                return;
            }
            if (currentWorkInfoState == WorkInfo.State.FAILED) {
                this.transferInfoLiveData.setValue(TransferInfo.createFailed(transferId,
                    tryGetWorkerErrorMessage(workInfo)));
                return;
            }
            Log.e(TAG, "Received Unexpected WorkInfo event from WorkManager:" + workInfo.toString());
        });
        return new Result(this.transferIdOrErrorLiveData,
            this.transferInfoLiveData,
            this.transferFlags);
    }

    /**
     * Store the transfer id or error received from TransferIdOrError LiveData.
     *
     * @param transferIdOrError the transfer id or error
     */
    private void setTransferIdOrError(TransferIdOrError transferIdOrError) {
        if (this.inputTransferIdOrError != null && !this.inputTransferIdOrError.isError()) {
            if (this.inputTransferIdOrError.getId() != transferIdOrError.getId()) {
                Log.e(TAG,
                    "Cannot be associated to a different transferId."
                        + " existing:" + this.inputTransferIdOrError.getId()
                        + " new:" + transferIdOrError.getId());
            }
        }
        this.inputTransferIdOrError = transferIdOrError;
    }

    /**
     * Check whether the current {@link TransferInfo} event about to send to the transfer
     * Observer is the first event.
     *
     * @return true for first event, false otherwise
     */
    private boolean isFirstEvent() {
        return this.isFirstEvent;
    }

    /**
     * Mark that the current {@link TransferInfo} event about to send to the transfer
     * Observer is the first event.
     */
    private void setDoneFirstEvent() {
        this.isFirstEvent = false;
    }

    /**
     * Check whether the last {@link TransferInfo} event sent to the transfer Observer was
     * a pause event i.e. {@link TransferInfo.State#SYSTEM_PAUSED} or {@link TransferInfo.State#USER_PAUSED}.
     *
     * @return true if the last event was a pause event, false otherwise.
     */
    private boolean wasPaused() {
        return this.wasPaused;
    }

    /**
     * Set whether the current event about to send to the transfer Observer is a pause
     * event i.e. {@link TransferInfo.State#SYSTEM_PAUSED} or {@link TransferInfo.State#USER_PAUSED}.
     *
     * @param wasPaused true for pause event, false for non-pause event
     */
    private void setWasPaused(boolean wasPaused) {
        this.wasPaused = wasPaused;
    }

    /**
     * Get the {@link WorkInfo.State} of the last {@link WorkInfo} event received from
     * the transfer worker.
     *
     * @return the workinfo state
     */
    private WorkInfo.State getLastWorkInfoState() {
        return this.lastWorkInfoState;
    }

    /**
     * Store the current {@link WorkInfo.State} so that if needed it can be used while
     * preparing to send next event to the transfer Observer.
     *
     * @param state the WorkInfo state
     */
    private void setLastWorkInfoState(WorkInfo.State state) {
        this.lastWorkInfoState = state;
    }

    /**
     * Get the LiveData that stream {@link WorkInfo} of a transfer worker corresponding to the input transfer id.
     *
     * This method uses {@link WorkManager} API to retrieve the LiveData of a transfer worker
     * that is processing the transfer identified by the transfer id emitted by {@code inputTransferIdOrErrorLiveData}.
     *
     * @param context the context
     * @return a LiveData of {@link WorkInfo}
     */
    private LiveData<WorkInfo> mapInputTransferIdToWorkInfoLiveData(Context context) {
        LiveData<List<WorkInfo>> workInfoListLiveData = Transformations
            .switchMap(
                this.transferIdOrErrorLiveData,
                transferIdOrError -> {
                    setTransferIdOrError(transferIdOrError);
                    if (this.inputTransferIdOrError.isError()) {
                        // An error from TransferClient. To continue the LiveData pipeline it is required
                        // to return non-null LiveData. Null from switchMapFunction will cut the pipeline.
                        MutableLiveData<List<WorkInfo>> emptyWorkInfoList = new MutableLiveData<>();
                        emptyWorkInfoList.setValue(null);
                        return emptyWorkInfoList;
                    } else {
                        // No error from TransferClient i.e. transfer work may exists, get the underlying
                        // LiveData<WorkInfo> for the transfer work.
                        final long transferId = this.inputTransferIdOrError.getId();
                        return WorkManager.getInstance(context)
                            .getWorkInfosForUniqueWorkLiveData(TransferClient.toTransferUniqueWorkName(transferId));
                    }
                }
            );
        return Transformations.map(workInfoListLiveData, workInfoList -> {
            if (this.inputTransferIdOrError.isError()) {
                // An error from TransferClient then emit null WorkInfo. The downstream should check error before
                // start processing the WorkInfo.
                return null;
            } else {
                final long transferId = this.inputTransferIdOrError.getId();
                if (workInfoList == null || workInfoList.isEmpty()) {
                    Log.e(TAG, "Received null or Empty WorkInfo list for the transfer '" + transferId + "' from WorkManager." );
                    return null;
                }
                if (workInfoList.size() > 1) {
                    Log.e(TAG, "Received multiple WorkInfo for the transfer '" + transferId + "' from WorkManager." );
                }
                return workInfoList.get(0);
            }
        });
    }

    /**
     * Map any error reported by TransferClient via inputTransferIdOrError to appropriate event
     * in output TransferInfo LiveData.
     */
    private void mapErrorFromTransferClient() {
        if (this.inputTransferIdOrError.isError()) {
            if (this.inputTransferIdOrError.getOperation() == TransferIdOrError.Operation.RESUME) {
                if (this.inputTransferIdOrError.isNotFoundError()) {
                    // Follow the same convention as WorkManager, i.e. if an Work is
                    // unknown then emit null. Here application provided transfer id was not
                    // identifying a transfer record.
                    this.transferInfoLiveData.setValue(null);
                    this.setLastWorkInfoState(WorkInfo.State.FAILED);
                    return;
                } else if (this.inputTransferIdOrError.isTerminatedError()) {
                    TransferIdOrError.TransferInTerminatedStateError tError = this.inputTransferIdOrError.getError();
                    if (tError.isCompleted()) {
                        this.transferInfoLiveData
                            .setValue(TransferInfo.createCompleted(this.inputTransferIdOrError.getId()));
                        this.setLastWorkInfoState(WorkInfo.State.SUCCEEDED);
                        return;
                    }
                }
                this.transferInfoLiveData.setValue(TransferInfo.createFailed(this.inputTransferIdOrError.getId(),
                    this.inputTransferIdOrError.getErrorMessage()));
                this.setLastWorkInfoState(WorkInfo.State.FAILED);
                return;
            } else {
                // TransferIdOrError.Operation.UPLOAD_DOWNLOAD
                this.transferInfoLiveData.setValue(TransferInfo.createFailed(this.inputTransferIdOrError.getId(),
                    this.inputTransferIdOrError.getErrorMessage()));
                this.setLastWorkInfoState(WorkInfo.State.FAILED);
                return;
            }
        }
    }

    /**
     * Try to retrieve transfer progress from a {@link WorkInfo}.
     *
     * @param workInfo the work info from transfer Worker
     * @return the progress description, null if description is not available in the WorkInfo.
     */
    private static TransferInfo.Progress tryGetWorkerProgress(WorkInfo workInfo) {
        Data progress = workInfo.getProgress();
        if (progress == null) {
            return null;
        }
        long totalBytes = progress.getLong(UploadWorker.Constants.PROGRESS_TOTAL_BYTES, -1);
        if (totalBytes == -1) {
            return null;
        }
        long bytesTransferred = progress.getLong(UploadWorker.Constants.PROGRESS_BYTES_UPLOADED, -1);
        return new TransferInfo.Progress(totalBytes, bytesTransferred);
    }

    /**
     * Try to retrieve transfer failure message from a {@link WorkInfo}.
     *
     * @param workInfo the work info object from transfer Worker
     * @return the error message, null if it is not available in the work info object.
     */
    private static String tryGetWorkerErrorMessage(WorkInfo workInfo) {
        Data data = workInfo.getOutputData();
        if (data == null) {
            return null;
        }
        return data.getString(UploadWorker.Constants.OUTPUT_ERROR_MESSAGE_KEY);
    }

    /**
     * Type to hold a pair consisting of transferIdOrError LiveData and associated TransferInfo LiveData.
     */
    final static class LiveDataPair {
        private final MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData;
        private final LiveData<TransferInfo> transferInfoLiveData;

        /**
         * Creates LiveDataPair.
         *
         * @param transferIdOrErrorLiveData the TransferIdOrError LiveData that TransferClient notify transferId
         * @param transferInfoLiveData the TransferInfo LiveData streaming TransferInfo of a transfer to it's Observers
         */
        LiveDataPair(@NonNull MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData,
                     @NonNull LiveData<TransferInfo> transferInfoLiveData) {
            this.transferIdOrErrorLiveData = transferIdOrErrorLiveData;
            this.transferInfoLiveData = transferInfoLiveData;
        }

        /**
         * Get the TransferIdOrError LiveData to notify the transferId.
         *
         * When transferId is set in this LiveData then the TransferInfo LiveData streams
         * TransferInfo of the transfer identified by that transfer id.
         *
         * @return the TransferIdOrError LiveData
         */
        MutableLiveData<TransferIdOrError> getTransferIdOrErrorLiveData() {
            return transferIdOrErrorLiveData;
        }

        /**
         * Get the TransferInfo LiveData.
         *
         * When a transferId is set in the TransferIdOrError LiveData then this LiveData streams
         * TransferInfo of the transfer identified by that transfer id.
         *
         * @return the TransferInfo LiveData
         */
        LiveData<TransferInfo> getTransferInfoLiveData() {
            return transferInfoLiveData;
        }
    }

    /**
     * Type representing result of {@link TransferIdInfoLiveData#create(Context)} method.
     */
    final static class Result {
        private final LiveDataPair liveDataPair;
        private final TransferFlags transferFlags;

        /**
         * Creates Result.
         *
         * @param transferIdOrErrorLiveData the TransferIdOrError LiveData that TransferClient sets transferId
         * @param transferInfoLiveData the TransferInfo LiveData streaming TransferInfo
         * @param transferFlags the object that TransferClient methods update any flag that it want
         *               TransferInfo LiveData source to know
         */
        private Result(@NonNull MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData,
                       @NonNull LiveData<TransferInfo> transferInfoLiveData,
                       @NonNull TransferFlags transferFlags) {
            this.liveDataPair = new LiveDataPair(transferIdOrErrorLiveData, transferInfoLiveData);
            this.transferFlags = transferFlags;
        }

        /**
         * Get the pair of holding transferIdOrError and TransferInfo LiveData.
         *
         * @return the LiveData pair
         */
        LiveDataPair getLiveDataPair() {
            return this.liveDataPair;
        }

        /**
         * Get the shared object that the TransferInfo LiveData source check for any flag
         * set by TransferClient methods on this transfer.
         *
         * @return the object that TransferClient methods update any flag that it want
         * TransferInfo LiveData source to know
         */
        TransferFlags getTransferFlags() {
            return this.transferFlags;
        }
    }

    /**
     * Instance of this type is used by TransferClient methods to set any flag that it want
     * TransferInfo LiveData source of the transfer to know.
     *
     * If a transfer has any 'active Observers' then that transfer's {@link TransferFlags}
     * and LiveData pair (TransferInfo and TransferIdOrError LiveData) will be tracked
     * in the {@link TransferIdInfoLiveDataCache} cache. By 'active Observers', we mean the
     * Observers of TransferInfo LiveData those are not GC-ed yet hence the corresponding cache entry.
     *
     * The cache is shared across all {@link TransferClient} instances within a application process.
     * This enables all Observers of a transfer [e.g. Observers of upload(tid: 1), Observers of
     * resume(tid: 1)] to share the same source TransferInfo LiveData. Because of TransferInfo LiveData
     * sharing, the effect of any flag set in {@link TransferFlags} by one {@link TransferClient} will be
     * visible to Observers of same transfer in a different {@link TransferClient}.
     */
    final static class TransferFlags {
        private volatile boolean userPaused;

        /**
         * Set the flag indicating that user paused a transfer by calling {@link TransferClient#pause(long)}.
         */
        void setUserPaused() {
            this.userPaused = true;
        }

        /**
         * Check whether the user paused the transfer in the current session.
         * Note: this method reset the flag once called.
         *
         * @return true if user paused the transfer, false otherwise.
         */
        @MainThread
        boolean isUserPaused() {
            boolean b = this.userPaused;
            this.userPaused = false;
            return b;
        }
    }
}
