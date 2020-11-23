package com.azure.android.communication.chat.implementation.converters;

/**
 * A converter between {@link com.azure.android.communication.chat.models.SendChatMessageResult} and
 * {@link String}.
 */
public final class SendChatMessageResultConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.SendChatMessageResult} to {@link String}.
     */
    public static String convert(com.azure.android.communication.chat.models.SendChatMessageResult obj) {
        if (obj == null) {
            return null;
        }

        return obj.getId();
    }

    private SendChatMessageResultConverter() {
    }
}
