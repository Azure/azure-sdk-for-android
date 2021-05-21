// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

/**
 * A sequence of elements that can be enumerated asynchronously.
 *
 * @param <T> The element type.
 */
public interface AsyncStream<T> {
    /**
     * Enumerate the {@link AsyncStream} by signaling each element to the {@code handler.onNext}.
     *
     * All the elements will be enumerated as long as there is no cancellation requested and
     * there is no error while retrieving the element (e.g. auth error, network error).
     *
     * @param handler The handler to receive result of enumeration.
     * @return CancellationToken to request the cancellation of enumeration.
     */
    CancellationToken forEach(AsyncStreamHandler<T> handler);
}
