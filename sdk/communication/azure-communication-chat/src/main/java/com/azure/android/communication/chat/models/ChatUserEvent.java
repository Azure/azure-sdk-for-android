// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for chat event
 */
public abstract class ChatUserEvent extends BaseEvent {

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
     * Sets new The Id of the event recipient..
     *
     * @param recipient New value of The Id of the event recipient..
     */
    public void setRecipient(CommunicationIdentifier recipient) {
        this.recipient = recipient;
    }

    /**
     * Sets new The Id of the event sender..
     *
     * @param sender New value of The Id of the event sender..
     */
    public void setSender(CommunicationIdentifier sender) {
        this.sender = sender;
    }

    /**
     * Gets The Id of the event sender..
     *
     * @return Value of The Id of the event sender..
     */
    public CommunicationIdentifier getSender() {
        return sender;
    }

    /**
     * Gets The Id of the event recipient..
     *
     * @return Value of The Id of the event recipient..
     */
    public CommunicationIdentifier getRecipient() {
        return recipient;
    }
}
