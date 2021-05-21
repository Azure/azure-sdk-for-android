// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The base event of chat events
 */
public abstract class BaseEvent {
    /**
     * Thread Id of the event.
     */
    @JsonProperty(value = "threadId")
    private String threadId;

    /**
     * Thread Id of the event. Named as groupId in notification payload for message events.
     */
    @JsonProperty(value = "groupId", access = JsonProperty.Access.WRITE_ONLY)
    private String groupId;

    /**
     * Gets Thread Id of the event.
     *
     * @return Value of Thread Id of the event.
     */
    public String getThreadId() {
        return threadId;
    }

    /**
     * Sets Thread Id of the event.
     */
    BaseEvent setThreadId() {
        this.threadId = groupId;
        return this;
    }
}
