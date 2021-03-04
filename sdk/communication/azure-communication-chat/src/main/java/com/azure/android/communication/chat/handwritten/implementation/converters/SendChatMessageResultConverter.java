// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.handwritten.implementation.converters;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.SendChatMessageResult} and
 * {@link String}.
 */
public final class SendChatMessageResultConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.SendChatMessageResult} to {@link String}.
     */
    public static String convert(com.azure.android.communication.chat.implementation.models.SendChatMessageResult obj) {
        if (obj == null) {
            return null;
        }

        return obj.getId();
    }

    private SendChatMessageResultConverter() {
    }
}
