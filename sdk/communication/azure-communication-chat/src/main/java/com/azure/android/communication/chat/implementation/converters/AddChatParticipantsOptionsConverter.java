// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.models.AddChatParticipantsOptions;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.core.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.AddChatParticipantsOptions} and
 * {@link AddChatParticipantsOptions}.
 */
public final class AddChatParticipantsOptionsConverter {
    /**
     * Maps from {AddChatThreadMembersOptions} to
     * {@link com.azure.android.communication.chat.implementation.models.AddChatParticipantsOptions}.
     */
    public static com.azure.android.communication.chat.implementation.models.AddChatParticipantsOptions convert(
        AddChatParticipantsOptions obj,
        ClientLogger logger) {

        if (obj == null) {
            return null;
        }

        List<com.azure.android.communication.chat.implementation.models.ChatParticipant> innerParticipants
            = new ArrayList<>(obj.getParticipants().size());
        for (ChatParticipant participant : obj.getParticipants()) {
            innerParticipants.add(ChatParticipantConverter.convert(participant, logger));
        }

        com.azure.android.communication.chat.implementation.models.AddChatParticipantsOptions addChatThreadMembersOptions
            = new com.azure.android.communication.chat.implementation.models.AddChatParticipantsOptions()
            .setParticipants(innerParticipants);

        return addChatThreadMembersOptions;
    }

    private AddChatParticipantsOptionsConverter() {
    }
}
