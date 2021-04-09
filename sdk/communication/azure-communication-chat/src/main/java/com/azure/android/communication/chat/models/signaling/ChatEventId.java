// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models.signaling;

/**
 * Defines values for chat event.
 */
public enum ChatEventId {
    chatMessageReceived,
    chatMessageEdited,
    chatMessageDeleted,
    typingIndicatorReceived,
    readReceiptReceived,
    chatThreadCreated,
    chatThreadDeleted,
    chatThreadPropertiesUpdated,
    participantsAdded,
    participantsRemoved
}
