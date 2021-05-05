// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

/**
 * Event for a received read receipt.
 */
public final class ReadReceiptReceivedEvent extends ChatUserEvent {
    /**
     * The id of the last read chat message.
     */
    @JsonProperty(value = "chatMessageId")
    private String chatMessageId;

    /**
     * The timestamp when the message was read. The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ
     */
    @JsonProperty(value = "readOn")
    private OffsetDateTime readOn;

    /**
     * Gets The id of the last read chat message.
     *
     * @return Value of The id of the last read chat message.
     */
    public String getChatMessageId() {
        return chatMessageId;
    }

    /**
     * Gets The timestamp when the message was read. The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ.
     *
     * @return Value of The timestamp when the message was read.
     *         The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ.
     */
    public OffsetDateTime getReadOn() {
        return readOn;
    }

    /**
     * Sets new The id of the last read chat message.
     *
     * @param chatMessageId New value of The id of the last read chat message.
     */
    public void setChatMessageId(String chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    /**
     * Sets new The timestamp when the message was read. The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ.
     *
     * @param readOn New value of The timestamp when the message was read.
     *               The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ.
     */
    public void setReadOn(OffsetDateTime readOn) {
        this.readOn = readOn;
    }
}
