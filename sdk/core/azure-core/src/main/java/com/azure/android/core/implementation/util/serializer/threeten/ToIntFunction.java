package com.azure.android.core.implementation.util.serializer.threeten;

/**
 * Represents a function that accepts one argument and produces an {@code int} result.
 *
 * Very simple and stupid interface for representing functions f(T) &rarr; {@code int}.
 */
interface ToIntFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    int applyAsInt(T value);
}
