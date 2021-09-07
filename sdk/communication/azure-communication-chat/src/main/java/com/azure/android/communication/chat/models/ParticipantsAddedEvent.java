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
 * Event for participants added to a chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ParticipantsAddedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "time")
    private OffsetDateTime addedOn;

    /**
     * The information of the user that added the chat participants.
     */
    private ChatParticipant addedBy;

    /**
     * The user that added the chat participants. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "addedBy", access = JsonProperty.Access.WRITE_ONLY)
    private String addedByJsonString;

    /**
     * The participants added to the thread.
     */
    private List<ChatParticipant> participantsAdded;

    /**
     * The list of participants added to the thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "participantsAdded", access = JsonProperty.Access.WRITE_ONLY)
    private String participantsAddedJsonString;

    static {
        EventAccessorHelper.setParticipantsAddedEventAccessor(event -> {
            ParticipantsAddedEvent participantsAddedEvent = (ParticipantsAddedEvent) event;
            participantsAddedEvent
                .setAddedBy()
                .setParticipantsAdded();
        });
    }

    /**
     * Gets the timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the member was added. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getAddedOn() {
        return addedOn;
    }

    /**
     * Gets the information of the user that added the chat participants.
     *
     * @return Value of The information of the user that added the chat participants.
     */
    public ChatParticipant getAddedBy() {
        return addedBy;
    }

    /**
     * Gets the participants added to the thread.
     *
     * @return Value of The participants added to the thread.
     */
    public List<ChatParticipant> getParticipantsAdded() {
        return participantsAdded;
    }

    /**
     * Sets the addedBy of the thread.
     */
    ParticipantsAddedEvent setAddedBy() {
        this.addedBy = new ChatParticipant();

        try {
            JSONObject addedByJsonObject = new JSONObject(this.addedByJsonString);
            CommunicationIdentifier addedByCommunicationIdentifier = NotificationUtils.getCommunicationIdentifier(
                addedByJsonObject.getString("participantId"));

            this.addedBy
                .setCommunicationIdentifier(addedByCommunicationIdentifier)
                .setDisplayName(addedByJsonObject.getString("displayName"));
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    /**
     * Sets the participants added to the thread.
     */
    ParticipantsAddedEvent setParticipantsAdded() {
        this.participantsAdded = new ArrayList<>();

        try {
            JSONArray participantsAddedJsonArray = new JSONArray(this.participantsAddedJsonString);
            for (int i = 0; i < participantsAddedJsonArray.length(); i++) {
                JSONObject participant = participantsAddedJsonArray.getJSONObject(i);
                CommunicationIdentifier communicationUser = NotificationUtils.getCommunicationIdentifier(
                    participant.getString("participantId"));

                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setCommunicationIdentifier(communicationUser);
                chatParticipant.setDisplayName(participant.getString("displayName"));
                chatParticipant.setShareHistoryTime(
                    NotificationUtils.parseEpochTime(participant.getLong("shareHistoryTime")));

                this.participantsAdded.add(chatParticipant);
            }
        } catch (JSONException e) {
            return this;
        }

        return this;
    }
}
