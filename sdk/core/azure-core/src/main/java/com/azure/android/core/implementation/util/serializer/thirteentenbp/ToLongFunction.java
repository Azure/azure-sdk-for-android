package com.azure.android.core.implementation.util.serializer.thirteentenbp;

/**
 * Represents a function that accepts one argument and produces a {@code long} result.
 *
 * Very simple and stupid interface for representing functions f(T) &rarr; {@code long}.
 */
interface ToLongFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    long applyAsLong(T value);
}
