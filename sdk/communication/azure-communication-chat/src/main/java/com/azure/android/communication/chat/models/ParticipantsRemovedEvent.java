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
 * Event for a participant added to a chat thread.
 * All chat participants receive this event, including the original sender.
 */
public class ParticipantsRemovedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "time")
    private OffsetDateTime removedOn;

    /**
     * The information of the user that removed the chat participants.
     */
    private ChatParticipant removedBy;

    /**
     * The user that removed the chat participants. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "removedBy", access = JsonProperty.Access.WRITE_ONLY)
    private String removedByJsonString;

    /**
     * The participants removed from the thread.
     */
    private List<ChatParticipant> participantsRemoved;

    /**
     * The list of participants removed from the thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "participantsRemoved", access = JsonProperty.Access.WRITE_ONLY)
    private String participantsRemovedJsonString;

    static {
        EventAccessorHelper.setParticipantsRemovedEventAccessor(event -> {
            ParticipantsRemovedEvent participantsRemovedEvent = (ParticipantsRemovedEvent) event;
            participantsRemovedEvent
                .setRemovedBy()
                .setParticipantsRemoved();
        });
    }

    /**
     * Gets the timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the member was removed. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getRemovedOn() {
        return removedOn;
    }

    /**
     * Gets the information of the user that removed the chat participants.
     *
     * @return Value of The information of the user that removed the chat participants.
     */
    public ChatParticipant getRemovedBy() {
        return removedBy;
    }

    /**
     * Gets the participants removed from the thread.
     *
     * @return Value of The participants removed from the thread.
     */
    public List<ChatParticipant> getParticipantsRemoved() {
        return participantsRemoved;
    }

    /**
     * Sets the removedBy of the thread.
     */
    ParticipantsRemovedEvent setRemovedBy() {
        this.removedBy = new ChatParticipant();

        try {
            JSONObject removedByJsonObject = new JSONObject(this.removedByJsonString);
            CommunicationIdentifier removedByCommunicationIdentifier = NotificationUtils.getCommunicationIdentifier(
                removedByJsonObject.getString("participantId"));

            this.removedBy
                .setCommunicationIdentifier(removedByCommunicationIdentifier)
                .setDisplayName(removedByJsonObject.getString("displayName"));
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    /**
     * Sets the participants removed from the thread.
     */
    ParticipantsRemovedEvent setParticipantsRemoved() {
        this.participantsRemoved = new ArrayList<>();

        try {
            JSONArray participantsRemovedJsonArray = new JSONArray(this.participantsRemovedJsonString);
            for (int i = 0; i < participantsRemovedJsonArray.length(); i++) {
                JSONObject participant = participantsRemovedJsonArray.getJSONObject(i);
                CommunicationIdentifier communicationUser = NotificationUtils.getCommunicationIdentifier(
                    participant.getString("participantId"));

                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setCommunicationIdentifier(communicationUser);
                chatParticipant.setDisplayName(participant.getString("displayName"));
                chatParticipant.setShareHistoryTime(
                    NotificationUtils.parseEpochTime(participant.getLong("shareHistoryTime")));

                this.participantsRemoved.add(chatParticipant);
            }
        } catch (JSONException e) {
            return this;
        }

        return this;
    }
}
