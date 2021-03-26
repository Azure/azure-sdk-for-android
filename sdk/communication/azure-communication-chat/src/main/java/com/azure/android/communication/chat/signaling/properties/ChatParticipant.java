// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.properties;

import com.azure.android.communication.common.CommunicationIdentifier;

/**
 * An Azure Communication chat participant.
 */
public class ChatParticipant {
    /**
     * The user identity of the event initiator.
     * in the format `8:acs...`.
     */
    public CommunicationIdentifier user;

    /**
     * The display name of the event initiator.
     */
    public String displayName;

    /**
     * Time from which the chat history is shared with the chat participant.
     * The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public String shareHistoryTime;
}
