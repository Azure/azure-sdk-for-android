// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event for a received read receipt
 */
public class ReadReceiptReceivedEvent extends ChatUserEvent {
    /**
     * The id of the last read chat message.
     */
    @JsonProperty(value = "chatMessageId")
    private String chatMessageId;

    /**
     * The timestamp when the message was read. The timestamp is in ISO8601 format: yyyy-MM-ddTHH:mm:ssZ
     */
    @JsonProperty(value = "readOn")
    private String readOn;

    /**
     * Sets new The id of the last read chat message..
     *
     * @param chatMessageId New value of The id of the last read chat message..
     */
    public void setChatMessageId(String chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    /**
     * Sets new The timestamp when the message was read. The timestamp is in ISO8601 format: yyyy-MM-ddTHH:mm:ssZ.
     *
     * @param readOn New value of The timestamp when the message was read.
     *               The timestamp is in ISO8601 format: yyyy-MM-ddTHH:mm:ssZ.
     */
    public void setReadOn(String readOn) {
        this.readOn = readOn;
    }

    /**
     * Gets The id of the last read chat message..
     *
     * @return Value of The id of the last read chat message..
     */
    public String getChatMessageId() {
        return chatMessageId;
    }

    /**
     * Gets The timestamp when the message was read. The timestamp is in ISO8601 format: yyyy-MM-ddTHH:mm:ssZ.
     *
     * @return Value of The timestamp when the message was read.
     *         The timestamp is in ISO8601 format: yyyy-MM-ddTHH:mm:ssZ.
     */
    public String getReadOn() {
        return readOn;
    }
}
