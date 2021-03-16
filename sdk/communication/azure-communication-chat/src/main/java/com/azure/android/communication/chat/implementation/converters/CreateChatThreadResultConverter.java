// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.core.logging.ClientLogger;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.CreateChatThreadResult} and
 * {@link CreateChatThreadResult}.
 */
public final class CreateChatThreadResultConverter {
    /**
     * Maps from {@link com.azure.android.communication.chat.implementation.models.CreateChatThreadResult} to
     * {@link CreateChatThreadResult}.
     */
    public static CreateChatThreadResult convert(
        com.azure.android.communication.chat.implementation.models.CreateChatThreadResult obj, ClientLogger logger) {

        if (obj == null) {
            return null;
        }

        CreateChatThreadResult createChatThreadResult = new CreateChatThreadResult()
            .setChatThread(ChatThreadConverter.convert(obj.getChatThread(), logger))
            .setErrors(obj.getInvalidParticipants());

        return createChatThreadResult;
    }

    private CreateChatThreadResultConverter() {
    }
}
