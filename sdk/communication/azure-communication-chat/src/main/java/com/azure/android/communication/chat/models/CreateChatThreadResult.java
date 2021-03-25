// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.core.rest.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The CreateChatThreadResult model. */
@Fluent
public final class CreateChatThreadResult {
    /*
     * The chatThreadProperties property.
     */
    @JsonProperty(value = "chatThreadProperties")
    private ChatThreadProperties chatThreadProperties;

    /*
     * The participants that failed to be added to the chat thread.
     */
    @JsonProperty(value = "invalidParticipants", access = JsonProperty.Access.WRITE_ONLY)
    private List<ChatError> invalidParticipants;

    /**
     * Get the chatThreadProperties property: The chatThreadProperties property.
     *
     * @return the chatThreadProperties value.
     */
    public ChatThreadProperties getChatThreadProperties() {
        return this.chatThreadProperties;
    }

    /**
     * Set the chatThreadProperties property: The chatThreadProperties property.
     *
     * @param chatThreadProperties the thread value to set.
     * @return the CreateChatThreadResult object itself.
     */
    public CreateChatThreadResult setChatThreadProperties(ChatThreadProperties chatThreadProperties) {
        this.chatThreadProperties = chatThreadProperties;
        return this;
    }

    /**
     * Get the invalidParticipants property: The participants that failed to be added to the chat thread.
     *
     * @return the invalidParticipants value.
     */
    public List<ChatError> getInvalidParticipants() {
        return this.invalidParticipants;
    }

    /**
     * Set the invalidParticipants property: The invalidParticipants property.
     *
     * @param invalidParticipants the invalidParticipants value to set.
     * @return the CreateChatThreadResult object itself.
     */
    public CreateChatThreadResult setInvalidParticipants(List<ChatError> invalidParticipants) {
        this.invalidParticipants = invalidParticipants;
        return this;
    }
}
