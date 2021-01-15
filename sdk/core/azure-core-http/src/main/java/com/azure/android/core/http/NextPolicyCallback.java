// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import java.util.Objects;

/**
 * The callback type to receive the result from the next policy in the pipeline.
 *
 * @see HttpPipelinePolicyChain#processNextPolicy(HttpRequest, NextPolicyCallback)
 *
 */
public interface NextPolicyCallback {
    /**
     * The method that receives and intercept the {@link HttpResponse} from the next policy.
     *
     * @param response The response produced by the next policy.
     * @param notifyCompletion Once the {@code onSuccess} method completes the received
     *     {@code response} interception, it must use the notifyCompletion to notify
     *     the completion.
     */
    void onSuccess(HttpResponse response, NotifyCompletion notifyCompletion);

    /**
     * The method that receives and intercept the {@code error} from the next policy.
     *
     * @param error The error produced by the next policy.
     * @param notifyCompletion Once the {@code onError} method completes the received
     *     {@code error} interception, it must use the notifyCompletion to notify
     *     the completion.
     */
    void onError(Throwable error, NotifyCompletion notifyCompletion);

    /**
     * A handler provided to {@code NextPolicyCallback.onSuccess} and {@code NextPolicyCallback.onError}
     * methods along with response or error from the next policy, these methods uses the received notifier
     * to notify the completion of interception of {@code response} or {@code error}] it received.
     */
    final class NotifyCompletion {
        private final HttpPipelinePolicyChain chain;

        // pkg-private ctr
        NotifyCompletion(HttpPipelinePolicyChain chain) {
            Objects.requireNonNull(chain, "'chain' is required.");
            this.chain = chain;
        }

        /**
         * The method to notify that interception is successfully completed.
         *
         * <p>
         * This notification indicates the completion of interception of result received
         * from the next policy and signal that {@code response} should be given to
         * the previous policy in the pipeline.
         * </p>
         *
         * @param response The response.
         */
        public void onCompleted(HttpResponse response) {
            this.chain.onCompleted(response);
        }

        /**
         * The method to notify that interception is completed with error.
         *
         * <p>
         * This notification indicates the completion of interception of result received
         * from the next policy and signal that execution is failed and the error should
         * be propagated to the previous policy in the pipeline.
         * </p>
         *
         * @param error The error.
         */
        public void onCompleted(Throwable error) {
            this.chain.onCompleted(error);
        }
    }
}
