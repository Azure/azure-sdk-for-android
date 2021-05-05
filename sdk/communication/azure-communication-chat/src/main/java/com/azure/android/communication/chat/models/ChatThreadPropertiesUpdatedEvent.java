// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

/**
 * Event for an updated chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ChatThreadPropertiesUpdatedEvent extends ChatThreadEvent {
    /**
     * The properties of the thread.
     */
    @JsonProperty(value = "properties")
    private ChatThreadProperties properties;

    /**
     * The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "updatedOn")
    private OffsetDateTime updatedOn;

    /**
     * The information of the user that updated the chat thread.
     */
    @JsonProperty(value = "updatedBy")
    private ChatParticipant updatedBy;

    /**
     * Gets The properties of the thread.
     *
     * @return Value of The properties of the thread.
     */
    public ChatThreadProperties getProperties() {
        return properties;
    }

    /**
     * Gets The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Gets The information of the user that updated the chat thread.
     *
     * @return Value of The information of the user that updated the chat thread.
     */
    public ChatParticipant getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets new The properties of the thread.
     *
     * @param properties New value of The properties of the thread.
     */
    public void setProperties(ChatThreadProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets new The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param updatedOn New value of The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public void setUpdatedOn(OffsetDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Sets new The information of the user that updated the chat thread.
     *
     * @param updatedBy New value of The information of the user that updated the chat thread.
     */
    public void setUpdatedBy(ChatParticipant updatedBy) {
        this.updatedBy = updatedBy;
    }
}
