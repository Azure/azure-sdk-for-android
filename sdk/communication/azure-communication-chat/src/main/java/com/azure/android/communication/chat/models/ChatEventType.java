// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;

/**
 * Defines values for chat event type.
 */
public final class ChatEventType extends ExpandableStringEnum<ChatEventType> {
    /** Static value chatMessageReceived for ChatMessageType. */
    public static final ChatEventType CHAT_MESSAGE_RECEIVED = fromString("chatMessageReceived");

    /** Static value chatMessageEdited for ChatMessageType. */
    public static final ChatEventType CHAT_MESSAGE_EDITED = fromString("chatMessageEdited");

    /** Static value chatMessageDeleted for ChatMessageType. */
    public static final ChatEventType CHAT_MESSAGE_DELETED = fromString("chatMessageDeleted");

    /** Static value typingIndicatorReceived for ChatMessageType. */
    public static final ChatEventType TYPING_INDICATOR_RECEIVED = fromString("typingIndicatorReceived");

    /** Static value readReceiptReceived for ChatMessageType. */
    public static final ChatEventType READ_RECEIPT_RECEIVED = fromString("readReceiptReceived");

    /** Static value chatThreadCreated for ChatMessageType. */
    public static final ChatEventType CHAT_THREAD_CREATED = fromString("chatThreadCreated");

    /** Static value chatThreadDeleted for ChatMessageType. */
    public static final ChatEventType CHAT_THREAD_DELETED = fromString("chatThreadDeleted");

    /** Static value chatThreadPropertiesUpdated for ChatMessageType. */
    public static final ChatEventType CHAT_THREAD_PROPERTIES_UPDATED = fromString("chatThreadPropertiesUpdated");

    /** Static value participantsAdded for ChatMessageType. */
    public static final ChatEventType PARTICIPANTS_ADDED = fromString("participantsAdded");

    /** Static value participantsRemoved for ChatMessageType. */
    public static final ChatEventType PARTICIPANTS_REMOVED = fromString("participantsRemoved");

    /**
     * Creates or finds a ChatEventType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ChatEventType.
     */
    @JsonCreator
    public static ChatEventType fromString(String name) {
        return fromString(name, ChatEventType.class);
    }

    @Override
    @JsonValue
    public String toString() {
        return super.toString();
    }

    /** @return known ChatEventType values. */
    public static Collection<ChatEventType> values() {
        return values(ChatEventType.class);
    }
}