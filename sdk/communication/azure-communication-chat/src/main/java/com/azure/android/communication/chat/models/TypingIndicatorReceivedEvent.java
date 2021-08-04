// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.chat.implementation.signaling.EventAccessorHelper;
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
    @JsonProperty(value = "originalArrivalTime")
    private OffsetDateTime receivedOn;

    /**
     * The display name of the event sender.
     */
    @JsonProperty(value = "senderDisplayName")
    private String senderDisplayName;

    static {
        EventAccessorHelper.setTypingIndicatorReceivedEventAccessor(event -> {
            TypingIndicatorReceivedEvent typingIndicatorReceivedEvent = (TypingIndicatorReceivedEvent) event;
            typingIndicatorReceivedEvent
                .setSender()
                .setRecipient()
                .setThreadId();
        });
    }

    /**
     * Gets version of the message.
     *
     * @return Value of Version of the message.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getReceivedOn() {
        return receivedOn;
    }

    /**
     * Gets the display name of the event sender.
     *
     * @return Value of The display name of the event sender.
     */
    public String getSenderDisplayName() {
        return senderDisplayName;
    }
}
