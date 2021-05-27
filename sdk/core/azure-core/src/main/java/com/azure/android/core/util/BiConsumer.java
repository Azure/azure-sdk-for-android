// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

/**
 * Type representing an operation that accepts two input arguments.
 *
 * @param <T> The type of the first input.
 * @param <U> The type of the second input.
 */
public interface BiConsumer<T, U> {
    /**
     * Performs the operation on the given two inputs.
     *
     * @param t The first input.
     * @param u The second input.
     */
    void accept(T t, U u);
}
