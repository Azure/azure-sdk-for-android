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
 */
final class TransferIdMappedToTransferInfo {
    private static final String TAG = TransferIdMappedToTransferInfo.class.getSimpleName();
    // the input LiveData that receives the transfer id or error.
    private final MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData = new MutableLiveData<>();
    // the output TransferInfo LiveData.
    private final MediatorLiveData<TransferInfo> transferInfoLiveData = new MediatorLiveData<>();
    // the recent TransferIdOrError object emitted by the transferIdOrErrorLiveData object.
    private TransferIdOrError inputTransferIdOrError;
    // hold the state of the last WorkInfo received from Transfer Worker.
    private WorkInfo.State lastWorkInfoState;
    // flag to track whether current event about to emit is fist event.
    private boolean isFirstEvent = true;
    // flag to track whether the last event was a pause event.
    private boolean wasPaused;

    private TransferIdMappedToTransferInfo() {}

    @MainThread
    static TransferIdMappedToTransferInfo.Result create(Context context) {
        TransferIdMappedToTransferInfo transferIdInfoLiveData = new TransferIdMappedToTransferInfo();
        return transferIdInfoLiveData.init(context);
    }

    private TransferIdMappedToTransferInfo.Result  init(@NonNull Context context) {
        LiveData<WorkInfo> workInfoLiveData = this.mapInputTransferIdToWorkInfoLiveData(context);
        this.transferInfoLiveData.addSource(workInfoLiveData, workInfo -> {
            if (this.inputTransferIdOrError.isError()) {
                mapErrorFromTransferClient();
                return;
            }
            if (workInfo == null) {
                Log.v(TAG, "Received NULL WorkInfo event from WorkManager.");
                return;
            }
            final WorkInfo.State lastWorkInfoState = getLastWorkInfoState();
            if (lastWorkInfoState != null && lastWorkInfoState.isFinished()) {
                Log.e(TAG, "Received an unexpected WorkInfo event from WorkManager after terminal state:"
                    + workInfo.toString());
                return;
            }
            WorkInfo.State currentWorkInfoState = workInfo.getState();
            if (currentWorkInfoState == null) {
                Log.v(TAG, "Received a WorkInfo event from WorkManager with NULL state.");
                return;
            }
            final long transferId = this.inputTransferIdOrError.getId();
            this.setLastWorkInfoState(currentWorkInfoState);
            if (this.isFirstEvent()) {
                this.setDoneFirstEvent();
                this.setWasPaused(false);
                this.transferInfoLiveData.setValue(TransferInfo.createStarted(transferId));
                if (currentWorkInfoState == WorkInfo.State.ENQUEUED) {
                    return;
                }
            }
            if (currentWorkInfoState == WorkInfo.State.RUNNING) {
                if (this.wasPaused()) {
                    this.transferInfoLiveData.setValue(TransferInfo.createResumed(transferId));
                }
                this.setWasPaused(false);
                TransferInfo.Progress progress = tryGetProgress(workInfo);
                if (progress != null) {
                    this.transferInfoLiveData.setValue(TransferInfo.createProgress(transferId, progress));
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
            this.transferInfoLiveData);
    }

    /**
     * Store the transfer id or error received from TransferIdOrError LiveData.
     *
     * @param transferIdOrError the transfer id or error
     */
    private void setTransferIdOrError(TransferIdOrError transferIdOrError) {
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
     * Check whether the last {@link TransferInfo} event sent to the transfer Observer
     * was a pause event {@link TransferInfo.State#SYSTEM_PAUSED}.
     *
     * @return true if the last event was a pause event, false otherwise.
     */
    private boolean wasPaused() {
        return this.wasPaused;
    }

    /**
     * Set whether the current event about to send to the transfer Observer is a pause
     * event {@link TransferInfo.State#SYSTEM_PAUSED} or not.
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
     * @param state the workinfo state
     */
    private void setLastWorkInfoState(WorkInfo.State state) {
        this.lastWorkInfoState = state;
    }

    /**
     * Get the LiveData that stream {@link WorkInfo} of a transfer worker.
     *
     * This method uses {@link WorkManager} API to retrieve the LiveData of a transfer worker
     * that is processing the transfer identified by the transfer id from transferIdOrErrorLiveData.
     *
     * @param context the context
     * @return a LiveData of {@link WorkInfo}
     */
    private LiveData<WorkInfo> mapInputTransferIdToWorkInfoLiveData(Context context) {
        LiveData<List<WorkInfo>> workInfosLiveData = Transformations
            .switchMap(
                transferIdOrErrorLiveData,
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
        return Transformations.map(workInfosLiveData, workInfoList -> {
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
            this.transferInfoLiveData.setValue(TransferInfo.createFailed(this.inputTransferIdOrError.getId(),
                this.inputTransferIdOrError.getErrorMessage()));
            this.setLastWorkInfoState(WorkInfo.State.FAILED);
            return;
        }
    }

    /**
     * Try to retrieve transfer progress from a {@link WorkInfo}.
     *
     * @param workInfo the work info from transfer Worker
     * @return the progress description, null if description is not available in the workinfo.
     */
    private static TransferInfo.Progress tryGetProgress(WorkInfo workInfo) {
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
        private LiveDataPair(@NonNull MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData,
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
     * Type representing result of {@link TransferIdMappedToTransferInfo#create(Context)} method.
     */
    final static class Result {
        private final LiveDataPair liveDataPair;

        /**
         * Creates Result.
         *
         * @param transferIdOrErrorLiveData the TransferIdOrError LiveData that TransferClient sets transferId
         * @param transferInfoLiveData the TransferInfo LiveData streaming TransferInfo
         */
        private Result(@NonNull MutableLiveData<TransferIdOrError> transferIdOrErrorLiveData,
                       @NonNull LiveData<TransferInfo> transferInfoLiveData) {
            this.liveDataPair = new LiveDataPair(transferIdOrErrorLiveData, transferInfoLiveData);
        }

        /**
         * Get the pair of holding transferIdOrError and TransferInfo LiveData.
         *
         * @return the LiveData pair
         */
        LiveDataPair getLiveDataPair() {
            return this.liveDataPair;
        }
    }
}
