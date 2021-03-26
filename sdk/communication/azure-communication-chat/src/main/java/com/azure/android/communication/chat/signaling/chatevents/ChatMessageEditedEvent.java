// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

/**
 * Event for a edited chat message.
 * All chat participants receive this event, including the original sender
 */
public class ChatMessageEditedEvent extends ChatUserEvent {
    /**
     * Content of the edited message.
     */
    private String content;

    /**
     * The timestamp when the message was edited. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String editedOn;

    /**
     * The Id of the message. This Id is server generated.
     */
    private String id;

    /**
     * The display name of the event sender.
     */
    private String senderDisplayName;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String createdOn;

    /**
     * Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    private String version;


    /**
     * Gets The timestamp when the message was edited. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of The timestamp when the message was edited. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getEditedOn() {
        return editedOn;
    }

    /**
     * Gets Content of the edited message..
     *
     * @return Value of Content of the edited message..
     */
    public String getContent() {
        return content;
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
     * Sets new The timestamp when the message was edited. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param editedOn New value of The timestamp when the message was edited. The timestamp is in ISO8601 format:
     *                 `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setEditedOn(String editedOn) {
        this.editedOn = editedOn;
    }

    /**
     * Sets new Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @param version New value of Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     *                `1593117207131`.
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
     * Sets new The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param createdOn New value of The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     *                  `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
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
     * Sets new Content of the edited message..
     *
     * @param content New value of Content of the edited message..
     */
    public void setContent(String content) {
        this.content = content;
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
}
