// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

import java.util.List;

/**
 * Event for a created chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ChatThreadCreatedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "createdOn")
    private OffsetDateTime createdOn;

    /**
     * The properties of the thread.
     */
    @JsonProperty(value = "properties")
    private ChatThreadProperties properties;

    /**
     * The list of participants on the thread.
     */
    @JsonProperty(value = "participants")
    private List<ChatParticipant> participants;

    /**
     * The information of the user that created the chat thread.
     */
    @JsonProperty(value = "createdBy")
    private ChatParticipant createdBy;

    /**
     * Gets The timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Gets The properties of the thread.
     *
     * @return Value of The properties of the thread.
     */
    public ChatThreadProperties getProperties() {
        return properties;
    }

    /**
     * Gets The list of participants on the thread.
     *
     * @return Value of The list of participants on the thread.
     */
    public List<ChatParticipant> getParticipants() {
        return participants;
    }

    /**
     * Gets The information of the user that created the chat thread.
     *
     * @return Value of The information of the user that created the chat thread.
     */
    public ChatParticipant getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets new The timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param createdOn New value of The timestamp when the thread was created. The timestamp is in RFC3339 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
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
     * Sets new The list of participants on the thread.
     *
     * @param participants New value of The list of participants on the thread.
     */
    public void setParticipants(List<ChatParticipant> participants) {
        this.participants = participants;
    }

    /**
     * Sets new The information of the user that created the chat thread.
     *
     * @param createdBy New value of The information of the user that created the chat thread.
     */
    public void setCreatedBy(ChatParticipant createdBy) {
        this.createdBy = createdBy;
    }
}
