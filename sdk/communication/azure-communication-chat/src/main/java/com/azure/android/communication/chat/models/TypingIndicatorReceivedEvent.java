// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

/**
 * Event for a received typing indicator when a chat participant is typing.
 * All chat participants receive this event, including the original sender.
 */
public final class TypingIndicatorReceivedEvent extends ChatUserEvent {
    /**
     * Version of the message.
     */
    @JsonProperty(value = "version")
    private String version;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "receivedOn")
    private OffsetDateTime receivedOn;

    /**
     * Gets Version of the message.
     *
     * @return Value of Version of the message.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getReceivedOn() {
        return receivedOn;
    }

    /**
     * Sets new Version of the message.
     *
     * @param version New value of Version of the message.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Sets new The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param receivedOn New value of The timestamp when the message arrived at the server.
     *                   The timestamp is in RFC3339 format:  `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public void setReceivedOn(OffsetDateTime receivedOn) {
        this.receivedOn = receivedOn;
    }
}
