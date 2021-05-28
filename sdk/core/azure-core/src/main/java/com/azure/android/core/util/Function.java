// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

/**
 * The type representing a function that accept one input and produces a result.
 *
 * @param <I> Type of the input.
 * @param <O> Type of the output.
 */
@FunctionalInterface
public interface Function<I, O> {
    /**
     * Invokes the function.
     *
     * @param input The input.
     * @return The output.
     */
    O call(I input);
}
