// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.implementation.models.CommunicationError;
import com.azure.android.communication.chat.models.ChatError;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.core.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.AddChatParticipantsResult} and
 * {@link AddChatParticipantsResult}.
 */
public final class AddChatParticipantsResultConverter {
    /**
     * Maps from {@link com.azure.android.communication.chat.implementation.models.AddChatParticipantsResult} to
     * {@link AddChatParticipantsResult}.
     */
    public static AddChatParticipantsResult convert(
        com.azure.android.communication.chat.implementation.models.AddChatParticipantsResult obj, ClientLogger logger) {

        if (obj == null) {
            return null;
        }

        AddChatParticipantsResult addChatParticipantsResult = new AddChatParticipantsResult();

        if (obj.getInvalidParticipants() != null) {
            List<ChatError> invalidParticipants
                = new ArrayList<>(obj.getInvalidParticipants().size());
            for (CommunicationError invalidParticipant: obj.getInvalidParticipants()) {
                invalidParticipants.add(ChatErrorConverter.convert(invalidParticipant));
            }
            addChatParticipantsResult.setInvalidParticipants(invalidParticipants);
        }

        return addChatParticipantsResult;
    }

    private AddChatParticipantsResultConverter() {
    }
}
