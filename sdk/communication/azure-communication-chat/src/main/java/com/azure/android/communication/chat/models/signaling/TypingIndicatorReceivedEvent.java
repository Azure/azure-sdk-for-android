// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models.signaling;

/**
 * Event for a received typing indicator when a chat participant is typing.
 * All chat participants receive this event, including the original sender
 */
public class TypingIndicatorReceivedEvent extends ChatUserEvent {
    /**
     * Version of the message.
     */
    private String version;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String receivedOn;

    /**
     * Gets Version of the message..
     *
     * @return Value of Version of the message..
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets new Version of the message..
     *
     * @param version New value of Version of the message..
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getReceivedOn() {
        return receivedOn;
    }

    /**
     * Sets new The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param receivedOn New value of The timestamp when the message arrived at the server.
     *                   The timestamp is in ISO8601 format:  `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setReceivedOn(String receivedOn) {
        this.receivedOn = receivedOn;
    }
}
