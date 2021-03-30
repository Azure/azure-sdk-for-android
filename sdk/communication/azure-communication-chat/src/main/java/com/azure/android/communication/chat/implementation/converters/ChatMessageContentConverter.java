// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.implementation.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatMessageContent;
import com.azure.android.core.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.ChatMessageContent} and
 * {@link ChatMessageContent}.
 */
public final class ChatMessageContentConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ChatMessageContent} to {@link ChatMessageContent}.
     */
    public static ChatMessageContent convert(
        com.azure.android.communication.chat.implementation.models.ChatMessageContent obj,
        ClientLogger logger) {
        if (obj == null) {
            return null;
        }

        ChatMessageContent chatMessageContent = new ChatMessageContent()
            .setMessage(obj.getMessage())
            .setTopic(obj.getTopic());

        if (obj.getInitiatorCommunicationIdentifier() != null) {
            chatMessageContent.setInitiatorCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getInitiatorCommunicationIdentifier(), logger));
        }

        if (obj.getParticipants() != null) {
            List<com.azure.android.communication.chat.models.ChatParticipant> participants
                = new ArrayList<>(obj.getParticipants().size());
            for (ChatParticipant innerParticipant : obj.getParticipants()) {
                participants.add(ChatParticipantConverter.convert(innerParticipant, logger));
            }
            chatMessageContent.setParticipants(participants);
        }

        return chatMessageContent;
    }

    private ChatMessageContentConverter() {
    }
}

