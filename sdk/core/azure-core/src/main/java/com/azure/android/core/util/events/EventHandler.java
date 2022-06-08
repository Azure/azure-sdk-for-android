// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.events;

/**
 * A simple event handler.
 *
 * @param <E> The event to handle.
 */
@FunctionalInterface
public interface EventHandler<E extends Event> {
    /**
     * Handles a given event.
     *
     * @param event The event to handle.
     */
    void handle(E event);
}
