package com.azure.core.implementation.util.function;

public interface Function<T, R> {
    R apply(T t);
}
