// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.handwritten.implementation.converters;

import com.azure.android.communication.chat.handwritten.models.ChatMessage;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.ChatMessage} and
 * {@link ChatMessage}.
 */
public final class ChatMessageConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ChatMessage} to {@link ChatMessage}.
     */
    public static ChatMessage convert(com.azure.android.communication.chat.implementation.models.ChatMessage obj) {
        if (obj == null) {
            return null;
        }

        ChatMessage chatMessage = new ChatMessage()
            .setId(obj.getId())
            .setType(obj.getType())
            .setVersion(obj.getVersion())
            .setContent(ChatMessageContentConverter.convert(obj.getContent()))
            .setCreatedOn(obj.getCreatedOn())
            .setDeletedOn(obj.getDeletedOn())
            .setEditedOn(obj.getEditedOn())
            .setSenderDisplayName(obj.getSenderDisplayName());

        if (obj.getSenderCommunicationIdentifier() != null) {
            chatMessage.setSenderCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getSenderCommunicationIdentifier()));
        }

        return chatMessage;
    }

    private ChatMessageConverter() {
    }
}
