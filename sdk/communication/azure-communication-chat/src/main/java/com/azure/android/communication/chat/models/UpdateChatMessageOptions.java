// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.android.communication.chat.models;

import com.azure.android.core.rest.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** Request payload for updating a chat message. */
@Fluent
public final class UpdateChatMessageOptions {
    /*
     * Chat message content.
     */
    @JsonProperty(value = "content")
    private String content;

    /*
     * Message metadata.
     */
    @JsonProperty(value = "metadata")
    private Map<String, String> metadata;

    /**
     * Get the content property: Chat message content.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Chat message content.
     *
     * @param content the content value to set.
     * @return the UpdateChatMessageOptions object itself.
     */
    public UpdateChatMessageOptions setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Get the metadata property: Message metadata.
     *
     * @return the metadata value.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: Message metadata.
     *
     * @param metadata the metadata value to set.
     * @return the UpdateChatMessageOptions object itself.
     */
    public UpdateChatMessageOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}