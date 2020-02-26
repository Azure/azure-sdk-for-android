// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

/**
 * Package private.
 *
 * A type to transform an input LiveData that emit a transfer id to an output LiveData that emit
 * a stream of {@link TransferInfo}.
 *
 * Each {@link TransferInfo} event from output LiveData describes current state of the transfer
 * identified by the transfer id.
 */
final class TransferIdMappedToTransferInfo {
    private static final String TAG = TransferIdMappedToTransferInfo.class.getSimpleName();
    // transfer id from the input LiveData.
    private long transferId;
    // the output TransferInfo LiveData.
    private final MediatorLiveData<TransferInfo> transferInfoLiveData = new MediatorLiveData<>();
    // hold the state of the last WorkInfo received from Transfer Worker.
    private WorkInfo.State lastWorkInfoState;
    // flag to track whether current event about to emit is fist event.
    private boolean isFirstEvent = true;
    // flag to track whether the last event was a pause event.
    private boolean wasPaused;

    /**
     * Get LiveData that stream {@link TransferInfo} of a transfer.
     *
     * @param context the context
     * @param transferIdLiveData the input LiveData that emit a transfer id. The transfer id is not
     *     available at the time of calling this method, hence using LiveData to deliver the id once
     *     it is available.
     * @return an output LiveData that streams {@link TransferInfo} describing current state
     * of the transfer identified by the transfer id
     */
    @MainThread
    LiveData<TransferInfo> getTransferInfoLiveData(@NonNull Context context,
                                                   @NonNull LiveData<Long> transferIdLiveData) {
        LiveData<WorkInfo> workInfoLiveData = this.getWorkInfoLiveDataForTransferId(context, transferIdLiveData);
        this.transferInfoLiveData.addSource(workInfoLiveData, workInfo -> {
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
                // TODO: anuchan decide on error propagation
                this.transferInfoLiveData.setValue(TransferInfo.createFailed(transferId));
                return;
            }
            Log.e(TAG, "Received Unexpected WorkInfo event from WorkManager:" + workInfo.toString());
        });
        return this.transferInfoLiveData;
    }

    /**
     * Set the transfer id.
     *
     * @param transferId the transfer id
     */
    private void setTransferId(long transferId) {
        this.transferId = transferId;
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
     * that is processing the transfer identified by the given transfer id.
     *
     * @param context the context
     * @param transferIdLiveData the LiveData that emit the transfer id
     * @return a LiveData of {@link WorkInfo}
     */
    private LiveData<WorkInfo> getWorkInfoLiveDataForTransferId(Context context,
                                                                LiveData<Long> transferIdLiveData) {
        LiveData<List<WorkInfo>> workInfosLiveData = Transformations
            .switchMap(
                transferIdLiveData,
                transferId -> {
                    setTransferId(transferId);
                    return WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWorkLiveData(TransferClient.toTransferUniqueWorkName(transferId));
                }
            );
        return Transformations.map(workInfosLiveData, workInfos -> {
            if (workInfos == null || workInfos.isEmpty()) {
                Log.e(TAG, "Received NULL/Empty WorkInfo list from WorkManager::getWorkInfosForUniqueWorkLiveData.");
                return null;
            }
            if (workInfos.size() > 1) {
                Log.e(TAG, "Received Count(WorkInfos) > 1 from WorkManager::getWorkInfosForUniqueWorkLiveData.");
            }
            return workInfos.get(0);
        });
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
}
