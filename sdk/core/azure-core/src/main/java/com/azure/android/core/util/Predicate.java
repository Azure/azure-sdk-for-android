// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

/**
 * A type representing a predicate to test.
 *
 * @param <I> The type of the input to use when testing the predicate.
 */
public interface Predicate<I> {
    /**
     * Evaluates this predicate on the given input.
     *
     * @param input The input to use when testing the predicate.
     * @return true if the testing succeeded, false otherwise.
     */
    boolean test(I input);
}
