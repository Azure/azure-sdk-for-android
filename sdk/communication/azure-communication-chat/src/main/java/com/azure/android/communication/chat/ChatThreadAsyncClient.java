// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.ChatThreadImpl;
import com.azure.android.communication.chat.implementation.converters.AddChatParticipantsOptionsConverter;
import com.azure.android.communication.chat.implementation.converters.ChatMessageConverter;
import com.azure.android.communication.chat.implementation.converters.CommunicationIdentifierConverter;
import com.azure.android.communication.chat.implementation.converters.SendChatMessageResultConverter;
import com.azure.android.communication.chat.implementation.models.SendReadReceiptRequest;
import com.azure.android.communication.chat.models.AddChatParticipantsOptions;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatThreadOptions;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.util.Context;

import java.util.Collections;

import java9.util.concurrent.CompletableFuture;

/**
 * Async Client that supports chat thread operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatThreadAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatThreadAsyncClient.class);

    private final ChatThreadImpl chatThreadClient;
    private final String chatThreadId;

    ChatThreadAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient, String chatThreadId) {
        this.chatThreadClient = chatServiceClient.getChatThreadClient();
        this.chatThreadId = chatThreadId;
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
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> updateTopic(String topic) {
        if (topic == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'topic' cannot be null."));
        }
        return this.updateTopic(topic, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Updates a thread's properties.
     *
     * @param topic The new topic.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> updateTopicWithResponse(String topic) {
        if (topic == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'topic' cannot be null."));
        }
        return this.updateTopic(topic, null);
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> updateTopic(String topic, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.updateChatThreadWithResponseAsync(
            chatThreadId,
            new UpdateChatThreadOptions().setTopic(topic),
            context
        );
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> addParticipants(AddChatParticipantsOptions options) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.addParticipants(options, null)
            .thenApply(response -> {
                return null;
            });
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<AddChatParticipantsResult>> addParticipantsWithResponse(
        AddChatParticipantsOptions options) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.addParticipants(options, null);
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> addParticipant(ChatParticipant participant) {
        return this.addParticipants(
            new AddChatParticipantsOptions().setParticipants(Collections.singletonList(participant)),
            null)
            .thenApply(response -> {
                return null;
            });
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<AddChatParticipantsResult>> addParticipantWithResponse(
        ChatParticipant participant) {
        return this.addParticipants(
            new AddChatParticipantsOptions().setParticipants(Collections.singletonList(participant)),
            null);
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @param context The context to associate with this operation.
     * @return the result.
     */
    CompletableFuture<Response<AddChatParticipantsResult>> addParticipants(AddChatParticipantsOptions
                                                                               options, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.addChatParticipantsWithResponseAsync(
            this.chatThreadId, AddChatParticipantsOptionsConverter.convert(options, this.logger), context);
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> removeParticipant(CommunicationIdentifier identifier) {
        if (identifier == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'identifier' cannot be null."));
        }
        return this.removeParticipant(identifier, null).thenApply(response -> {
            return response.getValue();
        });
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> removeParticipantWithResponse(CommunicationIdentifier identifier) {
        if (identifier == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'identifier' cannot be null."));
        }
        return this.removeParticipant(identifier, null);
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> removeParticipant(CommunicationIdentifier identifier, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.removeChatParticipantWithResponseAsync(
            chatThreadId, CommunicationIdentifierConverter.convert(identifier, this.logger), context);
    }

//    /**
//     * Gets the participants of a thread.
//     *
//     * @return the participants of a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatParticipant> listParticipants() {
//        ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions();
//        return listParticipants(listParticipantsOptions);
//    }
//
//    /**
//     * Gets the participants of a thread.
//     *
//     * @param listParticipantsOptions The request options.
//     * @return the participants of a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions) {
//        final ListParticipantsOptions serviceListParticipantsOptions =
//            listParticipantsOptions == null ? new ListParticipantsOptions() : listParticipantsOptions;
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () -> withContext(context ->
//                        this.chatThreadClient.listChatParticipantsSinglePageAsync(
//                            chatThreadId,
//                            serviceListParticipantsOptions.getMaxPageSize(),
//                            serviceListParticipantsOptions.getSkip(),
//                            context)),
//                    nextLink -> withContext(context ->
//                        this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, context))),
//                f -> ChatParticipantConverter.convert(f));
//        } catch (RuntimeException ex) {
//
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    /**
//     * Gets the participants of a thread.
//     *
//     * @param context The context to associate with this operation.
//     * @return the participants of a thread.
//     */
//    PagedFlux<ChatParticipant> listParticipants(Context context) {
//        ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions();
//        return listParticipants(listParticipantsOptions, context);
//    }
//
//    /**
//     * Gets the participants of a thread.
//     *
//     * @param context The context to associate with this operation.
//     * @param listParticipantsOptions The request options.
//     * @return the participants of a thread.
//     */
//    PagedFlux<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions, Context context) {
//        final Context serviceContext = context == null ? Context.NONE : context;
//        final ListParticipantsOptions serviceListParticipantsOptions =
//            listParticipantsOptions == null ? new ListParticipantsOptions() : listParticipantsOptions;
//
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () ->
//                        this.chatThreadClient.listChatParticipantsSinglePageAsync(
//                            chatThreadId,
//                            serviceListParticipantsOptions.getMaxPageSize(),
//                            serviceListParticipantsOptions.getSkip(),
//                            serviceContext),
//                    nextLink ->
//                        this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, serviceContext)),
//                f -> ChatParticipantConverter.convert(f));
//        } catch (RuntimeException ex) {
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<String> sendMessage(SendChatMessageOptions options) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.sendMessage(options, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<String>> sendMessageWithResponse(SendChatMessageOptions options) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.sendMessage(options, null);
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @param context The context to associate with this operation.
     * @return the MessageId.
     */
    CompletableFuture<Response<String>> sendMessage(SendChatMessageOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.sendChatMessageWithResponseAsync(this.chatThreadId, options, context)
            .thenApply(result -> {
                return new SimpleResponse<>(result,
                    SendChatMessageResultConverter.convert(result.getValue(), this.logger));
            });
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<ChatMessage> getMessage(String chatMessageId) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.getMessage(chatMessageId, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<ChatMessage>> getMessageWithResponse(String chatMessageId) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.getMessage(chatMessageId, null);
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return a message by id.
     */
    CompletableFuture<Response<ChatMessage>> getMessage(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.getChatMessageWithResponseAsync(chatThreadId, chatMessageId, context)
            .thenApply(result -> new SimpleResponse<>(result, ChatMessageConverter.convert(result.getValue(),
                this.logger)));
    }

//    /**
//     * Gets a list of messages from a thread.
//     *
//     * @return a paged list of messages from a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatMessage> listMessages() {
//        ListChatMessagesOptions listMessagesOptions = new ListChatMessagesOptions();
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () -> withContext(context ->  this.chatThreadClient.listChatMessagesSinglePageAsync(
//                        chatThreadId, listMessagesOptions.getMaxPageSize(), listMessagesOptions.getStartTime(),
//                        context)),
//                    nextLink -> withContext(context -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
//                        nextLink, context))),
//                f -> ChatMessageConverter.convert(f));
//        } catch (RuntimeException ex) {
//
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    /**
//     * Gets a list of messages from a thread.
//     *
//     * @param listMessagesOptions The request options.
//     * @return a paged list of messages from a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions) {
//        final ListChatMessagesOptions serviceListMessagesOptions =
//            listMessagesOptions == null ? new ListChatMessagesOptions() : listMessagesOptions;
//
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () -> withContext(context ->  this.chatThreadClient.listChatMessagesSinglePageAsync(
//                        chatThreadId,
//                        serviceListMessagesOptions.getMaxPageSize(),
//                        serviceListMessagesOptions.getStartTime(),
//                        context)),
//                    nextLink -> withContext(context -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
//                        nextLink, context))),
//                f -> ChatMessageConverter.convert(f));
//        } catch (RuntimeException ex) {
//
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    /**
//     * Gets a list of messages from a thread.
//     *
//     * @param listMessagesOptions The request options.
//     * @param context The context to associate with this operation.
//     * @return a paged list of messages from a thread.
//     */
//    PagedFlux<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions, Context context) {
//        final ListChatMessagesOptions serviceListMessagesOptions
//            = listMessagesOptions == null ? new ListChatMessagesOptions() : listMessagesOptions;
//        final Context serviceContext = context == null ? Context.NONE : context;
//
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () ->  this.chatThreadClient.listChatMessagesSinglePageAsync(
//                        chatThreadId,
//                        serviceListMessagesOptions.getMaxPageSize(),
//                        serviceListMessagesOptions.getStartTime(),
//                        serviceContext),
//                    nextLink -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
//                        nextLink, serviceContext)),
//                f -> ChatMessageConverter.convert(f));
//        } catch (RuntimeException ex) {
//
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> updateMessage(String chatMessageId, UpdateChatMessageOptions options) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.updateMessage(chatMessageId, options, null)
            .thenApply(response -> response.getValue());
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> updateMessageWithResponse(String chatMessageId,
                                                                       UpdateChatMessageOptions options) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.updateMessage(chatMessageId, options, null);
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> updateMessage(String chatMessageId, UpdateChatMessageOptions options,
                                                    Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.updateChatMessageWithResponseAsync(chatThreadId, chatMessageId, options, context);
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> deleteMessage(String chatMessageId) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.deleteMessage(chatMessageId, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> deleteMessageWithResponse(String chatMessageId) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.deleteMessage(chatMessageId, null);
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> deleteMessage(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.deleteChatMessageWithResponseAsync(chatThreadId, chatMessageId, context);
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> sendTypingNotification() {
        return this.sendTypingNotification(null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendTypingNotificationWithResponse() {
        return this.sendTypingNotification(null);
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> sendTypingNotification(Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.sendTypingNotificationWithResponseAsync(chatThreadId, context);
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> sendReadReceipt(String chatMessageId) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.sendReadReceipt(chatMessageId, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendReadReceiptWithResponse(String chatMessageId) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.sendReadReceipt(chatMessageId, null);
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> sendReadReceipt(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;
        SendReadReceiptRequest request = new SendReadReceiptRequest()
            .setChatMessageId(chatMessageId);
        return this.chatThreadClient.sendChatReadReceiptWithResponseAsync(chatThreadId, request, context);
    }

//    /**
//     * Gets read receipts for a thread.
//     *
//     * @return read receipts for a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatMessageReadReceipt> listReadReceipts() {
//        ListReadReceiptOptions listReadReceiptOptions = new ListReadReceiptOptions();
//        return listReadReceipts(listReadReceiptOptions);
//    }
//
//    /**
//     * Gets read receipts for a thread.
//     *
//     * @param listReadReceiptOptions The additional options for this operation.
//     * @return read receipts for a thread.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions) {
//        final ListReadReceiptOptions serviceListReadReceiptOptions =
//            listReadReceiptOptions == null ? new ListReadReceiptOptions() : listReadReceiptOptions;
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () -> withContext(context ->  this.chatThreadClient.listChatReadReceiptsSinglePageAsync(
//                        chatThreadId,
//                        serviceListReadReceiptOptions.getMaxPageSize(),
//                        serviceListReadReceiptOptions.getSkip(),
//                        context)),
//                    nextLink -> withContext(context -> this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(
//                        nextLink, context))),
//                f -> ChatMessageReadReceiptConverter.convert(f));
//        } catch (RuntimeException ex) {
//
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    /**
//     * Gets read receipts for a thread.
//     *
//     * @param context The context to associate with this operation.
//     *
//     * @return read receipts for a thread.
//     */
//    PagedFlux<ChatMessageReadReceipt> listReadReceipts(Context context) {
//        ListReadReceiptOptions listReadReceiptOptions = new ListReadReceiptOptions();
//        return listReadReceipts(listReadReceiptOptions, context);
//    }
//
//    /**
//     * Gets read receipts for a thread.
//     *
//     * @param listReadReceiptOptions The additional options for this operation.
//     * @param context The context to associate with this operation.
//     *
//     * @return read receipts for a thread.
//     */
//    PagedFlux<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions,
//                                                       Context context) {
//        final Context serviceContext = context == null ? Context.NONE : context;
//        final ListReadReceiptOptions serviceListReadReceiptOptions =
//            listReadReceiptOptions == null ? new ListReadReceiptOptions() : listReadReceiptOptions;
//
//        try {
//            return pagedFluxConvert(new PagedFlux<>(
//                    () -> this.chatThreadClient.listChatReadReceiptsSinglePageAsync(
//                        chatThreadId,
//                        serviceListReadReceiptOptions.getMaxPageSize(),
//                        serviceListReadReceiptOptions.getSkip(),
//                        serviceContext),
//                    nextLink -> this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(
//                        nextLink, serviceContext)),
//                f -> ChatMessageReadReceiptConverter.convert(f));
//        } catch (RuntimeException ex) {
//
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    private <T1, T2> PagedFlux<T1> pagedFluxConvert(PagedFlux<T2> originalPagedFlux, Function<T2, T1> func) {
//
//        final Function<PagedResponse<T2>,
//            PagedResponse<T1>> responseMapper
//            = response -> new PagedResponseBase<Void, T1>(response.getRequest(),
//            response.getStatusCode(),
//            response.getHeaders(),
//            response.getValue()
//                .stream()
//                .map(value -> func.apply(value)).collect(Collectors.toList()),
//            response.getContinuationToken(),
//            null);
//
//        final Supplier<PageRetriever<String, PagedResponse<T1>>> provider = () ->
//            (continuationToken, pageSize) -> {
//                Flux<PagedResponse<T2>> flux
//                    = (continuationToken == null)
//                    ? originalPagedFlux.byPage()
//                    : originalPagedFlux.byPage(continuationToken);
//                return flux.map(responseMapper);
//            };
//
//        return PagedFlux.create(provider);
//    }
}
