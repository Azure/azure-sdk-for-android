// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;
import com.azure.android.communication.chat.signaling.properties.ChatThreadProperties;

import java.util.List;

/**
 * Event for a created chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ChatThreadCreatedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was created. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public String createdOn;

    /**
     * The properties of the thread.
     */
    public ChatThreadProperties properties;

    /**
     * The list of participants on the thread.
     */
    public List<ChatParticipant> participants;

    /**
     * The information of the user that created the chat thread.
     */
    public ChatParticipant createdBy;
}
