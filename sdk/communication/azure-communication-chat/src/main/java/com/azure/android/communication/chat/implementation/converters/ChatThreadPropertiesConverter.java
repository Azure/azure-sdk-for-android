// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.models.ChatThreadProperties;
import com.azure.android.core.logging.ClientLogger;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.ChatThreadProperties} and
 * {@link ChatThreadProperties}.
 */
public final class ChatThreadPropertiesConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ChatThread} to {@link ChatThreadProperties}.
     */
    public static ChatThreadProperties convert(com.azure.android.communication.chat.implementation.models.ChatThreadProperties obj,
                                               ClientLogger logger) {
        if (obj == null) {
            return null;
        }

        ChatThreadProperties chatThreadProperties = new ChatThreadProperties()
            .setId(obj.getId())
            .setTopic(obj.getTopic())
            .setCreatedOn(obj.getCreatedOn());

        if (obj.getCreatedByCommunicationIdentifier() != null) {
            chatThreadProperties.setCreatedByCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getCreatedByCommunicationIdentifier(), logger));
        }

        return chatThreadProperties;
    }

    private ChatThreadPropertiesConverter() {
    }
}
