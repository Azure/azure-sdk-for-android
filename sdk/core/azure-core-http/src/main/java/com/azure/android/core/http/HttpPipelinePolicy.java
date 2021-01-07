// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
public interface HttpPipelinePolicy {
    /**
     * Applies the policy to a {@link HttpRequest} and the corresponding {@link HttpResponse}.
     * <p>
     * Policy implementations are expected to use {@link HttpPipelinePolicyChain#getRequest()} to access and intercept
     * the request before calling {@link HttpPipelinePolicyChain#processNextPolicy(HttpRequest, HttpCallback)},
     * and intercept the response given to the {@code HttpCallback} before calling
     * {@link HttpPipelinePolicyChain#finishedProcessing(HttpResponse)} or
     * {@link HttpPipelinePolicyChain#finishedProcessing(Throwable)}.
     * </p>
     *
     * @param chain The chain.
     */
    void process(HttpPipelinePolicyChain chain);
}
