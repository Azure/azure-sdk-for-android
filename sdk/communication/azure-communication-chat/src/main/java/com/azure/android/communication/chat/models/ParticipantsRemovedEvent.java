// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import java.util.List;

/**
 * Event for a participant added to a chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ParticipantsRemovedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was removed. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String removedOn;

    /**
     * The participants removed from the thread.
     */
    private List<ChatParticipant> participantsRemoved;

    /**
     * The information of the user that removed the chat participants.
     */
    private ChatParticipant removedBy;

    /**
     * Gets The participants removed from the thread..
     *
     * @return Value of The participants removed from the thread..
     */
    public List<ChatParticipant> getParticipantsRemoved() {
        return participantsRemoved;
    }

    /**
     * Sets new The participants removed from the thread..
     *
     * @param participantsRemoved New value of The participants removed from the thread..
     */
    public void setParticipantsRemoved(List<ChatParticipant> participantsRemoved) {
        this.participantsRemoved = participantsRemoved;
    }

    /**
     * Gets The timestamp when the member was removed. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the member was removed. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getRemovedOn() {
        return removedOn;
    }

    /**
     * Sets new The timestamp when the member was removed. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param removedOn New value of The timestamp when the member was removed. The timestamp is in ISO8601 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setRemovedOn(String removedOn) {
        this.removedOn = removedOn;
    }

    /**
     * Gets The information of the user that removed the chat participants..
     *
     * @return Value of The information of the user that removed the chat participants..
     */
    public ChatParticipant getRemovedBy() {
        return removedBy;
    }

    /**
     * Sets new The information of the user that removed the chat participants..
     *
     * @param removedBy New value of The information of the user that removed the chat participants..
     */
    public void setRemovedBy(ChatParticipant removedBy) {
        this.removedBy = removedBy;
    }
}
