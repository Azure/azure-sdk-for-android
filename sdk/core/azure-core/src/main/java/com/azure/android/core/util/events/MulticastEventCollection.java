// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that maps event types with handlers for events of said types. An event type can be mapped to multiple handlers
 * if necessary.
 *
 * @param <T> The event type.
 * @param <E> The event itself.
 */
public class MulticastEventCollection<T, E extends Event> {
    private final Map<T, Set<EventHandler<E>>> eventHandlers = new HashMap<>();

    /**
     * Adds an event handler mapped to the event type provided.
     *
     * @param eventType The type of the event to map the handler to.
     * @param eventHandler The handler to map to the event type.
     */
    @SuppressWarnings("ConstantConditions")
    public void addEventHandler(T eventType, EventHandler<? extends Event> eventHandler) {
        Set<EventHandler<E>> handlers;

        if (this.eventHandlers.containsKey(eventType)) {
            handlers = this.eventHandlers.get(eventType);
        } else {
            handlers = new HashSet<>();
        }

        handlers.add((EventHandler<E>) eventHandler);
        this.eventHandlers.put(eventType, handlers);
    }

    /**
     * Removes a specific handler for the given event type.
     *
     * @param eventType The type of the event the handler is mapped to.
     * @param eventHandler The handler to remove.
     */
    @SuppressWarnings("ConstantConditions")
    public void removeEventHandler(T eventType, EventHandler<? extends Event> eventHandler) {
        if (this.eventHandlers.containsKey(eventType)) {
            Set<EventHandler<E>> handlers = this.eventHandlers.get(eventType);

            handlers.remove(eventHandler);

            if (handlers.isEmpty()) {
                this.eventHandlers.remove(eventType);
            } else {
                this.eventHandlers.put(eventType, handlers);
            }
        }
    }

    /**
     * Removes all event handlers for a given event type.
     *
     * @param eventType The type of the event to remove all handlers of.
     */
    public void removeAllEventHandlers(T eventType) {
        if (this.eventHandlers.containsKey(eventType)) {
            Set<EventHandler<E>> handlers = this.eventHandlers.get(eventType);

            if (handlers != null) {
                this.eventHandlers.remove(eventType);
            }
        }
    }

    /**
     * Removes all event handlers.
     */
    public void removeAllEventHandlers() {
        eventHandlers.clear();
    }

    /**
     * Calls the {@code handle} method in all handlers for a given event.
     *
     * @param eventType The type of the event to handle.
     * @param event The event to handle.
     */
    public void handleEvent(T eventType, E event) {
        if (this.eventHandlers.containsKey(eventType)) {
            Set<EventHandler<E>> handlers = this.eventHandlers.get(eventType);

            if (handlers != null) {
                for (EventHandler<E> eventHandler : handlers) {
                    eventHandler.handle(event);
                }
            }
        }
    }
}
