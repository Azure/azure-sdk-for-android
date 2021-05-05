// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.threeten.bp.OffsetDateTime;

/**
 * Event for a received chat message.
 * All chat participants receive this event, including the original sender.
 */
public final class ChatMessageReceivedEvent extends ChatUserEvent {
    /**
     * Type of the chat message.
     */
    @JsonProperty(value = "type")
    private ChatMessageType type;

    /**
     * Content of the message.
     */
    @JsonProperty(value = "content")
    private String content;

    /**
     * Priority of the message. Possible values include: 'Normal', 'High'
     */
    @JsonProperty(value = "priority")
    private String priority;

    /**
     * The Id of the message. This Id is server generated.
     */
    @JsonProperty(value = "id")
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
    @JsonProperty(value = "createdOn")
    private OffsetDateTime createdOn;

    /**
     * Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    @JsonProperty(value = "version")
    private String version;

    /**
     * Gets Type of the chat message.
     *
     * @return Value of Type of the chat message.
     */
    public ChatMessageType getType() {
        return type;
    }

    /**
     * Gets Content of the message.
     *
     * @return Value of Content of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets Priority of the message. Possible values include: 'Normal', 'High'.
     *
     * @return Value of Priority of the message. Possible values include: 'Normal', 'High'.
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Gets The Id of the message. This Id is server generated.
     *
     * @return Value of The Id of the message. This Id is server generated.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets The display name of the event sender.
     *
     * @return Value of The display name of the event sender.
     */
    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    /**
     * Gets The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return Value of The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Gets Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @return Value of Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets new Type of the chat message.
     *
     * @param type New value of Type of the chat message.
     */
    public void setType(ChatMessageType type) {
        this.type = type;
    }

    /**
     * Sets new Content of the message.
     *
     * @param content New value of Content of the message.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets new Priority of the message. Possible values include: 'Normal', 'High'.
     *
     * @param priority New value of Priority of the message. Possible values include: 'Normal', 'High'.
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Sets new The Id of the message. This Id is server generated.
     *
     * @param id New value of The Id of the message. This Id is server generated.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets new The display name of the event sender.
     *
     * @param senderDisplayName New value of The display name of the event sender.
     */
    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    /**
     * Sets new The timestamp when the message arrived at the server. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param createdOn New value of The timestamp when the message arrived at the server.
     *                  The timestamp is in RFC3339 format:  `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Sets new Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @param version New value of Version of the message.
     *                This version is an epoch time in a numeric unsigned Int64 format:  `1593117207131`.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
