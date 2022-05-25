// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.events;

import com.azure.android.core.util.ExpandableStringEnum;

/**
 * Base implementation for an event. Extend this class if you need to bundle event details together with an event type.
 *
 * @param <T> The event type.
 */
public abstract class Event<T extends ExpandableStringEnum<T>> {
    final T eventType;

    /**
     * Base constructor that requires an type for the event.
     *
     * @param eventType The type of the event.
     */
    Event(T eventType) {
        this.eventType = eventType;
    }

    /**
     * Get the type of the event.
     *
     * @return The type of the event.
     */
    public T getEventType() {
        return eventType;
    }
}
