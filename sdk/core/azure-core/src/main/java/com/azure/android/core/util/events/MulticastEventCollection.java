package com.azure.android.core.util.events;

import com.azure.android.core.util.ExpandableStringEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MulticastEventCollection<T extends ExpandableStringEnum<T>, E> {
    Map<T, Map<String, EventHandler<E>>> eventHandlers = new HashMap<>();

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

    public void removeAllEventHandler() {
        eventHandlers.clear();
    }

    public void handleEvent(E event) {
        if (event instanceof Event) {
            handleEvent((T)((Event) event).getEventType(), event);
        } else {
            throw new IllegalArgumentException("Cannot handle objects of a type other than Event.");
        }
    }

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
