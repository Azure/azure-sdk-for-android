// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event for a deleted chat message.
 * All chat participants receive this event, including the original sender
 */
public class ChatMessageDeletedEvent extends ChatUserEvent {
    /**
     * The timestamp when the message was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "deletedOn")
    private String deletedOn;

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
     * The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "createdOn")
    private String createdOn;

    /**
     * Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    @JsonProperty(value = "version")
    private String version;

    /**
     * Sets new The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param createdOn New value of The timestamp when the message arrived at the server.
     *                  The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
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
     * Gets The timestamp when the message was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the message was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getDeletedOn() {
        return deletedOn;
    }

    /**
     * Sets new The display name of the event sender..
     *
     * @param senderDisplayName New value of The display name of the event sender..
     */
    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    /**
     * Gets The Id of the message. This Id is server generated..
     *
     * @return Value of The Id of the message. This Id is server generated..
     */
    public String getId() {
        return id;
    }

    /**
     * Gets The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getCreatedOn() {
        return createdOn;
    }

    /**
     * Sets new The timestamp when the message was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param deletedOn New value of The timestamp when the message was deleted. The timestamp is in ISO8601 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setDeletedOn(String deletedOn) {
        this.deletedOn = deletedOn;
    }

    /**
     * Gets The display name of the event sender..
     *
     * @return Value of The display name of the event sender..
     */
    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    /**
     * Sets new Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @param version New value of Version of the message.
     *                This version is an epoch time in a numeric unsigned Int64 format: `1593117207131`.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Sets new The Id of the message. This Id is server generated..
     *
     * @param id New value of The Id of the message. This Id is server generated..
     */
    public void setId(String id) {
        this.id = id;
    }
}
