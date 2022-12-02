// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.chat.implementation.notifications.signaling.EventAccessorHelper;
import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.OffsetDateTime;

/**
 * Event for an updated chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ChatThreadPropertiesUpdatedEvent extends ChatThreadEvent {
    /**
     * The properties of the thread.
     */
    private ChatThreadProperties properties;

    /**
     * The properties of the thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "properties")
    private String propertiesJsonString;

    /**
     * The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "editTime")
    private OffsetDateTime updatedOn;

    /**
     * The information of the user that updated the chat thread.
     */
    private ChatParticipant updatedBy;

    /**
     * The user that updated the chat thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "editedBy", access = JsonProperty.Access.WRITE_ONLY)
    private String updatedByJsonString;

    static {
        EventAccessorHelper.setChatThreadPropertiesUpdatedEventAccessor(event -> {
            ChatThreadPropertiesUpdatedEvent chatThreadPropertiesUpdatedEvent
                = (ChatThreadPropertiesUpdatedEvent) event;
            chatThreadPropertiesUpdatedEvent
                .setUpdatedBy()
                .setProperties();
        });
    }

    /**
     * Gets the properties of the thread.
     *
     * @return Value of The properties of the thread.
     */
    public ChatThreadProperties getProperties() {
        return properties;
    }

    /**
     * Gets the timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the thread was updated. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Gets the information of the user that updated the chat thread.
     *
     * @return Value of The information of the user that updated the chat thread.
     */
    public ChatParticipant getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the updatedBy of the thread.
     */
    ChatThreadPropertiesUpdatedEvent setUpdatedBy() {
        this.updatedBy = new ChatParticipant();

        try {
            JSONObject updatedByJsonObject = new JSONObject(this.updatedByJsonString);
            CommunicationIdentifier updatedByCommunicationIdentifier = NotificationUtils.getCommunicationIdentifier(
                updatedByJsonObject.getString("participantId"));

            this.updatedBy
                .setCommunicationIdentifier(updatedByCommunicationIdentifier)
                .setDisplayName(updatedByJsonObject.getString("displayName"));
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    /**
     * Sets the properties of the thread.
     */
    ChatThreadPropertiesUpdatedEvent setProperties() {
        this.properties = new ChatThreadProperties();

        try {
            JSONObject propertiesJsonObject = new JSONObject(this.propertiesJsonString);
            this.properties
                .setId(this.getChatThreadId())
                .setTopic(propertiesJsonObject.getString("topic"));
        } catch (JSONException e) {
            return this;
        }

        return this;
    }
}