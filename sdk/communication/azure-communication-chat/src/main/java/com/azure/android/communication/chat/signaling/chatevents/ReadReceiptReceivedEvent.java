package com.azure.android.communication.chat.signaling.chatevents;

/**
 * Event for a received read receipt
 */
class ReadReceiptReceivedEvent extends BaseEvent {
    /**
     * The id of the last read chat message.
     */
    String chatMessageId;

    /**
     * The timestamp when the message was read. The timestamp is in ISO8601 format: yyyy-MM-ddTHH:mm:ssZ
     */
    String readOn;
}
