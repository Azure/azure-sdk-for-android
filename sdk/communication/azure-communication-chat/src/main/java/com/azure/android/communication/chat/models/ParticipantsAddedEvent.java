// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

import java.util.List;

/**
 * Event for participants added to a chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ParticipantsAddedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "addedOn")
    private OffsetDateTime addedOn;

    /**
     * The information of the user that added the chat participants.
     */
    @JsonProperty(value = "addedBy")
    private ChatParticipant addedBy;

    /**
     * The participants added to the thread.
     */
    @JsonProperty(value = "participantsAdded")
    private List<ChatParticipant> participantsAdded;

    /**
     * Gets The timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getAddedOn() {
        return addedOn;
    }

    /**
     * Gets The information of the user that added the chat participants.
     *
     * @return Value of The information of the user that added the chat participants.
     */
    public ChatParticipant getAddedBy() {
        return addedBy;
    }

    /**
     * Gets The participants added to the thread.
     *
     * @return Value of The participants added to the thread.
     */
    public List<ChatParticipant> getParticipantsAdded() {
        return participantsAdded;
    }

    /**
     * Sets new The timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param addedOn New value of The timestamp when the member was added. The timestamp is in RFC3339 format:
     *                `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public void setAddedOn(OffsetDateTime addedOn) {
        this.addedOn = addedOn;
    }

    /**
     * Sets new The information of the user that added the chat participants.
     *
     * @param addedBy New value of The information of the user that added the chat participants.
     */
    public void setAddedBy(ChatParticipant addedBy) {
        this.addedBy = addedBy;
    }

    /**
     * Sets new The participants added to the thread.
     *
     * @param participantsAdded New value of The participants added to the thread.
     */
    public void setParticipantsAdded(List<ChatParticipant> participantsAdded) {
        this.participantsAdded = participantsAdded;
    }
}
