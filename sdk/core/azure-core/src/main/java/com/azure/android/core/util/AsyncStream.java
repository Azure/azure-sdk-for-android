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
     * Enumerate the {@link AsyncStream} by signaling each to the {@code handler.onNext}.
     *
     * All the elements will be enumerated as long as there is no cancellation requested and
     * there is no error while retrieving the element (e.g. auth error, network error).
     *
     * The {@code CancellationToken} returned can be used to cancel the enumeration
     *
     * @param handler The enumeration handler.
     * @return CancellationToken to signal the enumeration cancel.
     */
    CancellationToken forEach(AsyncStreamHandler<T> handler);
}
