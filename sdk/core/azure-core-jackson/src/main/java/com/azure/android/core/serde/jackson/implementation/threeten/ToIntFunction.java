package com.azure.android.core.serde.jackson.implementation.threeten;

/**
 * Represents a function that accepts one argument and produces an {@code int} result.
 *
 * Very simple interface for representing functions f(T) &rarr; {@code int}.
 */
@FunctionalInterface
interface ToIntFunction<T> {
    /**
     * Applies this function to the given argument.
     *
     * @param value The function argument.
     * @return The function result.
     */
    int applyAsInt(T value);
}