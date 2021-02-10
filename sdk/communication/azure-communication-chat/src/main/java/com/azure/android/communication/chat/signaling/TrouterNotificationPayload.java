package com.azure.android.communication.chat.signaling;

// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

class BasePayload {
    long _eventId;
    String senderId;
    String recipientId;
    String groupId;
}

class MessageReceivedPayload extends BasePayload {
    String messageId;
    String clientMessageId;
    String senderDisplayName;
    String messageType;
    String messageBody;
    String priority;
    String version;
    String originalArrivalTime;
}

class MessageEditedPayload extends BasePayload {
    String messageId;
    String clientMessageId;
    String senderDisplayName;
    String messageBody;
    String version;
    String edittime;
    String originalArrivalTime;
}

class MessageDeletedPayload extends BasePayload {
    String messageId;
    String clientMessageId;
    String senderDisplayName;
    String version;
    String deletetime;
    String originalArrivalTime;
}

class TypingIndicatorReceivedPayload extends BasePayload {
    String version;
    String originalArrivalTime;
}

class ReadReceiptReceivedPayload extends BasePayload {
    String messageId;
    String clientMessageId;
    String messageBody;
}

class ReadReceiptMessageBody {
    String user;
    String consumptionhorizon;
    long messageVisibilityTime;
    String version;
}

class ChatThreadPayload {
    long _eventId;
    String threadId;
    String version;
}

class ChatParticipantPayload {
    String participantId;
    String displayName;
    String shareHistoryTime;
}

class ChatThreadCreatedPayload extends ChatThreadPayload {
    String createTime;
    String createdBy;
    String members;
    String properties;
}

class ChatThreadPropertiesUpdatedPayload extends ChatThreadPayload {
    String editTime;
    String editedBy;
    String properties;
}

class ChatThreadDeletedPayload extends ChatThreadPayload {
    String deleteTime;
    String deletedBy;
}

class ParticipantsAddedPayload extends ChatThreadPayload {
    String time;
    String addedBy;
    String participantsAdded;
}

class ParticipantsRemovedPayload extends ChatThreadPayload {
    String time;
    String removedBy;
    String participantsRemoved;
}
