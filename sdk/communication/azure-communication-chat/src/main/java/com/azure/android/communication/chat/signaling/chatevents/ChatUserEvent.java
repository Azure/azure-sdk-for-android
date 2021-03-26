// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.common.CommunicationIdentifier;

/**
 * Base class for chat event
 */
public abstract class ChatUserEvent extends BaseEvent {

    /**
     * The Id of the event sender.
     */
    public CommunicationIdentifier sender;

    /**
     * The Id of the event recipient.
     */
    public CommunicationIdentifier recipient;

}
