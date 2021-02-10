package com.azure.android.communication.chat.signaling.chatevents;

/**
 * Event for a received chat message.
 * All chat participants receive this event, including the original sender
 */
public class ChatMessageReceivedEvent extends BaseEvent {
    /**
     * Type of the chat message.
     * The only type currently supported is Text
     */
    String type;

    /**
     * Content of the message.
     */
    String content;

    /**
     * Priority of the message. Possible values include: 'Normal', 'High'
     */
    String priority;

    /**
     * The Id of the message. This Id is server generated.
     */
    String id;

    /**
     * The display name of the event sender.
     */
    String senderDisplayName;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String createdOn;

    /**
     * Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    String version;
}
