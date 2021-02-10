package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;

/**
 * Event for a participant added to a chat thread.
 * All chat participants receive this event, including the original sender
 */
class ParticipantsRemovedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was removed. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String removedOn;

    /**
     * The participants removed from the thread.
     */
    ChatParticipant[] participantsRemoved;

    /**
     * The information of the user that removed the chat participants.
     */
    ChatParticipant removedBy;
}
