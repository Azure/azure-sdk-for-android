// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

/**
 * Defines values for chat event.
 */
public enum ChatEventType {
    CHAT_MESSAGE_RECEIVED,
    CHAT_MESSAGE_EDITED,
    CHAT_MESSAGE_DELETED,
    TYPING_INDICATOR_RECEIVED,
    READ_RECEIPT_RECEIVED,
    CHAT_THREAD_CREATED,
    CHAT_THREAD_DELETED,
    CHAT_THREAD_PROPERTIES_UPDATED,
    PARTICIPANTS_ADDED,
    PARTICIPANTS_REMOVED
}
