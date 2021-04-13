// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

/**
 * Event for an updated chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ChatThreadDeletedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String deletedOn;

    /**
     * The information of the user that deleted the chat thread.
     */
    private ChatParticipant deletedBy;

    /**
     * Sets new The timestamp when the thread was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param deletedOn New value of The timestamp when the thread was deleted. The timestamp is in ISO8601 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setDeletedOn(String deletedOn) {
        this.deletedOn = deletedOn;
    }

    /**
     * Gets The information of the user that deleted the chat thread..
     *
     * @return Value of The information of the user that deleted the chat thread..
     */
    public ChatParticipant getDeletedBy() {
        return deletedBy;
    }

    /**
     * Sets new The information of the user that deleted the chat thread..
     *
     * @param deletedBy New value of The information of the user that deleted the chat thread..
     */
    public void setDeletedBy(ChatParticipant deletedBy) {
        this.deletedBy = deletedBy;
    }

    /**
     * Gets The timestamp when the thread was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the thread was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getDeletedOn() {
        return deletedOn;
    }
}
