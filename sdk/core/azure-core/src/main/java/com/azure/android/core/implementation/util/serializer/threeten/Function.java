package com.azure.android.core.implementation.util.serializer.threeten;

@FunctionalInterface
interface Function<T, R> {
    R apply(T t);
}

