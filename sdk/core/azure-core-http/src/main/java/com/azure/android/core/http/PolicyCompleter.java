// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import java.util.Objects;

/**
 * A completer provided to {@code NextPolicyCallback.onSuccess} and {@code NextPolicyCallback.onError}
 * methods along with the result (response or error) produced from the next policy.
 * The {@code NextPolicyCallback.onSuccess} and {@code NextPolicyCallback.onError} implementations
 * must use the completer it received to notify the current policy's completion of result interception.
 *
 * @see NextPolicyCallback#onSuccess(HttpResponse, PolicyCompleter)
 * @see NextPolicyCallback#onError(Throwable, PolicyCompleter)
 */
public final class PolicyCompleter {
    private final HttpPipelinePolicyChain chain;

    // pkg-private ctr
    PolicyCompleter(HttpPipelinePolicyChain chain) {
        this.chain = Objects.requireNonNull(chain, "'chain' is required.");
    }

    /**
     * The method to notify the successful completion of result interception.
     *
     * <p>
     * This notification indicates the current policy's completion of result interception. It signals
     * that the pipeline can invoke the previous policy for it to intercept the {@code response}.
     * </p>
     *
     * @param response The response.
     * @return The completion state.
     */
    public CompletionState completed(HttpResponse response) {
        this.chain.completed(response);
        return CompletionState.INSTANCE;
    }

    /**
     * The method to notify that interception is completed with error.
     *
     * <p>
     * This notification indicates the current policy's completion of result interception. It signals
     * that the pipeline can invoke the previous policy for it to intercept the {@code error}.
     * </p>
     *
     * @param error The error.
     * @return The completion state.
     */
    public CompletionState completedError(Throwable error) {
        this.chain.completedError(error);
        return CompletionState.INSTANCE;
    }

    /**
     * Defers the execution of a previous policy's response interception until one of the completed method
     * i.e. {@link PolicyCompleter#completed(HttpResponse)} or {@link PolicyCompleter#completedError(Throwable)}
     * is called.
     *
     * @return The completion state.
     */
    public CompletionState defer() {
        return CompletionState.INSTANCE;
    }

    /**
     * The type represents {@code NextPolicyCallback.onSuccess} and {@code NextPolicyCallback.onError}
     * return value. These method implementations must return {@link CompletionState} object obtained
     * by calling one of the following methods from {@link PolicyCompleter}.
     * <p>
     *   <ul>
     *     <li>{@link PolicyCompleter#completed(HttpResponse)}
     *     <li>{@link PolicyCompleter#completedError(Throwable)}
     *     <li>{@link PolicyCompleter#defer()}
     *   </ul>
     * </p>
     */
    public static final class CompletionState {
        // pkg-private
        static final CompletionState INSTANCE = new CompletionState();

        // the only one instance and it is pkg-private INSTANCE.
        private CompletionState() {
        }
    }
}
