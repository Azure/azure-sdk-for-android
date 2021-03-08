// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

/**
 * Event for a deleted chat message.
 * All chat participants receive this event, including the original sender
 */
class ChatMessageDeletedEvent extends BaseEvent {
    /**
     * The timestamp when the message was deleted. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String deletedOn;

    /**
     * The Id of the message. This Id is server generated.
     */
    String id;

    /**
     * The display name of the event sender.
     */
    String senderDisplayName;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String createdOn;

    /**
     * Version of the message. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    String version;
}
