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
import androidx.annotation.Nullable;

import com.azure.android.core.http.Callback;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public final class ResultTaskImpl<T> {
    // the reference to the head of the CallbackNode list (each node hold a Callback to receive the task result).
    private volatile CallbackNode<T> callbackNodes;
    // CAS Updater to update the above CallbackNode list head field.
    private static final AtomicReferenceFieldUpdater<ResultTaskImpl, CallbackNode> CALLBACK_NODES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ResultTaskImpl.class,  CallbackNode.class, "callbackNodes");;

    // the value that the task produces.
    private volatile T result;
    // an internal object to represent null result (when computation produces Void or fails).
    private static final Object NULL_RESULT = new Object();
    // CAS Updater to perform atomic onetime value set on the above result field.
    private static final AtomicReferenceFieldUpdater<ResultTaskImpl, Object> RESULT_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ResultTaskImpl.class, Object.class, "result");
    // the exception indicating task failure.
    private volatile Throwable throwable;

    // the reference to the CancelCallback wrapping the callback to be invoked when Task::cancel() is called.
    private volatile CancelCallback cancelCallback;
    // CAS Updater to perform atomic onetime value set on the above cancelCallback field.
    private static final AtomicReferenceFieldUpdater<ResultTaskImpl, CancelCallback> CANCEL_CALLBACK_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ResultTaskImpl.class, CancelCallback.class,"cancelCallback");
    private volatile boolean isCancelled;

    public ResultTaskImpl() {}

    public ResultTaskImpl(@NonNull Runnable onCancel) {
        Objects.requireNonNull(onCancel, "onCancel is required and cannot be null.");
        this.cancelCallback = new CancelCallback(onCancel);
    }

    public void addCallback(@NonNull Callback<T> callback, @NonNull Executor executor) {
        CallbackNode<T> node = new CallbackNode<>(callback, executor);
        final boolean added = this.tryAddCallbackNode(node);
        if (!added) {
            // Couldn't add since the list was frozen as a result of task completion,
            // invoke this Callback with the task result or error.
            node.invoke(this.result, this.throwable);
        }
    }

    public void cancel() {
        this.isCancelled = true;
        // We don't return immediately if 'isCancelled' was already true, but still an attempt
        // is made to invoke the callback, the reason for this is to handle the possible race
        // between cancel() call and setCancelCallback(..) call, if there is a race then
        // one of these methods will ensure callback is executed.
        //
        // Still multiple calls to cancel() won't result in multiple executions of the callback,
        // the invokeCallback allows only one invocation of cancel callback.
        CancelCallback callback = CANCEL_CALLBACK_UPDATER.get(this);
        if (callback != null) {
            callback.invoke();
        }
        // like invokeCallback, setFailed allows only single execution of registered callbacks.
        this.setFailed(new CancellationException("Task is cancelled."));
    }

    public boolean isCanceled() {
        return this.isCancelled;
    }

    /**
     * Mark the task as successfully completed and invoke any registered {@link Callback} instances.
     *
     * <p>
     * It is possible to mark the task as successfully completed only once. Once marked, then any later
     * attempt to set it again as completed will be ignored.
     * <p>
     * This method to mark the task as successfully completed and {@link this#setFailed(Throwable)}}
     * to mark completion with failure are mutually exclusive, completion can be set by only one of
     * these methods and cannot be changed later.
     * <p>
     *
     *
     * @param result The result of the task computation.
     */
    public void setSucceeded(@Nullable T result) {
        Object r = result == null ? NULL_RESULT : result;
        if (RESULT_UPDATER.compareAndSet(this, null, r)) {
            this.invokeCallbacks();
        }
    }

    /**
     * Mark the task as completed with failure and invoke any registered {@link Callback} instances.
     *
     * <p>
     * It is possible to mark the task as completed with failure only once. Once marked, then any later
     * attempt to set it again as completed will be ignored.
     * <p>
     * This method to mark the task as completed with failure and {@link this#setSucceeded(Object)}
     * to mark successful completion are mutually exclusive, completion can be set by only one of
     * these methods and cannot be changed later.
     *
     * @param throwable The reason for the task failure.
     */
    public void setFailed(@NonNull Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable is required and cannot be null.");
        if (RESULT_UPDATER.compareAndSet(this, null, NULL_RESULT)) {
            this.throwable = throwable;
            this.invokeCallbacks();
        }
    }

    /**
     * Sets the callback to notify when a call to {@link this#cancel()} is made to cancel the task.
     *
     * <p>
     * It is possible to set the onCancel callback only once. Once set, then any later attempt
     * to set it will be ignored.
     * <p>
     * Even if the user calls cancel() method multiple times, only one call to onCancel callback
     * will be ever made.
     * <p>
     * If the task is already in cancelled state by the time this setter method is called then
     * the onCancel callback will be immediately invoked. If there is a race between this method
     * and cancel() method, then it is ensured that callback is invoked exactly once by either
     * of these methods.
     *
     * @param onCancel The cancel callback.
     */
    public void setCancelCallback(@NonNull Runnable onCancel) {
        Objects.requireNonNull(onCancel, "onCancel is required and cannot be null.");
        final CancelCallback cancelCallback = new CancelCallback(onCancel);
        // Use CAS to ensure the callback is set only once.
        if (CANCEL_CALLBACK_UPDATER.compareAndSet(this, null, cancelCallback)) {
            // Ensure the 'onCancel' callback is called if the task is already cancelled.
            if (this.isCancelled) {
                // If there is a race between setCancelCallback(..) and cancel() calls, then
                // the CANCEL_CALLBACK_UPDATER and atomic nature of CancelCallback::invokeCallback
                // ensures onCancel is called exactly once.
                //
                this.cancelCallback.invoke();
            }
        }
    }

    /**
     * Try to atomically add a Callback node to the list.
     *
     * @param newNode The node holding {@link Callback}.
     *
     * @return false if the list got frozen hence Callback node is not added
     * to the list, true otherwise.
     */
    private boolean tryAddCallbackNode(CallbackNode<T> newNode) {
        //fetch the head pointing to the callback list.
        CallbackNode<T> headNode = this.callbackNodes;

        // busy-loop to atomically insert the new node to the list.
        do {
            newNode.next = headNode;
            // insert the new node to the list by making it the head and it's next
            // referencing to the old list.
            // Do this only if the head was not updated since last fetch, it is possible
            // that another thread added a new node in mean time, in that case, this thread's
            // local reference, headNode, to the head is outdated.
            if (!CALLBACK_NODES_UPDATER.compareAndSet(this, headNode, newNode)) {
                // head was changed since last fetched, re-fetch.
                headNode = this.callbackNodes;
            } else {
                // able to insert the new node (hence the head update) so return.
                return true;
            }
        } while (headNode != CallbackNode.FREEZED);

        // if the busy-loop detect that the list got frozen then it won't add
        // the callback to the list. This happens if the task gets completed.
        return false;
    }

    /**
     * Atomically mark the Callback list as frozen.
     *
     * @return The reference to the head of the Callback list.
     */
    private CallbackNode<T> freezeCallbackNodes() {
        CallbackNode<T> headNode;

        // busy-loop for atomic freezing of the list.
        do {
            headNode = this.callbackNodes;
        } while (!CALLBACK_NODES_UPDATER.compareAndSet(this, headNode, CallbackNode.FREEZED));

        return headNode;
    }

    /**
     * Atomically mark the Callback list as frozen and get the Callbacks
     * in the order those were added through {@link this#addCallback(Callback, Executor)}.
     *
     * @return The reference to the Callback list.
     */
    private CallbackNode<T> freezeAndGetCallbackNodes() {
        CallbackNode<T> current = this.freezeCallbackNodes();
        // The list need to be reversed since the callback needs to be called
        // in the order that user originally added it.
        CallbackNode<T> reversed = null;
        while (current != null) {
            CallbackNode<T> tmp = current;
            current = current.next;
            tmp.next = reversed;
            reversed = tmp;
        }
        return reversed;
    }

    /**
     * Execute all the callBacks in the order they were added through
     * {@link this#addCallback(Callback, Executor)}.
     */
    private void invokeCallbacks() {
        CallbackNode<T> next = this.freezeAndGetCallbackNodes();
        while (next != null) {
            next.invoke(this.result, this.throwable);
            next = next.next;
        }
    }

    /**
     * Represents a node in the Callback list.
     *
     * @param <U> the result type
     */
    private static final class CallbackNode<U> {
        // a node to indicate that the list is frozen hence no more
        // node can be added to the list.
        static final CallbackNode FREEZED = new CallbackNode(null, null);
        // the reference to the Callback.
        private final Callback<U> callback;
        // the executor the invoke the Callback methods.
        private final Executor executor;
        // the next node in the list.
        CallbackNode next;

        /**
         * Creates CallbackNode.
         *
         * @param callback The Callback to wrap.
         * @param executor The executor to invoke the Callback methods on.
         */
        CallbackNode(Callback<U> callback, Executor executor) {
            this.callback = callback;
            this.executor = executor;
        }

        /**
         * Invokes the wrapped Callback in it's executor.
         *
         * @param result The result of the task computation.
         * @param throwable The task computation failure reason.
         */
        public void invoke(final U result, final Throwable throwable) {
            this.executor.execute(() -> {
                if (throwable != null) {
                    callback.onFailure(throwable);
                } else {
                    U r = result == NULL_RESULT ? null : result;
                    callback.onResponse(r);
                }
            });
        }
    }

    /**
     * A type to wraps task cancel callback.
     */
    private static final class CancelCallback extends AtomicBoolean  {
        // the reference to cancel Callback.
        private final Runnable onCancel;

        CancelCallback(final Runnable onCancel) {
            this.onCancel = onCancel;
        }

        /**
         * Atomically invoke the cancel callback.
         *
         * <p>
         * Calling this method more than once is a NOP.
         */
        void invoke() {
            if (super.compareAndSet(false, true)) {
                this.onCancel.run();
            }
        }
    }
}
