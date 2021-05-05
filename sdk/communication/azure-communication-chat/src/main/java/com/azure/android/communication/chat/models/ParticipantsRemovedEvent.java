// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

import java.util.List;

/**
 * Event for a participant added to a chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ParticipantsRemovedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "removedOn")
    private OffsetDateTime removedOn;

    /**
     * The information of the user that removed the chat participants.
     */
    @JsonProperty(value = "removedBy")
    private ChatParticipant removedBy;

    /**
     * The participants removed from the thread.
     */
    @JsonProperty(value = "participantsRemoved")
    private List<ChatParticipant> participantsRemoved;

    /**
     * Gets The timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getRemovedOn() {
        return removedOn;
    }

    /**
     * Gets The information of the user that removed the chat participants.
     *
     * @return Value of The information of the user that removed the chat participants.
     */
    public ChatParticipant getRemovedBy() {
        return removedBy;
    }

    /**
     * Gets The participants removed from the thread.
     *
     * @return Value of The participants removed from the thread.
     */
    public List<ChatParticipant> getParticipantsRemoved() {
        return participantsRemoved;
    }

    /**
     * Sets new The timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param removedOn New value of The timestamp when the member was removed. The timestamp is in RFC3339 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public void setRemovedOn(OffsetDateTime removedOn) {
        this.removedOn = removedOn;
    }

    /**
     * Sets new The information of the user that removed the chat participants.
     *
     * @param removedBy New value of The information of the user that removed the chat participants.
     */
    public void setRemovedBy(ChatParticipant removedBy) {
        this.removedBy = removedBy;
    }

    /**
     * Sets new The participants removed from the thread.
     *
     * @param participantsRemoved New value of The participants removed from the thread.
     */
    public void setParticipantsRemoved(List<ChatParticipant> participantsRemoved) {
        this.participantsRemoved = participantsRemoved;
    }
}
