package com.azure.android.core.internal.util.serializer.threeten;

@FunctionalInterface
interface Function<T, R> {
    R apply(T t);
}

