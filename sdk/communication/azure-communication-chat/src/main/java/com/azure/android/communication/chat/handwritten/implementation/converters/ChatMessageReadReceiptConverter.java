// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.handwritten.implementation.converters;

import com.azure.android.communication.chat.handwritten.models.ChatMessageReadReceipt;

/**
 * A converter between {@link com.azure.android.communication.chat.implementation.models.ChatMessageReadReceipt} and
 * {@link ChatMessageReadReceipt}.
 */
public final class ChatMessageReadReceiptConverter {
    /**
     * Maps from {com.azure.android.communication.chat.implementation.models.ReadReceipt} to {@link ChatMessageReadReceipt}.
     */
    public static ChatMessageReadReceipt convert(
        com.azure.android.communication.chat.implementation.models.ChatMessageReadReceipt obj) {
        if (obj == null) {
            return null;
        }

        ChatMessageReadReceipt readReceipt = new ChatMessageReadReceipt()
            .setSenderCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getSenderCommunicationIdentifier()))
            .setChatMessageId(obj.getChatMessageId())
            .setReadOn(obj.getReadOn());

        return readReceipt;
    }

    private ChatMessageReadReceiptConverter() {
    }
}
