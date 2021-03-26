// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;

import java.util.List;

/**
 * Event for a participant added to a chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ParticipantsRemovedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was removed. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public String removedOn;

    /**
     * The participants removed from the thread.
     */
    public List<ChatParticipant> participantsRemoved;

    /**
     * The information of the user that removed the chat participants.
     */
    public ChatParticipant removedBy;
}
