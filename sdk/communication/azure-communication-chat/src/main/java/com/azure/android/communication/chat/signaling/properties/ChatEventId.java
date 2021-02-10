package com.azure.android.communication.chat.signaling.properties;

// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.


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
    participantsRemoved,
    connectionChanged;
}

