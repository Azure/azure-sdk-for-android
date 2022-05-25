package com.azure.android.core.util.events;

import com.azure.android.core.util.ExpandableStringEnum;

public class Event<T extends ExpandableStringEnum<T>> {
    T eventType;

    Event(T eventType) {
        this.eventType = eventType;
    }

    public T getEventType() {
        return eventType;
    }
}
