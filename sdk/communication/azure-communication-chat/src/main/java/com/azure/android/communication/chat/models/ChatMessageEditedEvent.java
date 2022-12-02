// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.chat.implementation.notifications.signaling.EventAccessorHelper;
import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

import java.util.Map;

/**
 * Event for a edited chat message.
 * All chat participants receive this event, including the original sender.
 */
public final class ChatMessageEditedEvent extends ChatUserEvent {
    /**
     * Content of the edited message.
     */
    @JsonProperty(value = "messageBody")
    private String content;

    /**
     * The timestamp when the message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "edittime")
    private OffsetDateTime editedOn;

    /**
     * The Id of the message. This Id is server generated.
     */
    @JsonProperty(value = "messageId")
    private String id;

    /**
     * The display name of the event sender.
     */
    @JsonProperty(value = "senderDisplayName")
    private String senderDisplayName;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "originalArrivalTime")
    private OffsetDateTime createdOn;

    /**
     * Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    @JsonProperty(value = "version")
    private String version;

    /**
     * Original metadata of the chat message. A string property in notification payload.
     */
    @JsonProperty(value = "acsChatMessageMetadata")
    private String acsChatMessageMetadata;

    /**
     * Message metadata.
     */
    @JsonProperty(value = "metadata")
    private Map<String, String> metadata;

    static {
        EventAccessorHelper.setChatMessageEditedEventAccessorAccessor(event -> {
            ChatMessageEditedEvent chatMessageEditedEvent = (ChatMessageEditedEvent) event;
            chatMessageEditedEvent
                .setMetadata()
                .setSender()
                .setRecipient()
                .setThreadId();
        });
    }

    /**
     * Gets content of the edited message.
     *
     * @return Value of Content of the edited message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the timestamp when the message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getEditedOn() {
        return editedOn;
    }

    /**
     * Gets the Id of the message. This Id is server generated.
     *
     * @return Value of The Id of the message. This Id is server generated.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name of the event sender.
     *
     * @return Value of The display name of the event sender.
     */
    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    /**
     * Gets the timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Gets version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @return Value of Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets message metadata.
     *
     * @return Value of message metadata.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets metadata of the chat message.
     */
    private ChatMessageEditedEvent setMetadata() {
        this.metadata = NotificationUtils.parseChatMessageMetadata(this.acsChatMessageMetadata);
        return this;
    }
}