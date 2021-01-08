// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.core.logging.ClientLogger;

import java.io.InterruptedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The dispatcher to dispatch async HTTP calls send through the pipeline. Additionally, an HttpClient
 * that does not have native async support can also use the dispatcher to enable async HTTP calls.
 */
public final class HttpCallDispatcher {
    private final ClientLogger logger = new ClientLogger(HttpCallDispatcher.class);

    private int maxRunningCalls = 64;
    private final ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private final Deque<DispatchableCall> waitingRootDispatchableCalls = new ArrayDeque<>();
    private final Deque<DispatchableCall> runningRootDispatchableCalls = new ArrayDeque<>();
    private final Deque<DispatchableCall> waitingNestedDispatchableCalls = new ArrayDeque<>();

    /**
     * Creates an HttpCallDispatcher with an ExecutorService with default settings to execute HTTP calls.
     */
    public HttpCallDispatcher() {
        // The ThreadPoolExecutor by design creates the core threads only when new tasks arrive,
        // essentially lazy by default.
        this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>());
        this.scheduledExecutorService = null;
    }

    /**
     * Creates an HttpCallDispatcher that uses the given {@code executorService} to execute HTTP calls.
     *
     * @param executorService The executor service.
     */
    public HttpCallDispatcher(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService, "'executorService' is required.");
        this.scheduledExecutorService = null;
    }

    /**
     * Creates an HttpCallDispatcher that uses the given {@code executorService} to execute HTTP calls
     * and uses the the given {@code scheduledExecutorService} to schedule HTTP calls to execute on
     * {@code executorService} after a specific delay.
     *
     * @param executorService The executor service.
     * @param scheduledExecutorService The scheduled executor service.
     */
    public HttpCallDispatcher(ExecutorService executorService,
                              ScheduledExecutorService scheduledExecutorService) {
        this.executorService = Objects.requireNonNull(executorService, "'executorService' is required.");
        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService,
            "'scheduledExecutorService' is required.");
    }

    /**
     * Sets the maximum number of HTTP calls to run concurrently.
     *
     * <p>
     * Calls beyond this value will be stored in-memory queue waiting for running calls to complete.
     * </p>
     *
     * @param maxCalls The maximum number of HTTP calls to run concurrently.
     * @throws IllegalArgumentException if value of {@code maxCalls} parameter is less than 1.
     */
    public void setMaxRunningCalls(int maxCalls) {
        if (maxCalls < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The maximum number of HTTP calls to execute concurrently "
                + "must be greater than 1, provided:" + maxCalls));
        }
        synchronized (this) {
            this.maxRunningCalls = maxCalls;
        }
        this.dispatchCalls();
    }


    /**
     * Gets the maximum number of HTTP calls that can run concurrently in the dispatcher threads.
     *
     * @return The maximum number of HTTP calls that can run concurrently.
     */
    public int getMaxRunningCalls() {
        synchronized (this) {
            return this.maxRunningCalls;
        }
    }

    /**
     * Enqueue a function to perform the HTTP call on the dispatcher thread.
     *
     * @param httpCallFunction The function that perform the HTTP call when invoked.
     * @param httpRequest The HTTP request to be given to {@code httpCallFunction} when the function
     *     is invoked.
     * @param httpCallback The HTTP callback to be given to {@code httpCallFunction} to notify the
     *     result of the HTTP call.
     */
    public void enqueue(HttpCallFunction httpCallFunction,
                        HttpRequest httpRequest,
                        HttpCallback httpCallback) {
        Objects.requireNonNull(httpCallFunction, "'httpCallFunction' is required.");
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(httpCallback, "'httpCallback' is required.");

        // 1]. The most common use case of 'enqueue' is to enable the async pipeline run for an HTTP request.
        /** see {@link HttpPipelinePolicyChainImpl#beginPipelineExecution(HttpPipeline, HttpRequest, HttpCallback)} */
        //
        // 2]. Additionally, an HttpClient implementation that does not have native async support can
        // use 'enqueue' to enable async HTTP calls.
        //
        // The DispatchableCall instance that when executes puts the pipeline in "run-mode" for the first time
        // is called Root-DispatchableCall.
        //
        final DispatchableCall rootDispatchableCall = new DispatchableCall() {
            private static final String INCORRECT_POLICY_IMPL_ERROR_STR = "Error potentially due to an incorrect"
                + " policy implementation - such as executing chain.processNextPolicy|finishedProcessing multiple"
                + " times or errors got escaped (directly thrown) from a policy along with "
                + "the chain.processNextPolicy|finishedProcessing execution. ";
            private static final String MULTI_DELIVERY_ERROR_STR
                = "The pipeline run attempted to deliver the result more than once. " + INCORRECT_POLICY_IMPL_ERROR_STR;
            private String callerIdTrace = "Code:";

            @Override
            public void run() {
                httpCallFunction.apply(httpRequest, this);
            }

            @Override
            public void onSuccess(HttpResponse response) {
                final boolean isFirstDelivery = this.compareAndSet(false, true);
                try {
                    if (isFirstDelivery) {
                        httpCallback.onSuccess(response);
                    } else {
                        throw logger.logExceptionAsError(new IllegalStateException(MULTI_DELIVERY_ERROR_STR));
                    }
                } finally {
                    if (isFirstDelivery) {
                        this.markNotRunning(0);
                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                final boolean isFirstDelivery = this.compareAndSet(false, true);
                try {
                    if (isFirstDelivery) {
                        httpCallback.onError(error);
                    } else {
                        throw logger.logExceptionAsError(new IllegalStateException(MULTI_DELIVERY_ERROR_STR, error));
                    }
                } finally {
                    if (isFirstDelivery) {
                        this.markNotRunning(0);
                    }
                }
            }

            @Override
            public void markNotRunning(int callerId) {
                synchronized (this) {
                    callerIdTrace += callerId;
                    boolean wasRunning = HttpCallDispatcher.this.runningRootDispatchableCalls.remove(this);
                    if (!wasRunning) {
                        throw logger.logExceptionAsError(
                            new IllegalStateException(INCORRECT_POLICY_IMPL_ERROR_STR + callerIdTrace));
                    }
                }
                // Attempt to dispatch other waiting calls since this call is no-longer-running.
                // no-longer-running = executor-rejected-call-execution | call-paused | call-completed.
                HttpCallDispatcher.this.dispatchCalls();
            }
        };

        synchronized (this) {
            this.waitingRootDispatchableCalls.add(rootDispatchableCall);
        }
        this.dispatchCalls();
    }

    /**
     * package-private.
     *
     * Schedule a {@link HttpPipelinePolicyChain#processNextPolicy(HttpRequest, HttpCallback)} call to
     * run in the future (in any dispatcher thread).
     *
     * <p>
     * An execution of a DispatchableCall submitted by the enqueue(..) initiates a pipeline run;
     * Such a DispatchableCall that puts the pipeline in "running-mode" for the first time is
     * called Root-DispatchableCall.
     *
     * When a policy (from a pipeline in "running-mode") successfully schedules "chain.processNextPolicy()"
     * the pipeline switch to "pause-mode", the pipeline is back to "running-mode" when that scheduled
     * call runs in the future.
     *
     * The Dispatching system uses the Root-DispatchableCall (that initiated the pipeline run
     * for the first time) to track whether the pipeline is in "running-mode" or not.
     * When the pipeline switch to "pause-mode", the system uses the Root-DispatchableCall to
     * yield thread to other executable calls waiting to run, in this way pipeline in "pause-mode"
     * won't be holding a thread.
     *
     * Since chain.processNextPolicy() call is scheduled from a running call, we refer such scheduled
     * call as Nested-call.
     * </p>
     *
     * @param chain The chain to invoke {@code processNextPolicy} call on.
     * @param httpRequest The HTTP request parameter for the scheduled {@code processNextPolicy} call.
     * @param httpCallback The HTTP callback parameter for the scheduled {@code processNextPolicy} call.
     * @param delay The time from now to delay the execution of the {@code processNextPolicy} call.
     * @param timeUnit The time unit of the {@code delay}.
     */
    void scheduleProcessNextPolicy(HttpPipelinePolicyChainImpl chain,
                                   HttpRequest httpRequest,
                                   HttpCallback httpCallback,
                                   long delay,
                                   TimeUnit timeUnit) {
        Objects.requireNonNull(chain, "'chain' is required.");
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(httpCallback, "'httpCallback' is required.");
        Objects.requireNonNull(timeUnit, "'timeUnit' is required.");

        final DispatchableCall rootDispatchableCall = this.getRootDispatchableCall(chain);
        final DispatchableCall nestedDispatchableCall = new DispatchableCall(rootDispatchableCall) {
            @Override
            public void run() {
                chain.processNextPolicy(httpRequest, this);
            }

            @Override
            public void onSuccess(HttpResponse response) {
                httpCallback.onSuccess(response);
            }

            @Override
            public void onError(Throwable error) {
                httpCallback.onError(error);
            }

            @Override
            public void markNotRunning(int i) {
                // NOP
            }
        };

        boolean scheduled = false;
        try {
            this.getScheduledExecutorService().schedule(() -> {
                synchronized (HttpCallDispatcher.this) {
                    // The HttpCallDispatcher::executorService executes both Root-DispatchableCall and
                    // Nested-DispatchableCall calls. Using scheduledExecutorService to hand over
                    // the Nested-DispatchableCall to HttpCallDispatcher::executorService for execution.
                    HttpCallDispatcher.this.waitingNestedDispatchableCalls.add(nestedDispatchableCall);
                }
                HttpCallDispatcher.this.dispatchCalls();
            }, delay, timeUnit);
            scheduled = true;
        } catch (RejectedExecutionException e) {
            nestedDispatchableCall.onError(new InterruptedIOException("scheduled executor rejected").initCause(e));
        } catch (Throwable t) {
            // The ScheduledExecutorService::execute() is not supposed to throw any exception
            // other than RejectedExecutionException, but if it ever throws other exceptions,
            // let's do the cleanup and then rethrow.
            rootDispatchableCall.markNotRunning(1);
            throw logger.logExceptionAsError(new RuntimeException("ScheduledExecutorService::schedule failed.", t));
        }

        if (scheduled) {
            // Once scheduled successfully, pipeline is in "pause-mode", yield the thread to other
            // executable calls waiting to run.
            rootDispatchableCall.markNotRunning(2);
        }
    }

    /**
     * Gets the ScheduledExecutorService to schedule HTTP calls to execute on {@code executorService}
     * after a specific delay.
     *
     * @return The ScheduledExecutorService for HTTP calls with delay.
     */
    private ScheduledExecutorService getScheduledExecutorService() {
        // DCL + volatile probably works on modern java's memory model but since we want to target
        // very lower api levels (L14) we prefer 'synchronized' to avoid any race surprise.
        //
        synchronized (this) {
            if (this.scheduledExecutorService == null) {
                // If the user doesn't provide a ScheduledExecutorService, we create one with one thread.
                // We use scheduledExecutorService only to handover a scheduled call for execution to
                // executorService when the time is up. [https://stackoverflow.com/a/2336879/1473510].
                //
                // Thought: We could do some heuristic such as if the pipeline doesn't use
                // the scheduledExecutorService for a certain period, we can shut down and
                // release the one pooled thread, then recreate when needed and repeat the
                // heuristic.
                //
                this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
            }
            return this.scheduledExecutorService;
        }
    }

    /**
     * Given a chain instance of a pipeline run, return the Root-DispatchableCall for the same pipeline run.
     *
     * <p>
     * The execution of a DispatchableCall submitted by the enqueue(..) initiates the pipeline run;
     * Such a DispatchableCall that puts the pipeline in "running-mode" for the first time is called
     * it's Root-DispatchableCall.
     * </p>
     *
     * @param chain The chain belongs to a pipeline run.
     * @return The Root-DispatchableCall of the pipeline run.
     */
    private DispatchableCall getRootDispatchableCall(HttpPipelinePolicyChainImpl chain) {
        // The rootCallback is a callback decorated as Root-DispatchableCall object.
        final HttpCallback rootCallback = chain.getRootCallback();
        assert rootCallback instanceof DispatchableCall;
        DispatchableCall rootDispatchableCall = (DispatchableCall) rootCallback;
        // A rootDispatchableCall must not have root.
        assert rootDispatchableCall.rootDispatchableCall == null;
        return rootDispatchableCall;
    }


    /**
     * Attempt to dispatch calls on the dispatcher threads.
     */
    private void dispatchCalls() {
        Deque<DispatchableCall> executableCalls = new ArrayDeque<>();
        synchronized (this) {
            // Collects the calls to dispatch.

            // 1. Collects the executable Nested-DispatchableCall calls.
            //    Note: Collecting Nested-DispatchableCall calls first to have them in front of executable queue.
            while (this.runningRootDispatchableCalls.size() < this.maxRunningCalls
                && !this.waitingNestedDispatchableCalls.isEmpty()) {
                final DispatchableCall nestedCall = this.waitingNestedDispatchableCalls.poll();
                assert nestedCall.rootDispatchableCall != null;
                this.runningRootDispatchableCalls.add(nestedCall.rootDispatchableCall);
                executableCalls.add(nestedCall);
            }

            // 2. Collects the executable Root-DispatchableCall calls.
            while (this.runningRootDispatchableCalls.size() < this.maxRunningCalls
                && !this.waitingRootDispatchableCalls.isEmpty()) {
                final DispatchableCall rootCall = this.waitingRootDispatchableCalls.poll();
                this.runningRootDispatchableCalls.add(rootCall);
                executableCalls.add(rootCall);
            }
        }

        // Dispatch the collected calls on dispatcher threads.
        // Dispatching must be done outside sync-block since calling into user-code while holding
        // lock is prohibited.
        while (!executableCalls.isEmpty()) {
            final DispatchableCall call = executableCalls.poll();
            assert call != null;
            try {
                this.executorService.execute(call);
            } catch (RejectedExecutionException e) {
                call.onError(new InterruptedIOException("executor rejected").initCause(e));
            } catch (Throwable t) {
                // The ExecutorService::execute() is not supposed to throw any exception other than
                // RejectedExecutionException, but if it ever throws other exceptions, let's do the
                // cleanup and then rethrow.
                call.markNotRunning(1);
                throw logger.logExceptionAsError(new RuntimeException("ExecutorService::schedule failed.", t));
            }
        }
    }

    /**
     * Contract representing an HTTP call to execute.
     */
    @FunctionalInterface
    public interface HttpCallFunction {
        /**
         * Perform an HTTP call.
         *
         * @param httpRequest The HTTP request.
         * @param httpCallback The callback to notify the result of the HTTP call.
         */
        void apply(HttpRequest httpRequest, HttpCallback httpCallback);
    }

    /**
     * The internal type represents work for a dispatcher thread to execute (therefore implements Runnable)
     * and enables the dispatching system to hook into onSuccess|onError callbacks methods for housekeeping
     * (thus implements HttpCallback).
     */
    private abstract class DispatchableCall extends AtomicBoolean implements Runnable, HttpCallback {
        private final DispatchableCall rootDispatchableCall;

        /**
         * Creates a Root-DispatchableCall, a DispatchableCall that when executes puts the pipeline
         * in "running-mode" for the first time.
         */
        DispatchableCall() {
            this.rootDispatchableCall = null;
        }

        /**
         * Creates a Nested-DispatchableCall, a DispatchableCall that when executes invokes
         * a scheduled chain.processNextPolicy(..) call.
         *
         * <p>
         * The pipeline is considered as in "pause-mode" when a chain.processNextPolicy(..) call
         * is successfully scheduled from the pipeline run. The pipeline switch to "run-mode"
         * when the scheduled chain.processNextPolicy(..) call executes.
         * </p>
         *
         * @param rootDispatchableCall The Root-DispatchableCall representing the pipeline run
         *     from which the chain.processNextPolicy(..) call (nested call) is scheduled.
         */
        DispatchableCall(DispatchableCall rootDispatchableCall) {
            assert rootDispatchableCall != null;
            this.rootDispatchableCall = rootDispatchableCall;
        }

        /**
         * Signal that this call is no longer running, perform any housekeeping finalization.
         * no-longer-running = executor-rejected-call-execution | call-paused | call-completed.
         *
         * @param callerId The identifier of the caller, this is strictly for debugging purposes,
         *     to get an idea of the call traces when a buggy policy implementations violate single-shot-only
         *     signaling requirement in callback paradigm. Often these violations occur due to a policy
         *     invoking chain.processNextPolicy|finishedProcessing multiple times (some time serially, sometimes
         *     concurrently) or due to (multiple) escaping of errors (direct throwing) combined with single|multiple
         *     signaling. Due to the complex nature, the best we can give is a hint on incorrect user-code
         *     implementation. The {@code callerId} series is mainly for the Fx author.
         * <ul>
         *   <li>callerId:0 Caller is dispatcher's Root-DispatchableCall</li>
         *   <li>callerId:1 Call was due to executor service rejecting work submission</li>
         *   <li>callerId:2 Caller is the schedule(..) after successful scheduling of Nested-DispatchableCall</li>
         * </ul>
         */
        public abstract void markNotRunning(int callerId);
    }
}
