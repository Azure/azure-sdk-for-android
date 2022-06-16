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
 */
@SuppressWarnings("rawtypes")
public class EventHandlerCollection {
    private final Map<String, Set<EventHandler>> eventHandlers = new HashMap<>();

    /**
     * Adds an event handler mapped to the event type provided.
     *
     * @param eventType The type of the event to map the handler to.
     * @param eventHandler The handler to map to the event type.
     */
    @SuppressWarnings("ConstantConditions")
    public void addEventHandler(String eventType, EventHandler eventHandler) {
        Set<EventHandler> handlers;

        if (this.eventHandlers.containsKey(eventType)) {
            handlers = this.eventHandlers.get(eventType);
        } else {
            handlers = new HashSet<>();
        }

        handlers.add(eventHandler);
        this.eventHandlers.put(eventType, handlers);
    }

    /**
     * Removes a specific handler for the given event type.
     *
     * @param eventType The type of the event the handler is mapped to.
     * @param eventHandler The handler to remove.
     */
    @SuppressWarnings("ConstantConditions")
    public void removeEventHandler(String eventType, EventHandler eventHandler) {
        if (this.eventHandlers.containsKey(eventType)) {
            Set<EventHandler> handlers = this.eventHandlers.get(eventType);

            handlers.remove(eventHandler);

            if (handlers.isEmpty()) {
                this.eventHandlers.remove(eventType);
            } else {
                this.eventHandlers.put(eventType, handlers); // Do we need to put this again? -vcolin7
            }
        }
    }

    /**
     * Calls the {@code handle} method in all handlers for a given event.
     *
     * @param eventType The type of the event to handle.
     * @param event The event to handle.
     */
    @SuppressWarnings("unchecked")
    public void fireEvent(String eventType, Object event) {
        if (this.eventHandlers.containsKey(eventType)) {
            Set<EventHandler> handlers = this.eventHandlers.get(eventType);

            if (handlers != null) {
                for (EventHandler eventHandler : handlers) {
                    eventHandler.handle(event);
                }
            }
        }
    }
}
