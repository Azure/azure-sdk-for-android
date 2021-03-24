// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

import com.azure.android.communication.chat.signaling.properties.CommunicationUser;

/**
 * Base class for chat event
 */
public abstract class BaseEvent {
    /**
     * Thread Id of the event.
     */
    String threadId;

    /**
     * The Id of the event sender.
     */
    CommunicationUser sender;

    /**
     * The Id of the event recipient.
     */
    CommunicationUser recipient;

}
