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
public class ChatThreadDeletedEvent extends ChatThreadEvent {
    /**
     * The timestamp when the thread was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "deleteTime")
    private OffsetDateTime deletedOn;

    /**
     * The information of the user that deleted the chat thread.
     */
    private ChatParticipant deletedBy;

    /**
     * The user that deleted the chat thread. A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "deletedBy", access = JsonProperty.Access.WRITE_ONLY)
    private String deletedByJsonString;

    static {
        EventAccessorHelper.setChatThreadDeletedEventAccessor(event -> {
            ChatThreadDeletedEvent chatThreadDeletedEvent = (ChatThreadDeletedEvent) event;
            chatThreadDeletedEvent.setDeletedBy();
        });
    }

    /**
     * Gets the timestamp when the thread was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the thread was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getDeletedOn() {
        return deletedOn;
    }

    /**
     * Gets the information of the user that deleted the chat thread.
     *
     * @return Value of The information of the user that deleted the chat thread.
     */
    public ChatParticipant getDeletedBy() {
        return deletedBy;
    }

    /**
     * Sets the deletedBy of the thread.
     */
    ChatThreadDeletedEvent setDeletedBy() {
        this.deletedBy = new ChatParticipant();

        try {
            JSONObject deletedByJsonObject = new JSONObject(this.deletedByJsonString);
            CommunicationIdentifier deletedByCommunicationIdentifier = NotificationUtils.getCommunicationIdentifier(
                deletedByJsonObject.getString("participantId"));

            this.deletedBy
                .setCommunicationIdentifier(deletedByCommunicationIdentifier)
                .setDisplayName(deletedByJsonObject.getString("displayName"));
        } catch (JSONException e) {
            return this;
        }

        return this;
    }
}
