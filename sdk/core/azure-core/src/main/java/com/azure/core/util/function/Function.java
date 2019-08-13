package com.azure.core.util.function;

public interface Function<T, R> {
    R apply(T t);
}
