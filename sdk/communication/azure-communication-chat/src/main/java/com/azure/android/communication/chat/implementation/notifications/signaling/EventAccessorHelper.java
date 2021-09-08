// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.ChatMessageDeletedEvent;
import com.azure.android.communication.chat.models.ChatMessageEditedEvent;
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent;
import com.azure.android.communication.chat.models.ChatThreadCreatedEvent;
import com.azure.android.communication.chat.models.ChatThreadDeletedEvent;
import com.azure.android.communication.chat.models.ChatThreadPropertiesUpdatedEvent;
import com.azure.android.communication.chat.models.ParticipantsAddedEvent;
import com.azure.android.communication.chat.models.ParticipantsRemovedEvent;
import com.azure.android.communication.chat.models.ReadReceiptReceivedEvent;
import com.azure.android.communication.chat.models.TypingIndicatorReceivedEvent;

public final class EventAccessorHelper {
    private static IAccessor chatMessageDeletedEventAccessor;
    private static IAccessor chatMessageEditedEventAccessor;
    private static IAccessor chatMessageReceivedEventAccessor;
    private static IAccessor readReceiptReceivedEventAccessor;
    private static IAccessor typingIndicatorReceivedEventAccessor;
    private static IAccessor chatThreadCreatedEventAccessor;
    private static IAccessor chatThreadDeletedEventAccessor;
    private static IAccessor chatThreadPropertiesUpdatedEventAccessor;
    private static IAccessor participantsAddedEventAccessor;
    private static IAccessor participantsRemovedEventAccessor;

    public interface IAccessor {
        void set(ChatEvent event);
    }

    public static void setChatMessageDeletedEventAccessor(final IAccessor accessor) {
        chatMessageDeletedEventAccessor = accessor;
    }

    public static void setChatMessageDeletedEvent(ChatMessageDeletedEvent chatMessageDeletedEvent) {
        chatMessageDeletedEventAccessor.set(chatMessageDeletedEvent);
    }

    public static void setChatMessageEditedEventAccessorAccessor(final IAccessor accessor) {
        chatMessageEditedEventAccessor = accessor;
    }

    public static void setChatMessageEditedEvent(ChatMessageEditedEvent chatMessageEditedEvent) {
        chatMessageEditedEventAccessor.set(chatMessageEditedEvent);
    }

    public static void setChatMessageReceivedEventAccessor(final IAccessor accessor) {
        chatMessageReceivedEventAccessor = accessor;
    }

    public static void setChatMessageReceivedEvent(ChatMessageReceivedEvent chatMessageReceivedEvent) {
        chatMessageReceivedEventAccessor.set(chatMessageReceivedEvent);
    }

    public static void setReadReceiptReceivedEventAccessor(final IAccessor accessor) {
        readReceiptReceivedEventAccessor = accessor;
    }

    public static void setReadReceiptReceivedEvent(ReadReceiptReceivedEvent readReceiptReceivedEvent) {
        readReceiptReceivedEventAccessor.set(readReceiptReceivedEvent);
    }

    public static void setTypingIndicatorReceivedEventAccessor(final IAccessor accessor) {
        typingIndicatorReceivedEventAccessor = accessor;
    }

    public static void setTypingIndicatorReceivedEvent(TypingIndicatorReceivedEvent typingIndicatorReceivedEvent) {
        typingIndicatorReceivedEventAccessor.set(typingIndicatorReceivedEvent);
    }

    public static void setChatThreadCreatedEventAccessor(final IAccessor accessor) {
        chatThreadCreatedEventAccessor = accessor;
    }

    public static void setChatThreadCreatedEvent(ChatThreadCreatedEvent chatThreadCreatedEvent) {
        chatThreadCreatedEventAccessor.set(chatThreadCreatedEvent);
    }

    public static void setChatThreadDeletedEventAccessor(final IAccessor accessor) {
        chatThreadDeletedEventAccessor = accessor;
    }

    public static void setChatThreadDeletedEvent(ChatThreadDeletedEvent chatThreadDeletedEvent) {
        chatThreadDeletedEventAccessor.set(chatThreadDeletedEvent);
    }

    public static void setChatThreadPropertiesUpdatedEventAccessor(final IAccessor accessor) {
        chatThreadPropertiesUpdatedEventAccessor = accessor;
    }

    public static void setChatThreadPropertiesUpdatedEvent(
        ChatThreadPropertiesUpdatedEvent chatThreadPropertiesUpdatedEvent) {
        chatThreadPropertiesUpdatedEventAccessor.set(chatThreadPropertiesUpdatedEvent);
    }

    public static void setParticipantsAddedEventAccessor(final IAccessor accessor) {
        participantsAddedEventAccessor = accessor;
    }

    public static void setParticipantsAddedEvent(ParticipantsAddedEvent participantsAddedEvent) {
        participantsAddedEventAccessor.set(participantsAddedEvent);
    }

    public static void setParticipantsRemovedEventAccessor(final IAccessor accessor) {
        participantsRemovedEventAccessor = accessor;
    }

    public static void setParticipantsRemovedEvent(ParticipantsRemovedEvent participantsRemovedEvent) {
        participantsRemovedEventAccessor.set(participantsRemovedEvent);
    }

}
