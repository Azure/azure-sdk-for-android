// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

/**
 * A type to asynchronously deliver the signals (element, error, completion) from a stream.
 *
 * @param <T> The type of the stream element.
 */
@FunctionalInterface
public interface AsyncStreamHandler<T> {
    /**
     * Invoked before initiating element retrieval from the stream.
     *
     * @param cancellationToken The token to cancel the steam from producing
     *                         and delivering elements to {@code onNext(e)}.
     */
    default void onInit(CancellationToken cancellationToken) {
    }

    /**
     * The next element produced by the stream.
     *
     * @param e the element signaled.
     */
    void onNext(T e);

    /**
     * A terminal signal indicating the stream terminated due to an error.
     *
     * @param throwable the error signaled.
     */
    default void onError(Throwable throwable) {
    }

    /**
     * A terminal signal indicating the stream terminated successfully.
     */
    default void onComplete() {
    }
}
