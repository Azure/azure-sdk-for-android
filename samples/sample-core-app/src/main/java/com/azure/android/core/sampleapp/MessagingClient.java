// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.sampleapp;

import com.azure.android.core.util.events.EventHandler;
import com.azure.android.core.util.events.EventHandlerCollection;

public class MessagingClient {
    static final String MESSAGE_RECEIVED = "MessageReceived";
    static final String MESSAGE_SENT = "MessageSent";

    public static class MessageSentEvent {
        private String messageId;
        private String userId;
        private String timestamp;

        public String getMessageId() {
            return messageId;
        }

        public MessageSentEvent setMessageId(String messageId) {
            this.messageId = messageId;

            return this;
        }

        public String getUserId() {
            return userId;
        }

        public MessageSentEvent setUserId(String userId) {
            this.userId = userId;

            return this;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public MessageSentEvent setTimestamp(String timestamp) {
            this.timestamp = timestamp;

            return this;
        }
    }

    public static class MessageReceivedEvent {
        private String messageId;
        private String userId;
        private String timestamp;
        private String contents;

        public String getMessageId() {
            return messageId;
        }

        public MessageReceivedEvent setMessageId(String messageId) {
            this.messageId = messageId;

            return this;
        }

        public String getUserId() {
            return userId;
        }

        public MessageReceivedEvent setUserId(String userId) {
            this.userId = userId;

            return this;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public MessageReceivedEvent setTimestamp(String timestamp) {
            this.timestamp = timestamp;

            return this;
        }

        public String getContents() {
            return contents;
        }

        public MessageReceivedEvent setContents(String contents) {
            this.contents = contents;

            return this;
        }
    }

    public final EventHandlerCollection eventHandlerCollection;

    public MessagingClient() {
        eventHandlerCollection = new EventHandlerCollection();
    }

    public void addOnMessageSentEventHandler(EventHandler<MessageSentEvent> eventHandler) {
        eventHandlerCollection.addEventHandler(MESSAGE_SENT, eventHandler);
    }

    public void addOnMessageReceivedEventHandler(EventHandler<MessageReceivedEvent> eventHandler) {
        eventHandlerCollection.addEventHandler(MESSAGE_RECEIVED, eventHandler);
    }

    public void removeOnMessageSentEventHandler(EventHandler<MessageSentEvent> eventHandler) {
        eventHandlerCollection.removeEventHandler(MESSAGE_SENT, eventHandler);
    }

    public void removeOnMessageReceivedEventHandler(EventHandler<MessageReceivedEvent> eventHandler) {
        eventHandlerCollection.removeEventHandler(MESSAGE_RECEIVED, eventHandler);
    }

    public void removeEventHandler(EventHandler handler) {
        eventHandlerCollection.removeEventHandler(handler);
    }

    public void sendMessage() {
        MessageSentEvent someEvent = new MessageSentEvent()
            .setMessageId("sentMessageId")
            .setTimestamp("912831203")
            .setUserId("myUser");

        eventHandlerCollection.fireEvent(MESSAGE_SENT, someEvent);
    }

    public void receiveMessage() {
        MessageReceivedEvent someEvent = new MessageReceivedEvent()
            .setMessageId("sentMessageId")
            .setTimestamp("912831203")
            .setUserId("myUser")
            .setContents("my contents");

        eventHandlerCollection.fireEvent(MESSAGE_RECEIVED, someEvent);
    }
}
