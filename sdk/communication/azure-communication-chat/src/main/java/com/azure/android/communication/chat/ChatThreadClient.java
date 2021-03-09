// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.AddChatParticipantsOptions;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.util.Context;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

/**
 * Sync Client that supports chat thread operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = false)
public final class ChatThreadClient {
    private final ClientLogger logger = new ClientLogger(ChatThreadClient.class);

    private final ChatThreadAsyncClient client;

    private final String chatThreadId;

    /**
     * Creates a ChatClient that sends requests to the chat service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link ChatAsyncClient} that the client routes its request through.
     */
    ChatThreadClient(ChatThreadAsyncClient client) {
        this.client = client;
        this.chatThreadId = client.getChatThreadId();
    }

    /**
     * Get the thread id property.
     *
     * @return the thread id value.
     */
    public String getChatThreadId() {
        return chatThreadId;
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateTopic(String topic) {
        block(this.client.updateTopic(topic));
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateTopicWithResponse(String topic, Context context) {
        return block(this.client.updateTopic(topic, context));
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addParticipants(AddChatParticipantsOptions options) {
        block(this.client.addParticipants(options));
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddChatParticipantsResult> addParticipantsWithResponse(
        AddChatParticipantsOptions options, Context context) {
        return block(this.client.addParticipants(options, context));
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addParticipant(ChatParticipant participant) {
        block(this.client.addParticipants(new AddChatParticipantsOptions()
            .setParticipants(Collections.singletonList(participant))));
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddChatParticipantsResult> addParticipantWithResponse(ChatParticipant participant,
                                                                          Context context) {
        return block(this.client.addParticipants(new AddChatParticipantsOptions()
            .setParticipants(Collections.singletonList(participant)), context));
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(CommunicationIdentifier identifier, Context context) {
        return block(this.client.removeParticipant(identifier, context));
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the thread participant to remove from the thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipant(CommunicationIdentifier identifier) {
        block(this.client.removeParticipant(identifier));
    }

//    /**
//     * Gets the participants of a thread.
//     *
//     * @return the participants of a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatParticipant> listParticipants() {
//
//        return new PagedIterable<>(this.client.listParticipants());
//    }
//
//    /**
//     * Gets the participants of a thread.
//     *
//     * @param listParticipantsOptions The request options.
//     * @return the participants of a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions) {
//        return new PagedIterable<>(this.client.listParticipants(listParticipantsOptions));
//    }
//
//    /**
//     * Gets the participants of a thread.
//     *
//     * @param context The context to associate with this operation.
//     * @return the participants of a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatParticipant> listParticipants(Context context) {
//
//        return new PagedIterable<>(this.client.listParticipants(context));
//    }
//
//    /**
//     * Gets the participants of a thread.
//     *
//     * @param listParticipantsOptions The request options.
//     * @param context The context to associate with this operation.
//     * @return the participants of a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions,
//                                                           Context context) {
//        return new PagedIterable<>(this.client.listParticipants(listParticipantsOptions, context));
//    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @param context The context to associate with this operation.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> sendMessageWithResponse(SendChatMessageOptions options, Context context) {
        return block(this.client.sendMessage(options, context));
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String sendMessage(SendChatMessageOptions options) {
        return block(this.client.sendMessage(options));
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ChatMessage> getMessageWithResponse(String chatMessageId, Context context) {
        return block(this.client.getMessage(chatMessageId, context));
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatMessage getMessage(String chatMessageId) {
        return block(this.client.getMessage(chatMessageId));
    }

//    /**
//     * Gets a list of messages from a thread.
//     *
//     * @return a list of messages from a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatMessage> listMessages() {
//
//        return new PagedIterable<>(this.client.listMessages());
//    }
//
//    /**
//     * Gets a list of messages from a thread.
//     *
//     * @param listMessagesOptions The request options.
//     * @param context The context to associate with this operation.
//     * @return a list of messages from a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions, Context context) {
//
//        return new PagedIterable<>(this.client.listMessages(listMessagesOptions, context));
//    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateMessageWithResponse(
        String chatMessageId, UpdateChatMessageOptions options, Context context) {
        return block(this.client.updateMessage(chatMessageId, options, context));
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateMessage(String chatMessageId, UpdateChatMessageOptions options) {
        block(this.client.updateMessage(chatMessageId, options));
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteMessageWithResponse(String chatMessageId, Context context) {
        return block(this.client.deleteMessage(chatMessageId, context));
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteMessage(String chatMessageId) {
        block(this.client.deleteMessage(chatMessageId));
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendTypingNotificationWithResponse(Context context) {
        return block(this.client.sendTypingNotification(context));
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendTypingNotification() {
        block(this.client.sendTypingNotification());
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendReadReceiptWithResponse(String chatMessageId, Context context) {
        return block(this.client.sendReadReceipt(chatMessageId, context));
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendReadReceipt(String chatMessageId) {
        block(this.client.sendReadReceipt(chatMessageId));
    }

//    /**
//     * Gets read receipts for a thread.
//     *
//     * @return read receipts for a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatMessageReadReceipt> listReadReceipts() {
//
//        return new PagedIterable<>(this.client.listReadReceipts());
//    }
//
//    /**
//     * Gets read receipts for a thread.
//     *
//     * @param listReadReceiptOptions The additional options for this operation.
//     * @return read receipts for a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions) {
//        return new PagedIterable<>(this.client.listReadReceipts(listReadReceiptOptions));
//    }
//
//    /**
//     * Gets read receipts for a thread.
//     *
//     * @param context The context to associate with this operation.
//     * @return read receipts for a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(Context context) {
//
//        return new PagedIterable<>(this.client.listReadReceipts(context));
//    }
//
//    /**
//     * Gets read receipts for a thread.
//     *
//     * @param listReadReceiptOptions The additional options for this operation.
//     * @param context The context to associate with this operation.
//     * @return read receipts for a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions,
//                                                                  Context context) {
//        return new PagedIterable<>(this.client.listReadReceipts(listReadReceiptOptions, context));
//    }

    private <T> T block(CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        } catch (ExecutionException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
