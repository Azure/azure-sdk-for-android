// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.handwritten.implementation.converters;

import com.azure.android.communication.chat.handwritten.models.ChatThread;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.ChatThread} and
 * {@link ChatThread}.
 */
public final class ChatThreadConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ChatThread} to {@link ChatThread}.
     */
    public static ChatThread convert(com.azure.android.communication.chat.implementation.models.ChatThread obj) {
        if (obj == null) {
            return null;
        }

        ChatThread chatThread = new ChatThread()
            .setId(obj.getId())
            .setTopic(obj.getTopic())
            .setCreatedOn(obj.getCreatedOn());

        if (obj.getCreatedByCommunicationIdentifier() != null) {
            chatThread.setCreatedByCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getCreatedByCommunicationIdentifier()));
        }

        return chatThread;
    }

    private ChatThreadConverter() {
    }
}
