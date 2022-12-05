package com.azure.android.core.serde.jackson.implementation.threeten;

/**
 * Represents a function that accepts one argument and produces a result.
 */
@FunctionalInterface
interface Function<T, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t The function argument.
     * @return The function result.
     */
    R apply(T t);
}