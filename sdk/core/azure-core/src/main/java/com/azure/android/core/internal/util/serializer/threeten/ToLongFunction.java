package com.azure.android.core.internal.util.serializer.threeten;

/**
 * Represents a function that accepts one argument and produces a {@code long} result.
 *
 * Very simple interface for representing functions f(T) &rarr; {@code long}.
 */
@FunctionalInterface
interface ToLongFunction<T> {
    /**
     * Applies this function to the given argument.
     *
     * @param value The function argument.
     * @return The function result.
     */
    long applyAsLong(T value);
}
