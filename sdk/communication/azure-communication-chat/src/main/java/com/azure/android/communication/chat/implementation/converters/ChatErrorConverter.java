// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.implementation.models.CommunicationError;
import com.azure.android.communication.chat.models.ChatError;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link CommunicationError} and
 * {@link ChatError}.
 */
public final class ChatErrorConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.CommunicationError} to {@link ChatError}.
     */
    public static ChatError convert(CommunicationError obj) {
        if (obj == null) {
            return null;
        }

        ChatError chatError = new ChatError()
            .setInnerError(convert(obj.getInnerError()))
            .setCode(obj.getCode())
            .setMessage(obj.getMessage())
            .setTarget(obj.getTarget());

        if (obj.getDetails() != null) {
            List<ChatError> details
                = new ArrayList<>(obj.getDetails().size());
            for (CommunicationError communicationError: obj.getDetails()) {
                details.add(convert(communicationError));
            }
            chatError.setDetails(details);
        }

        return chatError;
    }

    private ChatErrorConverter() {
    }
}
