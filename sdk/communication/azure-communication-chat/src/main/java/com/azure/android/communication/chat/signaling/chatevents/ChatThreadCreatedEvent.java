package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;
import com.azure.android.communication.chat.signaling.properties.ChatThreadProperties;

/**
 * Event for a created chat thread.
 * All chat participants receive this event, including the original sender
 */
class ChatThreadCreatedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was created. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String createdOn;

    /**
     * The properties of the thread.
     */
    ChatThreadProperties properties;

    /**
     * The list of participants on the thread.
     */
    ChatParticipant[] participants;

    /**
     * The information of the user that created the chat thread.
     */
    ChatParticipant createdBy;
}
