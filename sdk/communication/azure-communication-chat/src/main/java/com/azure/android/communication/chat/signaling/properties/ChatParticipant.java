package com.azure.android.communication.chat.signaling.properties;

/**
 * An Azure Communication chat participant.
 */
public class ChatParticipant {
    /**
     * The user identity of the event initiator.
     * in the format `8:acs...`.
     */
    CommunicationUser user;

    /**
     * The display name of the event initiator.
     */
    String displayName;

    /**
     * Time from which the chat history is shared with the chat participant.
     * The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String shareHistoryTime;
}
