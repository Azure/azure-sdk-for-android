// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkManager;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.azure.android.storage.blob.transfer.TransferIdInfoLiveData.TransferFlags;

/**
 * A Hash table based cache, with each entry has transferId as 'key' and 'value' as reference
 * to {@link TransferFlags}, {@link LiveData<TransferOperationResult>} and weak reference to
 * {@link LiveData<TransferInfo>}.
 *
 * The LiveData pair, i.e. TransferOperationResult LiveData and the TransferInfo LiveData
 * in a 'value' is related such that, when transferId set on TransferOperationResult LiveData
 * then transferInfo of corresponding transfer will start streaming from TransferInfo
 * LiveData.
 *
 * A cache entry will automatically be removed when the TransferInfo LiveData in
 * that entry's value is no longer in ordinary use. The presence of weak reference to it
 * in the cache entry will not prevent it being collected by the garbage collector.
 *
 * This cache is not synchronized explicitly but implicitly synchronized by enforcing its
 * methods to be accessible only from the main thread.
 *
 * The cache is designed to shared across all {@link TransferClient} instances within the
 * same application process. This enables all Observers of a transfer [e.g. Observers of
 * upload(tid: 1), Observers of resume(tid: 1)] to listen to same TransferInfo LiveData,
 * irrespective of the TransferClient they used to get the TransferInfo LiveData.
 *
 * The cache implementation assumes that the TransferOperationResult LiveData in the value
 * do not strongly refer to the TransferInfo LiveData in the same value either
 * directly or indirectly, since that will prevent the TransferOperationResult LiveData
 * from being collected.
 */
final class TransferIdInfoLiveDataCache {
    private static final String TAG = TransferIdInfoLiveDataCache.class.getSimpleName();
    // the internal map with each Entry in the format
    //  {
    //    key: transferId,
    //    value: {
    //        weak-ref: LiveData<TransferInfo>,
    //        ref: LiveData<TransferOperationResult>,
    //        ref: transferFlags
    //    }
    //  }
    private final HashMap<String, TransferInfoLiveDataWeakReference> map = new HashMap<>();
    // Reference queue that GC enqueues the map value holding collected weak-ref.
    private final ReferenceQueue<LiveData<TransferInfo>> queue = new ReferenceQueue<>();

    /**
     * Create a TransferOperationResult LiveData and a TransferInfo LiveData, associate it
     * with given {@code transferId} parameter and store them in the cache. This method return these
     * two LiveData in a {@Link TransferIdInfoLiveData#Pair}.
     *
     * When a transferId is set to TransferOperationResult LiveData then the TransferInfo LiveData stream
     * TransferInfo of the Transfer identified by that transferId. When transferId is set to
     * the TransferOperationResult LiveData it must be same as the transferId parameter.
     *
     * @param transferId the transferId to use as the key to identify the created LiveData
     *                   instances.
     * @param workManager reference to the {@link WorkManager} to retrieve WorkInfo.
     * @return the pair composing created {@code TransferOperationResult} LiveData and associated
     * {@code TransferInfo} LiveData
     */
    @MainThread
    TransferIdInfoLiveData.LiveDataPair create(String transferId, WorkManager workManager) {
        this.expunge();
        final TransferIdInfoLiveData.Result result = TransferIdInfoLiveData.create(workManager);
        final TransferIdInfoLiveData.LiveDataPair liveDataPair = result.getLiveDataPair();
        this.map.put(transferId,
            new TransferInfoLiveDataWeakReference(liveDataPair.getTransferInfoLiveData(),
                liveDataPair.getTransferOpResultLiveData(),
                result.getTransferFlags(),
                transferId,
                this.queue));
        return liveDataPair;
    }

    /**
     * Check whether the TransferOperationResult LiveData and the TransferInfo LiveData that is
     * identified by the given {@code transferId} key already exists in the cache, if it exists then
     * return the two LiveData in a {@Link TransferIdInfoLiveData#Pair}. If it does not exists then create,
     * store and return them, see {@link TransferIdInfoLiveDataCache#create(String, WorkManager)}
     * for more details.
     *
     * @param transferId the transferId to use as the key to identify the LiveData instances.
     * @param workManager reference to the {@link WorkManager} to retrieve WorkInfo.
     * @return the pair composing created TransferOperationResult LiveData and associated TransferInfo LiveData
     */
    @MainThread
    TransferIdInfoLiveData.LiveDataPair getOrCreate(String transferId, WorkManager workManager) {
        this.expunge();
        final TransferInfoLiveDataWeakReference ref = this.map.get(transferId);
        if (ref != null) {
            final LiveData<TransferInfo> transferInfoLiveData = ref.get();
            if (transferInfoLiveData != null) {
                return new TransferIdInfoLiveData.LiveDataPair(ref.getTransferOpResultLiveData(),
                    transferInfoLiveData);
            }
        }
        return create(transferId, workManager);
    }

    /**
     * Get the Flag object that the TransferInfo LiveData source (that backs TransferInfo LiveData
     * in the weak reference) check for any flag set by TransferClient methods on the transfer.
     *
     * @param transferId the transferId identifies the {@link TransferFlags} for the transfer
     * @return the {@link TransferFlags}
     */
    @MainThread
    TransferIdInfoLiveData.TransferFlags getTransferFlags(String transferId) {
        this.expunge();
        final TransferInfoLiveDataWeakReference ref = this.map.get(transferId);
        if (ref != null) {
            return ref.getTransferFlags();
        } else {
            return null;
        }
    }

    /**
     * Expunges stale entries (containing already GCed {@code TransferInfo} LiveData) from the map.
     */
    @SuppressWarnings("unchecked")
    private void expunge() {
        int cnt = 0;
        Reference<?> staledReference;
        while ((staledReference = this.queue.poll()) != null) {
            TransferInfoLiveDataWeakReference tReference = (TransferInfoLiveDataWeakReference) staledReference;
            tReference.expunge();
            this.map.remove(tReference.getTransferId());
            cnt++;
        }
        Log.d(TAG, "RefCount:" + map.size() + " Collected:" + cnt);
    }

    /**
     * Reference holding weak reference to TransferInfo LiveData and strong reference to TransferOperationResult
     * LiveData and {@link TransferFlags}.
     */
    private static class TransferInfoLiveDataWeakReference extends WeakReference<LiveData<TransferInfo>> {
        private final String transferId;
        private MutableLiveData<TransferOperationResult> transferOpResultLiveData;
        private TransferIdInfoLiveData.TransferFlags transferFlags;

        /**
         * Create a weak reference to TransferInfo LiveData.
         *
         * @param transferInfoLiveData the referent TransferInfo LiveData that this weak reference refer to
         * @param transferOpResultLiveData the TransferOperationResult LiveData
         * @param transferFlags the transfer flags object
         * @param transferId the transfer id
         * @param queue the reference queue for GC to enqueue this weak reference when once the
         *              referent TransferInfo LiveData is collected
         */
        TransferInfoLiveDataWeakReference(LiveData<TransferInfo> transferInfoLiveData,
                                          MutableLiveData<TransferOperationResult> transferOpResultLiveData,
                                          TransferIdInfoLiveData.TransferFlags transferFlags,
                                          String transferId,
                                          ReferenceQueue<LiveData<TransferInfo>> queue) {
            super(transferInfoLiveData, queue);
            this.transferId = transferId;
            this.transferOpResultLiveData = transferOpResultLiveData;
            this.transferFlags = transferFlags;
        }

        /**
         * Get the transfer id.
         *
         * @return the transfer id
         */
        String getTransferId() {
            return this.transferId;
        }

        /**
         * Get the transferId LiveData.
         *
         * When transferId set on this LiveData then transferInfo of corresponding
         * transfer will start streaming by transferInfo LiveData.
         *
         * @return the transferId LiveData
         */
        MutableLiveData<TransferOperationResult> getTransferOpResultLiveData() {
            return this.transferOpResultLiveData;
        }

        /**
         * Get the Flag object that the TransferInfo LiveData source (that backs TransferInfo LiveData
         * in the weak reference) check for any flag set by TransferClient methods on the transfer.
         *
         * @return the {@link TransferIdInfoLiveData.TransferFlags}
         */
        TransferIdInfoLiveData.TransferFlags getTransferFlags() {
            return this.transferFlags;
        }

        /**
         * null out any strong references.
         */
        void expunge() {
            if (this.get() == null) {
                this.transferOpResultLiveData = null;
                this.transferFlags = null;
            }
        }
    }
}
