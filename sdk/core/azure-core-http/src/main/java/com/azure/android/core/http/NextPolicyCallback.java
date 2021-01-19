// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * The callback type to receive the result from the next policy in the pipeline.
 *
 * @see HttpPipelinePolicyChain#processNextPolicy(HttpRequest, NextPolicyCallback)
 */
public interface NextPolicyCallback {
    /**
     * The method that receives and intercept the {@link HttpResponse} from the next policy.
     *
     * @param response The response produced by the next policy.
     * @param completer Once the {@code onSuccess} method completes the received {@code response}
     *     interception, it must use the completer to notify the completion.
     * @return The completion state. The implementation of {@code onSuccess} must return
     *     the completion state object returned from the methods in {@code completer}.
     */
    PolicyCompleter.CompletionState onSuccess(HttpResponse response, PolicyCompleter completer);

    /**
     * The method that receives and intercept the {@code error} from the next policy.
     *
     * @param error The error produced by the next policy.
     * @param completer Once the {@code onError} method completes the received {@code error}
     *     interception, it must use the completer to notify the completion.
     * @return The completion state. The implementation of {@code onSuccess} must return
     *     the completion state object returned from the methods in {@code completer}.
     */
    PolicyCompleter.CompletionState onError(Throwable error, PolicyCompleter completer);
}
