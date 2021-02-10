package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;

/**
 * Event for participants added to a chat thread.
 * All chat participants receive this event, including the original sender
 */
class ParticipantsAddedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was added. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String addedOn;

    /**
     * The participants added to the thread.
     */
    ChatParticipant[] participantsAdded;

    /**
     * The information of the user that added the chat participants.
     */
    ChatParticipant addedBy;
}
