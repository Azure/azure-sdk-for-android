// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.core.rest.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

/** The ChatThreadProperties model. */
@Fluent
public final class ChatThreadProperties {
    /*
     * Chat thread id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /*
     * Chat thread topic.
     */
    @JsonProperty(value = "topic")
    private String topic;

    /*
     * The timestamp when the chat thread was created. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "createdOn", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdOn;

    /*
     * Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This
     * model must be interpreted as a union: Apart from rawId, at most one
     * further property may be set.
     */
    @JsonProperty(value = "createdByCommunicationIdentifier", access = JsonProperty.Access.WRITE_ONLY)
    private CommunicationIdentifier createdByCommunicationIdentifier;

    /**
     * Get the id property: Chat thread id.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Chat thread id.
     *
     * @param id the id value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the topic property: Chat thread topic.
     *
     * @return the topic value.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Set the topic property: Chat thread topic.
     *
     * @param topic the topic value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Get the createdOn property: The timestamp when the chat thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Set the createdOn property: The timestamp when the chat thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param createdOn the createdOn value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /**
     * Get the createdByCommunicationIdentifier property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @return the createdByCommunicationIdentifier value.
     */
    public CommunicationIdentifier getCreatedByCommunicationIdentifier() {
        return this.createdByCommunicationIdentifier;
    }

    /**
     * Set the createdByCommunicationIdentifier property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @param createdByCommunicationIdentifier the createdByCommunicationIdentifier value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setCreatedByCommunicationIdentifier(
        CommunicationIdentifier createdByCommunicationIdentifier) {
        this.createdByCommunicationIdentifier = createdByCommunicationIdentifier;
        return this;
    }
}
