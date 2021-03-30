// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.implementation.models.ChatParticipant;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.core.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions} and
 * {@link CreateChatThreadOptions}.
 */
public final class CreateChatThreadOptionsConverter {
    /**
     * Maps from {CreateChatThreadOptions} to
     * {@link com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions}.
     */
    public static com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions convert(
        CreateChatThreadOptions obj,
        ClientLogger logger) {

        if (obj == null) {
            return null;
        }

        List<ChatParticipant> innerParticipants
            = new ArrayList<>(obj.getParticipants().size());
        for (com.azure.android.communication.chat.models.ChatParticipant participant : obj.getParticipants()) {
            innerParticipants.add(ChatParticipantConverter.convert(participant, logger));
        }

        com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions createChatThreadOptions
            = new com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions()
            .setTopic(obj.getTopic())
            .setParticipants(innerParticipants);

        return createChatThreadOptions;
    }

    private CreateChatThreadOptionsConverter() {
    }
}
