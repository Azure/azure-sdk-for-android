// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.chat.implementation.notifications.signaling.EventAccessorHelper;
import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Event for a created chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ChatThreadCreatedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "createTime")
    private OffsetDateTime createdOn;

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
     * The list of participants on the thread.
     */
    @JsonProperty(value = "participants")
    private List<ChatParticipant> participants;

    /**
     * The list of participants on the thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "members", access = JsonProperty.Access.WRITE_ONLY)
    private String participantsJsonString;

    /**
     * The user that created the chat thread.
     */
    private ChatParticipant createdBy;

    /**
     * The user that created the chat thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "createdBy", access = JsonProperty.Access.WRITE_ONLY)
    private String createdByJsonString;

    static {
        EventAccessorHelper.setChatThreadCreatedEventAccessor(event -> {
            ChatThreadCreatedEvent chatThreadCreatedEvent = (ChatThreadCreatedEvent) event;
            chatThreadCreatedEvent
                .setCreatedBy()
                .setParticipants()
                .setProperties();
        });
    }

    /**
     * Gets the timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
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
     * Gets the list of participants on the thread.
     *
     * @return Value of The list of participants on the thread.
     */
    public List<ChatParticipant> getParticipants() {
        return participants;
    }

    /**
     * Gets the information of the user that created the chat thread.
     *
     * @return Value of The information of the user that created the chat thread.
     */
    public ChatParticipant getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the properties of the thread.
     */
    ChatThreadCreatedEvent setProperties() {
        this.properties = new ChatThreadProperties();

        try {
            JSONObject propertiesJsonObject = new JSONObject(this.propertiesJsonString);
            this.properties
                .setId(this.getChatThreadId())
                .setTopic(propertiesJsonObject.getString("topic"))
                .setCreatedByCommunicationIdentifier(this.createdBy.getCommunicationIdentifier())
                .setCreatedOn(this.createdOn);
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    /**
     * Sets the createdBy of the thread.
     */
    ChatThreadCreatedEvent setCreatedBy() {
        this.createdBy = new ChatParticipant();

        try {
            JSONObject createdByJsonObject = new JSONObject(this.createdByJsonString);
            CommunicationIdentifier createdByCommunicationIdentifier = NotificationUtils.getCommunicationIdentifier(
                createdByJsonObject.getString("participantId"));

            this.createdBy
                .setCommunicationIdentifier(createdByCommunicationIdentifier)
                .setDisplayName(createdByJsonObject.getString("displayName"));
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    /**
     * Sets the participants of the thread.
     */
    ChatThreadCreatedEvent setParticipants() {
        this.participants = new ArrayList<>();

        try {
            JSONArray participantsJsonArray = new JSONArray(this.participantsJsonString);
            for (int i = 0; i < participantsJsonArray.length(); i++) {
                JSONObject participant = participantsJsonArray.getJSONObject(i);
                CommunicationIdentifier communicationUser = NotificationUtils.getCommunicationIdentifier(
                    participant.getString("participantId"));

                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setCommunicationIdentifier(communicationUser);
                chatParticipant.setDisplayName(participant.getString("displayName"));

                this.participants.add(chatParticipant);
            }
        } catch (JSONException e) {
            return this;
        }

        return this;
    }
}