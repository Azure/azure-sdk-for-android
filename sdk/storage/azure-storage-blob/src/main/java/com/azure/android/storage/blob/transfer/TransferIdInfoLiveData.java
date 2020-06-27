// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

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
 * A type to create the LiveData pair, a {@link TransferOperationResult} LiveData and associated {@link TransferInfo}
 * LiveData. Each transfer will have a unique LiveData pair.
 *
 * The TransferInfo LiveData streams {@link TransferInfo} events describing the current state of a transfer when
 * the transfer id for that transfer set in TransferOperationResult LiveData. Internally the source of the TransferInfo
 * LiveData queries {@link WorkManager} for a LiveData that streams {@link WorkInfo} and transform {@link WorkInfo}
 * events to {@link TransferInfo} events.
 *
 * The same transfer id can be set multiple times in the TransferOperationResult LiveData. Each such set results in
 * querying {@link WorkManager} as described above.
 *
 * When the application pauses a transfer, there can be already active Observes for the transfer; later,
 * if the application resumes the transfer using a different worker, we want to ensure those observers will continue
 * to receive events. The ability to set transfer id on the TransferOperationResult LiveData multiple times enables us
 * to switch the worker while keep emitting from the same TransferInfo LiveData.
 *
 * @see TransferIdInfoLiveDataCache (for LiveData pair sharing)
 */
final class TransferIdInfoLiveData {
    private static final String TAG = TransferIdInfoLiveData.class.getSimpleName();
    // the input LiveData that receives the transfer id or error from TransferClient operations.
    private final MutableLiveData<TransferOperationResult> transferOpResultLiveData = new MutableLiveData<>();
    // the recent TransferOperationResult object emitted by the transferOpResultLiveData.
    private TransferOperationResult transferOperationResult;
    // the output TransferInfo LiveData.
    private final MediatorLiveData<TransferInfo> transferInfoLiveData = new MediatorLiveData<>();
    // Once the above 'transferInfoLiveData' as a result of calling TransferClient::upload|download is
    // given to the application Observers, a TransferInfo generator backing the LiveData will start producing
    // the transfer events. This TransferInfo generator has to be notified about any other TransferClient
    // operation performed on the same transfer. For example, when the generator sees WorkInfo.State.CANCELLED,
    // it could be either application calling pause or cancel on the same transfer through TransferClient.
    // TransferFlags is an object that TransferClient has access to. The generator can inspect this flag object
    // to check the reason for WorkInfo.State.CANCELLED. Generator could also check the DB for the same flat,
    // but don't have to really make an io for it when this in-memory transferFlags object is available.
    private final TransferFlags transferFlags = new TransferFlags();
    // hold the state of the last WorkInfo received from Transfer Worker.
    private WorkInfo.State lastWorkInfoState;
    // flag to track whether current event about to emit from transferInfoLiveData is the first event.
    private boolean isFirstEvent = true;
    // flag to track whether the last emitted event from transferInfoLiveData was a pause event.
    private boolean wasPaused;

    private TransferIdInfoLiveData() {}

    @MainThread
    static TransferIdInfoLiveData.Result create(WorkManager workManager) {
        TransferIdInfoLiveData transferIdInfoLiveData = new TransferIdInfoLiveData();
        return transferIdInfoLiveData.init(workManager);
    }

    private TransferIdInfoLiveData.Result init(@NonNull WorkManager workManager) {
        // 1. Register mapping of transferId to LiveData<WorkInfo>
        LiveData<WorkInfo> workInfoLiveData = this.mapInputTransferIdToWorkInfoLiveData(workManager);
        // 2. Register mapping of LiveData<WorkInfo> to LiveData<TransferInfo>
        this.transferInfoLiveData.addSource(workInfoLiveData, workInfo -> {
            // The TransferInfo events generator.
            //
            if (this.transferOperationResult.isError()) {
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
            final String transferId = this.transferOperationResult.getId();
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
        return new Result(this.transferOpResultLiveData,
            this.transferInfoLiveData,
            this.transferFlags);
    }

    /**
     * Store the transfer id or error received from TransferOperationResult LiveData.
     *
     * @param transferOperationResult the transfer id or error
     */
    private void setTransferOperationResult(TransferOperationResult transferOperationResult) {
        if (this.transferOperationResult != null && !this.transferOperationResult.isError()) {
            if (this.transferOperationResult.getId() != transferOperationResult.getId()) {
                Log.e(TAG,
                    "Cannot be associated to a different transferId."
                        + " existing:" + this.transferOperationResult.getId()
                        + " new:" + transferOperationResult.getId());
            }
        }
        this.transferOperationResult = transferOperationResult;
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
     * @return the WorkInfo state
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
     * Get the LiveData that stream {@link WorkInfo} of a transfer worker.
     *
     * This method uses {@link WorkManager} API to retrieve the  WorkInfo LiveData of a transfer
     * worker that is processing the transfer identified by the transfer id emitted by
     * {@code transferIdOrErrorLiveData}.
     *
     * @param workManager reference to the {@link WorkManager} to retrieve WorkInfo
     * @return a LiveData of {@link WorkInfo}
     */
    private LiveData<WorkInfo> mapInputTransferIdToWorkInfoLiveData(WorkManager workManager) {
        LiveData<List<WorkInfo>> workInfoListLiveData = Transformations
            .switchMap(
                this.transferOpResultLiveData,
                transferOperationResult -> {
                    setTransferOperationResult(transferOperationResult);
                    if (this.transferOperationResult.isError()) {
                        // An error from TransferClient. To continue the LiveData pipeline it is required
                        // to return non-null LiveData. Null from switchMapFunction will cut the pipeline.
                        MutableLiveData<List<WorkInfo>> emptyWorkInfoList = new MutableLiveData<>();
                        emptyWorkInfoList.setValue(null);
                        return emptyWorkInfoList;
                    } else {
                        // No error from TransferClient i.e. transfer work may exists, get the underlying
                        // LiveData<WorkInfo> for the transfer work.
                        final String transferId = this.transferOperationResult.getId();
                        return workManager
                            .getWorkInfosForUniqueWorkLiveData(transferId);
                    }
                }
            );
        return Transformations.map(workInfoListLiveData, workInfoList -> {
            if (this.transferOperationResult.isError()) {
                // An error from TransferClient then emit null WorkInfo. The downstream should check error before
                // start processing the WorkInfo.
                return null;
            } else {
                final String transferId = this.transferOperationResult.getId();
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
     * Map any error reported by TransferClient via transferOperationResultLiveData to appropriate event
     * that transferInfoLiveData emits.
     */
    private void mapErrorFromTransferClient() {
        if (this.transferOperationResult.isError()) {
            // Handle error from TransferClient methods
            if (this.transferOperationResult.getOperation() == TransferOperationResult.Operation.RESUME) {
                // Error from TransferClient::resume.
                if (this.transferOperationResult.isNotFoundError()) {
                    // Follow the same convention as WorkManager, i.e. if an Work is
                    // unknown then emit null. Here application provided transfer id was not
                    // identifying a transfer record.
                    this.transferInfoLiveData.setValue(null);
                    this.setLastWorkInfoState(WorkInfo.State.FAILED);
                    return;
                } else if (this.transferOperationResult.isTerminatedError()) {
                    TransferOperationResult.TransferInTerminatedStateError tError = this.transferOperationResult.getError();
                    if (tError.isCompleted()) {
                        this.transferInfoLiveData
                            .setValue(TransferInfo.createCompleted(this.transferOperationResult.getId()));
                        this.setLastWorkInfoState(WorkInfo.State.SUCCEEDED);
                        return;
                    }
                }
                this.transferInfoLiveData.setValue(TransferInfo.createFailed(this.transferOperationResult.getId(),
                    this.transferOperationResult.getErrorMessage()));
                this.setLastWorkInfoState(WorkInfo.State.FAILED);
                return;
            } else {
                // Error from TransferClient::upload or TransferClient::download.
                this.transferInfoLiveData.setValue(TransferInfo.createFailed(this.transferOperationResult.getId(),
                    this.transferOperationResult.getErrorMessage()));
                this.setLastWorkInfoState(WorkInfo.State.FAILED);
                return;
            }
        }
    }

    /**
     * Try to retrieve transfer progress from a {@link WorkInfo}.
     *
     * @param workInfo the WorkInfo from transfer Worker
     * @return the progress description, null if description is not available in the WorkInfo.
     */
    private static TransferInfo.Progress tryGetWorkerProgress(WorkInfo workInfo) {
        Data progress = workInfo.getProgress();
        if (progress == null) {
            return null;
        }
        long totalBytes = progress.getLong(TransferConstants.PROGRESS_TOTAL_BYTES, -1);
        if (totalBytes == -1) {
            return null;
        }
        long bytesTransferred = progress.getLong(TransferConstants.PROGRESS_BYTES_TRANSFERRED, -1);
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
        return data.getString(TransferConstants.OUTPUT_ERROR_MESSAGE_KEY);
    }

    /**
     * Type to hold a pair consisting of TransferOperationResult LiveData and associated TransferInfo LiveData.
     */
    final static class LiveDataPair {
        private final MutableLiveData<TransferOperationResult> transferOpResultLiveData;
        private final LiveData<TransferInfo> transferInfoLiveData;

        /**
         * Creates LiveDataPair.
         *
         * @param transferOpResultLiveData the TransferOperationResult LiveData that TransferClient
         *                                 notify transferId or error
         * @param transferInfoLiveData the TransferInfo LiveData streaming TransferInfo of a transfer
         *                             to it's Observers
         */
        LiveDataPair(@NonNull MutableLiveData<TransferOperationResult> transferOpResultLiveData,
                     @NonNull LiveData<TransferInfo> transferInfoLiveData) {
            this.transferOpResultLiveData = transferOpResultLiveData;
            this.transferInfoLiveData = transferInfoLiveData;
        }

        /**
         * Get the TransferOperationResult LiveData to notify the transferId.
         *
         * When transferId is set in this LiveData then the TransferInfo LiveData streams
         * TransferInfo of the transfer identified by that transfer id.
         *
         * @return the TransferOperationResult LiveData
         */
        MutableLiveData<TransferOperationResult> getTransferOpResultLiveData() {
            return transferOpResultLiveData;
        }

        /**
         * Get the TransferInfo LiveData.
         *
         * When a transferId is set in the TransferOperationResult LiveData then this LiveData streams
         * TransferInfo of the transfer identified by that transfer id.
         *
         * @return the TransferInfo LiveData
         */
        LiveData<TransferInfo> getTransferInfoLiveData() {
            return transferInfoLiveData;
        }
    }

    /**
     * Type representing result of {@link TransferIdInfoLiveData#create(WorkManager)} method.
     */
    final static class Result {
        private final LiveDataPair liveDataPair;
        private final TransferFlags transferFlags;

        /**
         * Creates Result.
         *
         * @param transferIdOrErrorLiveData the TransferOperationResult LiveData that TransferClient sets transferId
         * @param transferInfoLiveData the TransferInfo LiveData streaming TransferInfo
         * @param transferFlags the object that TransferClient methods update any flag that it want
         *               TransferInfo LiveData source to know
         */
        private Result(@NonNull MutableLiveData<TransferOperationResult> transferIdOrErrorLiveData,
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
     * An instance of this type is used by TransferClient methods to communicate any flag to a TransferInfo
     * LiveData source.
     *
     * If a transfer has any 'active Observers' then that transfer's {@link TransferFlags} and LiveData pair
     * (TransferInfo and TransferOperationResult) will be tracked in the {@link TransferIdInfoLiveDataCache}
     * cache. All {@link TransferClient} instances within an application process share the same cache.
     *
     * Once TransferInfo LiveData is given to the application Observers, a TransferInfo generator backing
     * the LiveData will start producing the transfer events. This TransferInfo generator has to be notified
     * about any other TransferClient operation performed on the same transfer. For example, when the generator
     * sees WorkInfo.State.CANCELLED, it could be either application calling pause or cancel on the same transfer
     * through TransferClient. TransferFlags is an object that TransferClient has access to. The generator can
     * inspect this flag object to check the reason for WorkInfo.State.CANCELLED. Generator could also check
     * the DB for the same flag, but don't have to really make an io for it when this in-memory transferFlags
     * object is available.
     */
    final static class TransferFlags {
        private volatile boolean userPaused;

        /**
         * Set the flag indicating that user paused a transfer by calling {@link TransferClient#pause(String)}.
         */
        void setUserPaused() {
            this.userPaused = true;
        }

        /**
         * Check whether the user paused the transfer in the current session.
         *
         * @return true if user paused the transfer, false otherwise.
         */
        @MainThread
        boolean isUserPaused() {
            final boolean isPaused = this.userPaused;
            // We cancel the androidx.work.ListenableWorker backing the transfer in two cases:
            //
            // 1. When user pause the transfer
            // 2. When user cancel the transfer
            //
            // androidx worker cancel feature is used for case#1 since there is no concept of 'user pause'
            // in androidx.work.WorkManager framework.
            //
            // When TransferInfo LiveData source get a WorkInfo.State.CANCELLED event it calls
            // this method to check cancellation was happened due to 'user pause'.
            //
            // Below we reset the userPaused flag once read, so that a WorkInfo.State.CANCELLED event
            // as a result of 'user cancel' after a 'user pause' is not treated as a 'user pause'.
            //
            this.userPaused = false;
            return isPaused;
        }
    }
}
