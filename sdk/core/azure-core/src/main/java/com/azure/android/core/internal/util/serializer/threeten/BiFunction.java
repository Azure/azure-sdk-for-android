/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.android.core.internal.util.serializer.threeten;

/**
 * Represents a function that accepts two arguments and produces a result.
 */
@FunctionalInterface
interface BiFunction<T, U, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t The first argument of the function.
     * @param u The second argument of the function.
     * @return The function result.
     */
    R apply(T t, U u);
}
