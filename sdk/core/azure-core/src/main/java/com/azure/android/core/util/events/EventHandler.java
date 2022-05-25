// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.events;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(T event);
}
