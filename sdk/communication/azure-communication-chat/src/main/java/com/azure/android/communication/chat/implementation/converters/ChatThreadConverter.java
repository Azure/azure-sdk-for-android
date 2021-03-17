// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.implementation.models.ChatThreadProperties;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.core.logging.ClientLogger;

/**
 * A converter between {@link ChatThreadProperties} and
 * {@link ChatThread}.
 */
public final class ChatThreadConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ChatThread} to {@link ChatThread}.
     */
    public static ChatThread convert(ChatThreadProperties obj,
                                     ClientLogger logger) {
        if (obj == null) {
            return null;
        }

        ChatThread chatThread = new ChatThread()
            .setId(obj.getId())
            .setTopic(obj.getTopic())
            .setCreatedOn(obj.getCreatedOn());

        if (obj.getCreatedByCommunicationIdentifier() != null) {
            chatThread.setCreatedByCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getCreatedByCommunicationIdentifier(), logger));
        }

        return chatThread;
    }

    private ChatThreadConverter() {
    }
}
