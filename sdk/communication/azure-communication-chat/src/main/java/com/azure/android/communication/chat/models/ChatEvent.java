// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The base class of chat events
 */
public abstract class ChatEvent {
    /**
     * Chat Thread Id of the event.
     */
    @JsonProperty(value = "threadId")
    private String threadId;

    /**
     * Chat Thread Id of the event. Named as groupId in notification payload for message events.
     */
    @JsonProperty(value = "groupId", access = JsonProperty.Access.WRITE_ONLY)
    private String groupId;

    /**
     * Gets chat thread Id of the event.
     *
     * @return Value of chat thread Id of the event.
     */
    public String getChatThreadId() {
        return threadId;
    }

    /**
     * Sets Thread Id of the event.
     */
    ChatEvent setThreadId() {
        this.threadId = groupId;
        return this;
    }
}
