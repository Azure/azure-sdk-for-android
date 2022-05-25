package com.azure.android.core.util.events;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(T event);
}
