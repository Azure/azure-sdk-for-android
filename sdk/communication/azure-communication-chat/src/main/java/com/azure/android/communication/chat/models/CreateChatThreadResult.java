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
     * The thread property.
     */
    @JsonProperty(value = "chatThread")
    private ChatThread chatThread;

    /*
     * The participants that failed to be added to the chat thread.
     */
    @JsonProperty(value = "invalidParticipants", access = JsonProperty.Access.WRITE_ONLY)
    private List<ChatError> invalidParticipants;

    /**
     * Get the chatThread property: The chatThread property.
     *
     * @return the chatThread value.
     */
    public ChatThread getChatThread() {
        return this.chatThread;
    }

    /**
     * Set the chatThread property: The chatThread property.
     *
     * @param chatThread the thread value to set.
     * @return the CreateChatThreadResult object itself.
     */
    public CreateChatThreadResult setChatThread(ChatThread chatThread) {
        this.chatThread = chatThread;
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
