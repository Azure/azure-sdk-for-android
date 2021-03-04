// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.handwritten.implementation.converters;

import com.azure.android.communication.chat.handwritten.models.CreateChatThreadOptions;

import java.util.stream.Collectors;

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
        CreateChatThreadOptions obj) {

        if (obj == null) {
            return null;
        }

        com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions createChatThreadOptions
            = new com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions()
                .setTopic(obj.getTopic())
                .setParticipants(obj.getParticipants()
                    .stream()
                    .map(member -> ChatParticipantConverter.convert(member))
                    .collect(Collectors.toList()));

        return createChatThreadOptions;
    }

    private CreateChatThreadOptionsConverter() {
    }
}
