// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;
import com.azure.android.communication.chat.signaling.properties.ChatThreadProperties;

/**
 * Event for an updated chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ChatThreadPropertiesUpdatedEvent extends ChatThreadEvent {
    /**
     * The properties of the thread.
     */
    public ChatThreadProperties properties;

    /**
     * The timestamp when the thread was updated. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public String updatedOn;

    /**
     * The information of the user that updated the chat thread.
     */
    public ChatParticipant updatedBy;
}
