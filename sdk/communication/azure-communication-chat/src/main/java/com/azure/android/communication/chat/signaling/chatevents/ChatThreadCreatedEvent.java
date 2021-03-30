// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.ChatParticipant;
import com.azure.android.communication.chat.signaling.properties.ChatThreadProperties;

import java.util.List;

/**
 * Event for a created chat thread.
 * All chat participants receive this event, including the original sender
 */
public class ChatThreadCreatedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was created. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String createdOn;

    /**
     * The properties of the thread.
     */
    private ChatThreadProperties properties;

    /**
     * The list of participants on the thread.
     */
    private List<ChatParticipant> participants;

    /**
     * The information of the user that created the chat thread.
     */
    private ChatParticipant createdBy;


    /**
     * Sets new The properties of the thread..
     *
     * @param properties New value of The properties of the thread..
     */
    public void setProperties(ChatThreadProperties properties) {
        this.properties = properties;
    }

    /**
     * Gets The information of the user that created the chat thread..
     *
     * @return Value of The information of the user that created the chat thread..
     */
    public ChatParticipant getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets new The timestamp when the thread was created. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param createdOn New value of The timestamp when the thread was created. The timestamp is in ISO8601 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Gets The properties of the thread..
     *
     * @return Value of The properties of the thread..
     */
    public ChatThreadProperties getProperties() {
        return properties;
    }

    /**
     * Gets The timestamp when the thread was created. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the thread was created. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getCreatedOn() {
        return createdOn;
    }

    /**
     * Gets The list of participants on the thread..
     *
     * @return Value of The list of participants on the thread..
     */
    public List<ChatParticipant> getParticipants() {
        return participants;
    }

    /**
     * Sets new The list of participants on the thread..
     *
     * @param participants New value of The list of participants on the thread..
     */
    public void setParticipants(List<ChatParticipant> participants) {
        this.participants = participants;
    }

    /**
     * Sets new The information of the user that created the chat thread..
     *
     * @param createdBy New value of The information of the user that created the chat thread..
     */
    public void setCreatedBy(ChatParticipant createdBy) {
        this.createdBy = createdBy;
    }
}
