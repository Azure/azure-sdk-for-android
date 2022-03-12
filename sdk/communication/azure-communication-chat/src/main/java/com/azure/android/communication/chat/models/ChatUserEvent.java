// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.chat.implementation.signaling.TrouterUtils;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for chat event
 */
public abstract class ChatUserEvent extends ChatEvent {
    /**
     * The Id of the event sender. A string property in notification payload.
     */
    @JsonProperty(value = "senderId", access = JsonProperty.Access.WRITE_ONLY)
    private String senderId;

    /**
     * The Id of the event recipient. A string property in notification payload.
     */
    @JsonProperty(value = "recipientMri", access = JsonProperty.Access.WRITE_ONLY)
    private String recipientId;

    /**
     * The Id of the event sender.
     */
    @JsonProperty(value = "sender")
    private CommunicationIdentifier sender;

    /**
     * The Id of the event recipient.
     */
    @JsonProperty(value = "recipient")
    private CommunicationIdentifier recipient;

    /**
     * Gets the Id of the event sender.
     *
     * @return Value of The Id of the event sender.
     */
    public CommunicationIdentifier getSender() {
        return sender;
    }

    /**
     * Gets the Id of the event recipient.
     *
     * @return Value of The Id of the event recipient.
     */
    public CommunicationIdentifier getRecipient() {
        return recipient;
    }

    /**
     * Sets the Id of the event sender.
     */
    ChatUserEvent setSender() {
        this.sender = TrouterUtils.getCommunicationIdentifier(senderId);
        return this;
    }

    /**
     * Sets the Id of the event recipient.
     */
    ChatUserEvent setRecipient() {
        this.recipient = TrouterUtils.getCommunicationIdentifier(recipientId);
        return this;
    }
}
