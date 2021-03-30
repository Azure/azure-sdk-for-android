// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.core.logging.ClientLogger;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.ChatParticipant} and
 * {@link ChatParticipant}.
 */
public final class ChatParticipantConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ChatThreadMember} to {@link ChatParticipant}.
     */
    public static ChatParticipant convert(
        com.azure.android.communication.chat.implementation.models.ChatParticipant obj,
        ClientLogger logger) {
        if (obj == null) {
            return null;
        }

        ChatParticipant chatParticipant = new ChatParticipant()
            .setCommunicationIdentifier(CommunicationIdentifierConverter.convert(obj.getCommunicationIdentifier(),
                logger))
            .setDisplayName(obj.getDisplayName())
            .setShareHistoryTime(obj.getShareHistoryTime());

        return chatParticipant;
    }

    /**
     * Maps from {ChatParticipant} to {@link com.azure.android.communication.chat.implementation.models.ChatParticipant}.
     */
    public static com.azure.android.communication.chat.implementation.models.ChatParticipant convert(
        ChatParticipant obj,
        ClientLogger logger) {
        if (obj == null) {
            return null;
        }

        com.azure.android.communication.chat.implementation.models.ChatParticipant chatParticipant
            = new com.azure.android.communication.chat.implementation.models.ChatParticipant()
            .setCommunicationIdentifier(CommunicationIdentifierConverter.convert(obj.getCommunicationIdentifier(),
                logger))
            .setDisplayName(obj.getDisplayName())
            .setShareHistoryTime(obj.getShareHistoryTime());

        return chatParticipant;
    }

    private ChatParticipantConverter() {
    }
}
