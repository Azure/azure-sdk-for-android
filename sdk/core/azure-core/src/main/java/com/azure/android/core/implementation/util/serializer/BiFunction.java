package com.azure.android.core.implementation.util.serializer;

/**
 * Represents a function that accepts two arguments and produces a result.
 */
public interface BiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u);
}
