// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;

/**
 * Event for an updated chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ChatThreadDeletedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public String deletedOn;

    /**
     * The information of the user that deleted the chat thread.
     */
    public ChatParticipant deletedBy;
}
