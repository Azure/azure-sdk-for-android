// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.RequestContext;

import java.util.concurrent.TimeUnit;

/**
 * The type that enables {@link HttpPipelinePolicy} implementations to access the {@link HttpRequest}
 * and the corresponding {@link HttpResponse} flowing through the pipeline.
 *
 * <p>
 * Additionally policy implementations uses {@link HttpPipelinePolicyChain} to:
 *   <ul>
 *     <li>signal the completion of request-response interception.
 *     <li>check whether the http call is cancelled.
 *   </ul>
 * </p>
 */
public interface HttpPipelinePolicyChain {
    /**
     * Gets the {@link HttpRequest} for the policy too intercept.
     *
     * @return The HTTP request object.
     */
    HttpRequest getRequest();

    /**
     * Gets the {@link CancellationToken} associated with the pipeline run.
     *
     * <p>
     * In policy implementation, before starting any potentially time and resource-consuming work,
     * it is recommended to check {@link CancellationToken#isCancellationRequested()} to see that
     * the user expressed lost interest in the result; if so, the implementation can finish the execution
     * by calling {@code HttpPipelinePolicyChain#finishedProcessing(new IOException("Canceled."))}.
     * </p>
     *
     * @return The cancellation token.
     */
    CancellationToken getCancellationToken();

    /**
     * Gets the context that was given to
     * {@link HttpPipeline#send(HttpRequest, RequestContext, CancellationToken, HttpCallback)} call,
     * the send call that initiated the pipeline run.
     *
     * <p>
     * The policy implementation may inspect the context for any known settings specific to the policy.
     *
     * If the policy calls into Azure SDK API that accept context, then this context
     * or a new immutable context object obtained by calling the {@link RequestContext#addData(Object, Object)}
     * on this context should be provided to the API.
     * </p>
     *
     * @return The context.
     */
    RequestContext getContext();

    /**
     * Signal that the pipeline can proceed with the execution of the next policy.
     *
     * <p>
     * A policy implementation calls this method to indicate its completion of request
     * interception and to signal that the next policy can be executed.
     * </p>
     *
     * @param request The HTTP Request.
     */
    void processNextPolicy(HttpRequest request);

    /**
     * Signal that the pipeline can proceed with the execution of the next policy.
     *
     * <p>
     * A policy implementation calls this method to indicate its completion of request
     * interception and to signal that the next policy can be executed.
     * </p>
     *
     * @param request The HTTP Request.
     * @param callback The callback to receive the {@link HttpResponse} or the error from
     *     the next policy once its completes the execution.
     */
    void processNextPolicy(HttpRequest request, NextPolicyCallback callback);

    /**
     * Signal that, after the specified delay the pipeline can proceed with the execution of the next policy.
     *
     * <p>
     * A policy implementation calls this method to indicate its completion of request
     * interception and to signal that the next policy can be executed.
     * </p>
     *
     * @param request The HTTP Request.
     * @param callback The callback to receive the {@link HttpResponse} or the error from
     *     the next policy once its completes the execution.
     * @param delay The time from now to delay the execution of next policy.
     * @param timeUnit The time unit of the {@code delay}.
     */
    void processNextPolicy(HttpRequest request, NextPolicyCallback callback, long delay, TimeUnit timeUnit);

    /**
     * Signal that the policy execution is successfully completed.
     *
     * <p>
     * A policy implementation calls this method to indicate its completion of response
     * interception and to signal that response should be given to the previous policy
     * in the pipeline.
     * </p>
     *
     * @param response The HTTP Response.
     */
    void completed(HttpResponse response);

    /**
     * Signal that the policy execution is completed with failure.
     *
     * <p>
     * A policy implementation calls this method to signal that its execution is failed
     * and the failure should be propagated to the previous policy in the pipeline.
     * </p>
     *
     * @param error The failure.
     */
    void completedError(Throwable error);
}