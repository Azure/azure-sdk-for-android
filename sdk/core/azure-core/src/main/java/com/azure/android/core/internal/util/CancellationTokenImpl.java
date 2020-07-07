/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.android.core.internal.util;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.azure.android.core.util.CancellationToken;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Internal implementation of {@link CancellationToken}.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class CancellationTokenImpl extends CancellationToken {
    // the reference to the head of the OnCancelNode list (each node hold an onCancel Callback).
    private volatile OnCancelNode onCancelNodes;
    // CAS Updater to update the above OnCancelNode list head field.
    private static final AtomicReferenceFieldUpdater<CancellationTokenImpl, OnCancelNode> ON_CANCEL_NODES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(CancellationTokenImpl.class,  OnCancelNode.class, "onCancelNodes");
    // Ensures side-effect of app calling cancel() happens only once.
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    @Override
    public void cancel() {
        if (this == CancellationToken.NONE) {
            return;
        }
        if (this.isCancelled.compareAndSet(false, true)) {
            this.invokeCallbacks();
        }
    }

    /**
     * Registers the onCancel Callback that will be called when this CancellationToken is canceled.
     *
     * This operation of registering the CallBack is non-blocking and thread-safe. If the token
     * is already in the cancelled state then the provided CallBack will be immediately executed.
     *
     * This is O(1) in best case and O(k) in worst, where k is number of concurrent threads in case
     * of race.
     *
     * @param onCancel The onCancel Callback.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void registerOnCancel(@NonNull Runnable onCancel) {
        if (this == CancellationToken.NONE) {
            return;
        }
        OnCancelNode node = new OnCancelNode(onCancel);
        final boolean added = this.tryAddOnCancelNode(node);
        if (!added) {
            // Couldn't add since the list was frozen as a result of cancellation,
            // invoke this onCancel Callback immediately.
            node.invokeOnCancel();
        }
    }

    /**
     * Registers the onCancel Callback that will be called when this CancellationToken is canceled.
     *
     * This operation of registering the CallBack is non-blocking and thread-safe. If the token
     * is already in the cancelled state then the provided CallBack will be immediately executed.
     *
     * This is O(1) in best case and O(k) in worst, where k is number of concurrent threads in case
     * of race in registering Callback.
     *
     * @param id The registration id for the onCancel Callback.
     * @param onCancel The onCancel Callback.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void registerOnCancel(@NonNull String id, @NonNull Runnable onCancel) {
        if (this == CancellationToken.NONE) {
            return;
        }
        OnCancelNode node = new OnCancelNode(id, onCancel);
        final boolean added = this.tryAddOnCancelNode(node);
        if (!added) {
            // Couldn't add since the list was frozen as a result of cancellation,
            // invoke this onCancel Callback immediately.
            node.invokeOnCancel();
        }
    }

    /**
     * Unregister the onCancel Callback.
     *
     * This operation of unregistering the CallBack is non-blocking and thread-safe.
     *
     * @param id The id of the onCancel Callback to unregister.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void unregisterOnCancel(@NonNull String id) {
        if (this == CancellationToken.NONE) {
            return;
        }
        OnCancelNode itr = this.onCancelNodes;
        // Step_1: Locate the node and mark it as logically deleted.
        //
        while (itr != null) {
            if (this.onCancelNodes == OnCancelNode.FROZEN) {
                // The token is cancelled, no use in progressing.
                return;
            }
            if (itr.id != null && itr.id.equals(id)) {
                // Mark the node as logically deleted.
                itr.markDeleted();
                break;
            }
        }
        if (itr == null) {
            // A node with id was not found.
            return;
        }

        // Step_2: Sweep to unlink all logically deleted nodes.
        //
        boolean hadRace;
        do {
            // Outer 'do-while' to retry on any race during sweep.
            hadRace = false;
            OnCancelNode predecessor = null;
            OnCancelNode current = this.onCancelNodes; // re-fetch the volatile head for each retry.
            if (current == OnCancelNode.FROZEN) {
                // The token is cancelled, no use in sweeping.
                return;
            }
            OnCancelNode successor;
            // Inner 'while' to sweep & unlink all logically deleted nodes.
            while (current != null) {
                successor = current.next;
                if (current.isDeleted()) {
                    // Un-linking 'current' node.
                    if (predecessor == null) {
                        // The 'current' node has no 'predecessor' hence it's head, try CAS head with 'successor'.
                        if (!ON_CANCEL_NODES_UPDATER.compareAndSet(this, current, successor)) {
                            // Raced with
                            //     1. another thread calling registerOnCancelCallback.
                            //     2. OR the 'cancel()' call.
                            // need to retry.
                            hadRace = true;
                            break;
                        }
                    } else {
                        // The 'current' node has a 'predecessor'.
                        predecessor.next = successor;
                        if (predecessor.isDeleted()) {
                            // Raced with another thread that unlinked 'predecessor', need to retry.
                            hadRace = true;
                            break;
                        }
                    }
                } else {
                    // We aren't un-linking 'current' node, update 'predecessor'.
                    predecessor = current;
                }
                current = successor;
            }
        } while (hadRace);
    }

    /**
     * Try to atomically add an onCancel node to the list.
     *
     * @param newNode The node holding onCancel Callback.
     *
     * @return false if the list got frozen hence onCancel Callback node is
     * not added to the list, true otherwise.
     */
    private boolean tryAddOnCancelNode(OnCancelNode newNode) {
        //fetch the head pointing to the callback list.
        OnCancelNode headNode = this.onCancelNodes;
        if (headNode == OnCancelNode.FROZEN) {
            return false;
        }
        // busy-loop to atomically insert the new node to the list.
        do {
            newNode.next = headNode;
            // insert the new node to the list by making it the head and it's next
            // referencing to the old list.
            // Do this only if the head was not updated since last fetch, it is possible
            // that another thread added a new node in mean time, in that case, this thread's
            // local reference, headNode, to the head is outdated.
            if (!ON_CANCEL_NODES_UPDATER.compareAndSet(this, headNode, newNode)) {
                // head was changed since last fetched, re-fetch.
                headNode = this.onCancelNodes;
            } else {
                // able to insert the new node (hence the head update) so return.
                return true;
            }
        } while (headNode != OnCancelNode.FROZEN);

        // if the busy-loop detect that the list got frozen then it won't add
        // the callback to the list. This happens if the token is already cancelled.
        return false;
    }

    /**
     * Atomically mark the onCancel Callback list as frozen.
     *
     * @return The reference to the head of the onCancel Callback list.
     */
    private OnCancelNode freezeCallbackNodes() {
        OnCancelNode headNode;
        // busy-loop for atomic freezing of the list.
        do {
            headNode = this.onCancelNodes;
        } while (!ON_CANCEL_NODES_UPDATER.compareAndSet(this, headNode, OnCancelNode.FROZEN));

        return headNode;
    }

    /**
     * Atomically mark the onCancel Callback list as frozen and get the Callbacks
     * in the order those were added through {@link this#registerOnCancel(Runnable)}.
     *
     * @return The reference to the onCancel Callback list.
     */
    private OnCancelNode freezeAndGetCallbackNodes() {
        OnCancelNode current = this.freezeCallbackNodes();
        // The list has to be reversed since the onCancel callback needs to be
        // called in the order that they were originally added.
        OnCancelNode reversed = null;
        while (current != null) {
            OnCancelNode tmp = current;
            current = current.next;
            tmp.next = reversed;
            reversed = tmp;
        }
        return reversed;
    }

    /**
     * Execute all the onCancel Callbacks in the order they were added through
     * {@link this#registerOnCancel(Runnable)}.
     */
    private void invokeCallbacks() {
        OnCancelNode next = this.freezeAndGetCallbackNodes();
        while (next != null) {
            next.invokeOnCancel();
            next = next.next;
        }
    }

    /**
     * Represents a node in the OnCancel Callback list.
     */
    private static final class OnCancelNode {
        // a node to indicate that the list is frozen hence no more
        // node can be added to the list.
        static final OnCancelNode FROZEN = new OnCancelNode(null);
        private final String id;
        // the reference to the OnCancel Callback.
        private final Runnable onCancel;
        // indicate whether this node is marked as deleted.
        private volatile boolean isDeleted = false;
        // the next node in the list.
        volatile OnCancelNode next;

        /**
         * Creates OnCancelNode.
         *
         * @param onCancel The onCancel Callback to wrap.
         */
        OnCancelNode(Runnable onCancel) {
            this.id = null;
            this.onCancel = onCancel;
        }

        /**
         * Creates OnCancelNode.
         *
         * @param id The id for the node.
         * @param onCancel The onCancel Callback to wrap.
         */
        OnCancelNode(String id, Runnable onCancel) {
            this.id = Objects.requireNonNull(id, "'id' is required and cannot be null.");
            this.onCancel = onCancel;
        }

        /**
         * Invokes the wrapped onCancel Callback in it's executor.
         */
        void invokeOnCancel() {
            if (this.isDeleted) {
                return;
            }
            onCancel.run();
        }

        /**
         * Mark the node as deleted hence not a part of the OnCancel Callback list
         * any more.
         */
        void markDeleted() {
            this.isDeleted = true;
        }

        /**
         * Check whether the node is deleted from the list.
         *
         * @return true if deleted, false otherwise.
         */
        boolean isDeleted() {
            return this.isDeleted;
        }
    }
}
