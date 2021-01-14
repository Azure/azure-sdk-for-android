// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import android.util.Log;

import com.azure.android.core.micro.util.CancellationToken;
import com.azure.android.core.micro.util.Context;
import com.azure.core.logging.ClientLogger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link HttpPipelinePolicyChain}.
 */
final class HttpPipelinePolicyChainImpl implements HttpPipelinePolicyChain {
    private static final String TAG = HttpPipelinePolicyChainImpl.class.getName();
    private final ClientLogger logger = new ClientLogger(HttpPipelinePolicyChainImpl.class);

    private final int index;
    private final HttpPipeline httpPipeline;
    private final HttpRequest httpRequest;
    private final Context context;
    private final CancellationToken cancellationToken;
    private final HttpPipelinePolicyChainImpl prevChain;
    private final HttpCallback prevProceedCallback;
    private volatile boolean reportedBypassedError;

    /**
     * package-private.
     *
     * Begin the pipeline execution by using {@link HttpCallDispatcher} to run it asynchronously.
     *
     * @param httpPipeline The HTTP pipeline.
     * @param httpRequest The HTTP request to flow through the pipeline.
     * @param context The context to flow through the pipeline.
     * @param cancellationToken The cancellation token for the pipeline execution.
     * @param pipelineSendCallback The callback to invoke once the execution of the pipeline completes.
     */
    static void beginPipelineExecution(HttpPipeline httpPipeline,
                                       HttpRequest httpRequest,
                                       Context context,
                                       CancellationToken cancellationToken,
                                       HttpCallback pipelineSendCallback) {
        Objects.requireNonNull(httpPipeline, "'httpPipeline' is required.");
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(context, "'context' is required.");
        Objects.requireNonNull(cancellationToken, "'cancellationToken' is required.");
        Objects.requireNonNull(pipelineSendCallback, "'pipelineSendCallback' is required.");

        final HttpCallDispatcher.HttpCallFunction httpCallFunction = (request, rootCallback) -> {
            // The 'rootCallback' is a decorated callback (aka root-DispatchableCall) created by
            // HttpCallDispatcher to receive result (response|error) from the first policy and handover
            // that result to the 'pipelineSendCallback' along with dispatcher specific housekeeping.
            final HttpPipelinePolicyChainImpl rootChain = new HttpPipelinePolicyChainImpl(-1,
                httpPipeline,
                request,
                context,
                cancellationToken,
                null,
                rootCallback);
            rootChain.processNextPolicyIntern(request, rootChain.context, rootCallback);
        };

        httpPipeline.httpCallDispatcher.enqueue(httpCallFunction,
            httpRequest,
            cancellationToken,
            pipelineSendCallback);
    }

    /**
     * Creates a chain for the policy at {@code index}.
     *
     * <p>
     * Once the policy at {@code index} completes its execution by calling chain.completed(..),
     * the result will be notified to {@code prevProceedCallback}. This callback can be absent
     * if the previous policy used {@code proceed(..)} with no callback param, in such case
     * {@code complete(..)} method of previous chain {@code prevChain} will be called.
     * </p>
     *
     * @param index The index of the policy that uses this chain.
     * @param httpPipeline The HTTP Pipeline.
     * @param httpRequest The HTTP request to flow through the pipeline.
     * @param context The context to flow through the pipeline.
     * @param cancellationToken cancellationToken for the pipeline run this chain belongs to.
     * @param prevChain The reference to previous chain (chain for the policy at {@code index - 1}).
     * @param prevProceedCallback The reference to the callback provided to the {@code proceed(..)} method
     *     of the previous policy.
     */
    private HttpPipelinePolicyChainImpl(int index,
                                        HttpPipeline httpPipeline,
                                        HttpRequest httpRequest,
                                        Context context,
                                        CancellationToken cancellationToken,
                                        HttpPipelinePolicyChainImpl prevChain,
                                        HttpCallback prevProceedCallback) {
        // Private Ctr, hence simple assertion.
        assert (httpPipeline != null
            && httpRequest != null
            && context != null
            && cancellationToken != null
            && (prevChain != null || prevProceedCallback != null));

        this.index = index;
        this.httpPipeline = httpPipeline;
        this.httpRequest = httpRequest;
        this.context = context;
        this.cancellationToken = cancellationToken;
        this.prevChain = prevChain;
        this.prevProceedCallback = prevProceedCallback;
    }

    @Override
    public HttpRequest getRequest() {
        return this.httpRequest;
    }

    @Override
    public CancellationToken getCancellationToken() {
        return this.cancellationToken;
    }

    @Override
    public void processNextPolicy(HttpRequest httpRequest) {
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        this.processNextPolicyIntern(httpRequest, this.context, null);
    }

    @Override
    public void processNextPolicy(HttpRequest httpRequest, HttpCallback httpCallback) {
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(httpCallback, "'httpCallback' is required.");
        this.processNextPolicyIntern(httpRequest, this.context, httpCallback);
    }

    @Override
    public void processNextPolicy(HttpRequest request, Context context, HttpCallback httpCallback) {
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(context, "'context' is required.");
        Objects.requireNonNull(httpCallback, "'httpCallback' is required.");
        this.processNextPolicyIntern(httpRequest, context, httpCallback);
    }

    @Override
    public void processNextPolicy(HttpRequest httpRequest, HttpCallback httpCallback,
                                  long delay, TimeUnit timeUnit) {
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(httpCallback, "'httpCallback' is required.");
        Objects.requireNonNull(timeUnit, "'timeUnit' is required.");
        this.httpPipeline.httpCallDispatcher.scheduleProcessNextPolicy(this,
            httpRequest,
            this.context,
            httpCallback,
            delay,
            timeUnit);
    }

    @Override
    public void processNextPolicy(HttpRequest httpRequest, Context context, HttpCallback httpCallback,
                                  long delay, TimeUnit timeUnit) {
        Objects.requireNonNull(httpRequest, "'httpRequest' is required.");
        Objects.requireNonNull(context, "'context' is required.");
        Objects.requireNonNull(httpCallback, "'httpCallback' is required.");
        Objects.requireNonNull(timeUnit, "'timeUnit' is required.");
        this.httpPipeline.httpCallDispatcher.scheduleProcessNextPolicy(this,
            httpRequest,
            context,
            httpCallback,
            delay,
            timeUnit);
    }

    @Override
    public void finishedProcessing(HttpResponse httpResponse) {
        Objects.requireNonNull(httpResponse, "'httpResponse' is required.");
        if (this.prevProceedCallback != null) {
            try {
                this.prevProceedCallback.onSuccess(httpResponse);
            } catch (Throwable t) {
                this.reportBypassedError(t, true);
            }
        } else {
            // A callback to notify the previous policy could be null if that policy
            // implementation used 'processNextPolicy(HttpRequest httpRequest)'. A delegating
            // callback also works but avoiding an extra allocation by using previous
            // chain reference.
            assert (this.prevChain != null);
            this.prevChain.finishedProcessing(httpResponse);
        }
    }

    @Override
    public void finishedProcessing(Throwable error) {
        Objects.requireNonNull(error, "'throwable' is required.");
        if (this.prevProceedCallback != null) {
            try {
                this.prevProceedCallback.onError(error);
            } catch (Throwable t) {
                this.reportBypassedError(t, true);
            }
        } else {
            assert (this.prevChain != null);
            this.prevChain.finishedProcessing(error);
        }
    }

    /**
     * Proceed with execution of policy at {@code index + 1}.
     * <p>
     * If current policy (policy at {@code index}) is the last policy then the request will be
     * given to the HTTP Client for execution.
     * </p>
     *
     * @param httpRequest The HTTP request for the next policy.
     * @param context The HTTP context for the next policy.
     * @param proceedCallback The current policy's callback (policy at {@code index})
     *     that next policy notify results to.
     */
    private void processNextPolicyIntern(HttpRequest httpRequest, Context context, HttpCallback proceedCallback) {
        final int nextIndex = this.index + 1;
        assert nextIndex >= 0;

        // Create a chain for next policy.
        final HttpPipelinePolicyChainImpl nextChain = new HttpPipelinePolicyChainImpl(nextIndex,
            this.httpPipeline,
            httpRequest,
            context,
            this.cancellationToken,
            this,
            proceedCallback);

        if (nextIndex == this.httpPipeline.size) {
            try {
                // No more policies, invoke the network-policy to write the request to the wire.
                this.httpPipeline.networkPolicy.process(nextChain, nextChain.context);
            } catch (Throwable t) {
                this.reportBypassedError(t, false);
            }
        } else {
            try {
                // Invoke the next pipeline policy at this.index + 1.
                this.httpPipeline.getPolicy(nextIndex).process(nextChain, nextChain.context);
            } catch (Throwable t) {
                this.reportBypassedError(t, false);
            }
        }
    }

    /**
     * Report the given bypassed error.
     *
     * Bypassed error is an error directly 'thrown' from following sources:
     * <ul>
     *     <li> policy.process(..)
     *     <li> onSuccess(..) or onError(..) methods of the callback provided to chain.processNextPolicy(..) call.
     * </ul>
     *
     * <p>
     * Ideally, the user-code in these sources is supposed to propagate any error using chain.completed(Throwable)
     * instead of throwing. Error bypassed via throw represents incorrect/missed error handling in user-code.
     * This method notifies such error to "rootCallback".
     *
     * The "rootCallback" is the callback that receives result from the first policy when that policy call
     * chain.complete(..). If a bypassed error appears in the pipeline, we "short circuit" the pipeline chain
     * and report error to "rootCallback". The "rootCallback" is designed to delegates the received result
     * (response|error) to the callback that was provided to
     * {@link HttpPipeline#send(HttpRequest, Context, CancellationToken, HttpCallback)} and
     * to take care of dispatcher specific housekeeping.
     *
     * If an attempt to report a bypassed error e1 results in another bypassed error e2, we log e2 and re-throw e2.
     * </p>
     *
     * @param bypassedError The bypassed error.
     * @param isErrorFromProceedCallback true if the error is bypassed from onSuccess(..) or onError(..) of
     *     a proceedCallback, false if the error is bypassed from policy.process(..).
     */
    private void reportBypassedError(Throwable bypassedError, boolean isErrorFromProceedCallback) {
        HttpPipelinePolicyChainImpl rootChain = this.getRootChain();
        if (rootChain.reportedBypassedError) {
            // We processed escaped error once; after that, any more escaped errors will be rethrown.
            //
            Log.e(TAG, "Error escaped.", bypassedError);
            throw logger.logExceptionAsError(new RuntimeException(bypassedError.getMessage(), bypassedError));
        } else {
            rootChain.reportedBypassedError = true;
            HttpCallback rootCallback = rootChain.prevProceedCallback;
            if (isErrorFromProceedCallback) {
                if (this.index > 0) {
                    try {
                        rootCallback.onError(bypassedError);
                    } catch (Throwable t) {
                        // :( an error bypassed from the rootCallback.onError(e)
                        Log.e(TAG, "Error escaped from RootCallback::onError(e).", t);
                        throw logger.logExceptionAsError(
                            new RuntimeException("Error escaped from RootCallback::onError(e).", t));
                    }
                } else {
                    assert this.index == 0;
                    // :( an error bypassed from the rootCallback.onError(e)|onSuccess(r)
                    Log.e(TAG, "Error escaped from RootCallback::onError(e)|onSuccess(r).",
                        bypassedError);
                    throw logger.logExceptionAsError(new RuntimeException(bypassedError.getMessage(), bypassedError));
                }
            } else {
                try {
                    rootCallback.onError(bypassedError);
                } catch (Throwable t) {
                    Log.e(TAG, "Error escaped from RootCallback::onError(e).", t);
                    throw logger.logExceptionAsError(
                        new RuntimeException("Error escaped from RootCallback::onError(e).", t));
                }
            }
        }
    }

    /**
     * Retrieve the root chain with index -1.
     *
     * @return The root chain.
     */
    private HttpPipelinePolicyChainImpl getRootChain() {
        HttpPipelinePolicyChainImpl chain = this;
        while (chain.index != -1) {
            chain = chain.prevChain;
            assert chain != null;
        }
        assert chain.index == -1;
        return chain;
    }

    /**
     * package-private.
     *
     * Retrieve the "rootCallback". The "rootCallback" is the callback that receives result from
     * the first policy when that policy call chain.complete(..).
     * <p>
     * The "rootCallback" is designed to delegates the received result (response|error) to the callback
     * that was provided to {@link HttpPipeline#send(HttpRequest, Context, CancellationToken, HttpCallback)}
     * and to take care of dispatcher specific housekeeping.
     * </p>
     *
     * @return The "rootCallback".
     */
    HttpCallback getRootCallback() {
        assert this.index >= 0;
        HttpPipelinePolicyChainImpl rootChain = this.getRootChain();
        return rootChain.prevProceedCallback;
    }
}
