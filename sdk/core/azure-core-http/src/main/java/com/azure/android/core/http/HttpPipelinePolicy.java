// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.http.NextPolicyCallback.NotifyCompletion;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
public interface HttpPipelinePolicy {
    /**
     * Applies the policy to a {@link HttpRequest} and the corresponding {@link HttpResponse}.
     *
     * <p>
     * Policy implementations are expected to use {@link HttpPipelinePolicyChain#getRequest()}
     * to access and intercept the request, then proceed to next policy by calling
     * {@link HttpPipelinePolicyChain#processNextPolicy(HttpRequest)} or
     * {@link HttpPipelinePolicyChain#processNextPolicy(HttpRequest, NextPolicyCallback)}.
     *
     * The policy can intercept the resulting response notified to
     * {@link NextPolicyCallback#onSuccess(HttpResponse, NotifyCompletion)}
     *  or error notified to {@link NextPolicyCallback#onError(Throwable, NotifyCompletion)}
     * and must signal the completion of the policy by calling
     * {@link NotifyCompletion#onCompleted(HttpResponse)} or
     * {@link NotifyCompletion#onCompleted(Throwable)}.
     * </p>
     *
     * @param chain The chain for the policy to access the request and response.
     */
    void process(HttpPipelinePolicyChain chain);
}
