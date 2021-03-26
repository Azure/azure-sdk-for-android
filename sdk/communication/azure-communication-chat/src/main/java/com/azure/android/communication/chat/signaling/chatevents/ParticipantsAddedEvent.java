// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;

import java.util.List;

/**
 * Event for participants added to a chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ParticipantsAddedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was added. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public String addedOn;

    /**
     * The participants added to the thread.
     */
    public List<ChatParticipant> participantsAdded;

    /**
     * The information of the user that added the chat participants.
     */
    public ChatParticipant addedBy;
}
