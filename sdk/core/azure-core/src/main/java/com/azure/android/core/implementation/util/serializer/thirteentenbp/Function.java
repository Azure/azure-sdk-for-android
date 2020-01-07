package com.azure.android.core.implementation.util.serializer.thirteentenbp;

@FunctionalInterface
interface Function<T, R> {
    R apply(T t);
}

