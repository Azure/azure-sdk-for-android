// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.events;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.ExpandableStringEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class that maps event types with handlers for events of said types. An event type can be mapped to multiple handlers
 * if necessary.
 *
 * @param <T> The event type.
 * @param <E> The event itself.
 */
public class MulticastEventCollection<T, E extends Event> {
    private final Map<T, Map<String, EventHandler<E>>> eventHandlers = new HashMap<>();
    private final ClientLogger logger = new ClientLogger(MulticastEventCollection.class);

    /**
     * Adds an event handler mapped to the event type provided.
     *
     * @param eventType The type of the event to map the handler to.
     * @param eventHandler The handler to map to the event type.
     *
     * @return A unique ID that identifies the mapping.
     */
    public String addEventHandler(T eventType, EventHandler<E> eventHandler) {
        Map<String, EventHandler<E>> handlers;

        if (this.eventHandlers.containsKey(eventType)) {
            handlers = this.eventHandlers.get(eventType);
        } else {
            handlers = new HashMap<>();
        }

        String uuid = UUID.randomUUID().toString();

        handlers.put(uuid, eventHandler);
        this.eventHandlers.put(eventType, handlers);

        return uuid;
    }

    /**
     * Removes a specific handler for the given event type.
     *
     * @param eventType The type of the event the handler is mapped to.
     * @param handlerId The unique ID of the handler to remove.
     */
    public void removeEventHandler(T eventType, String handlerId) {
        if (this.eventHandlers.containsKey(eventType)) {
            Map<String, EventHandler<E>> handlers = this.eventHandlers.get(eventType);

            handlers.remove(handlerId);

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
            Map<String, EventHandler<E>> handlers = this.eventHandlers.get(eventType);

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
            Map<String, EventHandler<E>> handlers = this.eventHandlers.get(eventType);

            if (handlers != null) {
                for (EventHandler<E> eventHandler : handlers.values()) {
                    eventHandler.handle(event);
                }
            }
        }
    }
}
