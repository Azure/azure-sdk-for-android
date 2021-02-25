// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

/**
 * Event for a received typing indicator when a chat participant is typing.
 * All chat participants receive this event, including the original sender
 */
class TypingIndicatorReceivedEvent extends BaseEvent {
    /**
     * Version of the message.
     */
    String version;

    /**
     * The timestamp when the message arrived at the server. The timestamp is in ISO8601 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     */
    String receivedOn;
}
