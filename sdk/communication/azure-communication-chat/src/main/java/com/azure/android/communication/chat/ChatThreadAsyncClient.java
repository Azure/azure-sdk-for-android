// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.ChatThreadImpl;
import com.azure.android.communication.chat.implementation.converters.AddChatParticipantsResultConverter;
import com.azure.android.communication.chat.implementation.converters.AddChatParticipantsOptionsConverter;
import com.azure.android.communication.chat.implementation.converters.ChatMessageConverter;
import com.azure.android.communication.chat.implementation.converters.ChatMessageReadReceiptConverter;
import com.azure.android.communication.chat.implementation.converters.ChatParticipantConverter;
import com.azure.android.communication.chat.implementation.converters.ChatThreadPropertiesConverter;
import com.azure.android.communication.chat.implementation.converters.CommunicationErrorResponseExceptionConverter;
import com.azure.android.communication.chat.implementation.converters.CommunicationIdentifierConverter;
import com.azure.android.communication.chat.implementation.models.SendReadReceiptRequest;
import com.azure.android.communication.chat.models.AddChatParticipantsOptions;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatError;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThreadProperties;
import com.azure.android.communication.chat.models.InvalidParticipantException;
import com.azure.android.communication.chat.models.ListChatMessagesOptions;
import com.azure.android.communication.chat.models.ListParticipantsOptions;
import com.azure.android.communication.chat.models.ListReadReceiptOptions;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.SendChatMessageResult;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatThreadOptions;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.rest.util.paging.PagedResponseBase;
import com.azure.android.core.util.Context;
import com.azure.android.core.util.paging.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java9.util.concurrent.CompletableFuture;

/**
 * Async Client that supports chat thread operations.
 */
@ServiceClient(builder = ChatThreadClientBuilder.class, isAsync = true)
public final class ChatThreadAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatThreadAsyncClient.class);

    private final ChatThreadImpl chatThreadClient;
    private final String chatThreadId;

    ChatThreadAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient, String chatThreadId) {
        this.chatThreadClient = chatServiceClient.getChatThreadClient();
        this.chatThreadId = chatThreadId;
    }

    /**
     * Get the thread id.
     *
     * @return the thread id.
     */
    public String getChatThreadId() {
        return chatThreadId;
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     *
     * @return the {@link CompletableFuture} that signals the update result.
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
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> updateTopicWithResponse(String topic, Context context) {
        if (topic == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'topic' cannot be null."));
        }
        return this.updateTopic(topic, context);
    }

    /**
     * Gets chat thread properties.
     *
     * @return the {@link CompletableFuture} that emits the thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<ChatThreadProperties> getProperties() {
        return this.getProperties(null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Gets chat thread properties.
     *
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<ChatThreadProperties>> getPropertiesWithResponse(Context context) {
        return this.getProperties(context);
    }

    /**
     * Gets chat thread properties.
     *
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the thread properties.
     */
    CompletableFuture<Response<ChatThreadProperties>> getProperties(Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.getChatThreadPropertiesWithResponseAsync(this.chatThreadId, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(result -> {
                return new SimpleResponse<ChatThreadProperties>(result,
                    ChatThreadPropertiesConverter.convert(result.getValue(), this.logger));
            });
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    CompletableFuture<Response<Void>> updateTopic(String topic, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.updateChatThreadPropertiesWithResponseAsync(
            chatThreadId,
            new UpdateChatThreadOptions().setTopic(topic),
            context
        ).exceptionally(throwable -> {
            throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
        });
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     *
     * @return the {@link CompletableFuture} that signals the operation result.
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
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<AddChatParticipantsResult>> addParticipantsWithResponse(
        AddChatParticipantsOptions options, Context context) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.addParticipants(options, context);
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     *
     * @return the {@link CompletableFuture} that signals the operation result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> addParticipant(ChatParticipant participant) {
        return this.addParticipant(participant, null)
            .thenApply(response -> {
                return null;
            });
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> addParticipantWithResponse(
        ChatParticipant participant, Context context) {
        return this.addParticipant(participant, context);
    }

    /**
     * Adds a participant to a thread. If participants already exist, no change occurs.
     *
     * @param participant The new participant.
     * @param context The context to associate with this operation.
     * @throws InvalidParticipantException thrown if the participant is rejected by the server.
     *
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    CompletableFuture<Response<Void>> addParticipant(ChatParticipant participant, Context context) {
        context = context == null ? Context.NONE : context;
        return this.addParticipants(
            new AddChatParticipantsOptions().setParticipants(Collections.singletonList(participant)),
            context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(result -> {
                if (result.getValue().getInvalidParticipants() != null) {
                    if (result.getValue().getInvalidParticipants().size() > 0) {
                        ChatError error = result.getValue().getInvalidParticipants().get(0);
                        throw logger.logExceptionAsError(new InvalidParticipantException(error));
                    }
                }
                return new SimpleResponse<>(result, null);
            });
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    CompletableFuture<Response<AddChatParticipantsResult>> addParticipants(AddChatParticipantsOptions options,
                                                                           Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.addChatParticipantsWithResponseAsync(
            this.chatThreadId, AddChatParticipantsOptionsConverter.convert(options, this.logger), context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(result -> {
                return new SimpleResponse<>(result,
                    AddChatParticipantsResultConverter.convert(result.getValue(), this.logger));
            });
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier identity of the participant to remove from the thread.
     *
     * @return the {@link CompletableFuture} that signals the operation result.
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
     * @param identifier identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the remove request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> removeParticipantWithResponse(CommunicationIdentifier identifier,
                                                                           Context context) {
        if (identifier == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'identifier' cannot be null."));
        }
        return this.removeParticipant(identifier, context);
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the remove request.
     */
    CompletableFuture<Response<Void>> removeParticipant(CommunicationIdentifier identifier, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.removeChatParticipantWithResponseAsync(
            chatThreadId, CommunicationIdentifierConverter.convert(identifier, this.logger), context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets the list of the thread participants in the first page.
     *
     * @return the {@link CompletableFuture} that emits list of thread participants in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatParticipant>> getParticipantsFirstPage() {
        ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions();
        return this.getParticipantsFirstPage(listParticipantsOptions, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of the thread participants in the first page.
     *
     * @param listParticipantsOptions the list options.
     *
     * @return the {@link CompletableFuture} that emits list of thread participants in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatParticipant>> getParticipantsFirstPage(
        ListParticipantsOptions listParticipantsOptions) {
        if (listParticipantsOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listParticipantsOptions is required."));
        }
        return this.getParticipantsFirstPage(listParticipantsOptions, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of the thread participants in the first page.
     *
     * @param listParticipantsOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread participants
     * in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatParticipant>> getParticipantsFirstPageWithResponse(
        ListParticipantsOptions listParticipantsOptions,
        Context context) {
        if (listParticipantsOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listParticipantsOptions is required."));
        }
        return this.getParticipantsFirstPage(listParticipantsOptions, context);
    }

    /**
     * Gets the list of the thread participants in the first page.
     *
     * @param listParticipantsOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread participants
     * in the first page.
     */
    CompletableFuture<PagedResponse<ChatParticipant>> getParticipantsFirstPage(
        ListParticipantsOptions listParticipantsOptions,
        Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.listChatParticipantsSinglePageAsync(this.chatThreadId,
            listParticipantsOptions.getMaxPageSize(),
            listParticipantsOptions.getSkip(), context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(response -> {
                List<ChatParticipant> participants = new ArrayList<>();
                if (response.getValue() != null) {
                    for (com.azure.android.communication.chat.implementation.models.ChatParticipant innerParticipant
                        : response.getValue()) {
                        participants.add(ChatParticipantConverter.convert(innerParticipant, this.logger));
                    }
                }
                return new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    participants,
                    response.getContinuationToken(),
                    null);
            });
    }

    /**
     * Gets the list of the thread participants in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the list of thread participants in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatParticipant>> getParticipantsNextPage(String nextLink) {
        return this.getParticipantsNextPage(nextLink, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of the thread participants in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread participants in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatParticipant>> getParticipantsNextPageWithResponse(String nextLink,
                                                                                                 Context context) {
        return this.getParticipantsNextPage(nextLink, context);
    }

    /**
     * Gets the list of the thread participants in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread participants in the page.
     */
    CompletableFuture<PagedResponse<ChatParticipant>> getParticipantsNextPage(String nextLink,
                                                                              Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(response -> {
                List<ChatParticipant> participants = new ArrayList<>();
                if (response.getValue() != null) {
                    for (com.azure.android.communication.chat.implementation.models.ChatParticipant innerParticipant
                        : response.getValue()) {
                        participants.add(ChatParticipantConverter.convert(innerParticipant, this.logger));
                    }
                }
                return new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    participants,
                    response.getContinuationToken(),
                    null);
            });
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     *
     * @return the {@link CompletableFuture} that emits the id of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<SendChatMessageResult> sendMessage(SendChatMessageOptions options) {
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
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the id of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<SendChatMessageResult>> sendMessageWithResponse(SendChatMessageOptions options,
                                                                       Context context) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.sendMessage(options, context);
    }

    /**
     * Sends a message to a thread.
     *
     * @param options options for sending the message.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the id of the message.
     */
    CompletableFuture<Response<SendChatMessageResult>> sendMessage(SendChatMessageOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.sendChatMessageWithResponseAsync(this.chatThreadId, options, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     *
     * @return the {@link CompletableFuture} that emits the the message.
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
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<ChatMessage>> getMessageWithResponse(String chatMessageId, Context context) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.getMessage(chatMessageId, context);
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId the message id.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the message.
     */
    CompletableFuture<Response<ChatMessage>> getMessage(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.getChatMessageWithResponseAsync(chatThreadId, chatMessageId, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(result -> new SimpleResponse<>(result, ChatMessageConverter.convert(result.getValue(),
                this.logger)));
    }

    /**
     * Gets the list of thread messages in the first page.
     *
     * @return the {@link CompletableFuture} that emits list of thread messages in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatMessage>> getMessagesFirstPage() {
        ListChatMessagesOptions listMessagesOptions = new ListChatMessagesOptions();
        return this.getMessagesFirstPage(listMessagesOptions, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of thread messages in the first page.
     *
     * @param listChatMessagesOptions the list options.
     *
     * @return the {@link CompletableFuture} that emits list of thread messages in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatMessage>> getMessagesFirstPage(
        ListChatMessagesOptions listChatMessagesOptions) {
        if (listChatMessagesOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listChatMessagesOptions is required."));
        }
        return this.getMessagesFirstPage(listChatMessagesOptions, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of thread messages in the first page.
     *
     * @param listMessagesOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread messages
     * in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatMessage>> getMessagesFirstPageWithResponse(
        ListChatMessagesOptions listMessagesOptions,
        Context context) {
        if (listMessagesOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listChatMessagesOptions is required."));
        }
        return this.getMessagesFirstPage(listMessagesOptions, context);
    }

    /**
     * Gets the list of thread messages in the first page.
     *
     * @param listChatMessagesOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread messages
     * in the first page.
     */
    CompletableFuture<PagedResponse<ChatMessage>> getMessagesFirstPage(
        ListChatMessagesOptions listChatMessagesOptions,
        Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.listChatMessagesSinglePageAsync(this.chatThreadId,
            listChatMessagesOptions.getMaxPageSize(),
            listChatMessagesOptions.getStartTime(), context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(response -> {
                List<ChatMessage> messages = new ArrayList<>();
                if (response.getValue() != null) {
                    for (com.azure.android.communication.chat.implementation.models.ChatMessage innerMessage
                        : response.getValue()) {
                        messages.add(ChatMessageConverter.convert(innerMessage, this.logger));
                    }
                }
                return new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    messages,
                    response.getContinuationToken(),
                    null);
            });
    }

    /**
     * Gets the list of the thread messages in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the list of thread messages in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatMessage>> getMessagesNextPage(String nextLink) {
        return this.getMessagesNextPage(nextLink, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of the thread messages in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread messages in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatMessage>> getMessagesNextPageWithResponse(String nextLink,
                                                                                         Context context) {
        return this.getMessagesNextPage(nextLink, context);
    }

    /**
     * Gets the list of the thread messages in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread messages in the page.
     */
    CompletableFuture<PagedResponse<ChatMessage>> getMessagesNextPage(String nextLink,
                                                                      Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.listChatMessagesNextSinglePageAsync(nextLink, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(response -> {
                List<ChatMessage> messages = new ArrayList<>();
                if (response.getValue() != null) {
                    for (com.azure.android.communication.chat.implementation.models.ChatMessage innerMessage
                        : response.getValue()) {
                        messages.add(ChatMessageConverter.convert(innerMessage, this.logger));
                    }
                }
                return new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    messages,
                    response.getContinuationToken(),
                    null);
            });
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId the message id.
     * @param options options for updating the message.
     *
     * @return the {@link CompletableFuture} that signals the update result.
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
     * @param chatMessageId the message id.
     * @param options options for updating the message.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> updateMessageWithResponse(String chatMessageId,
                                                                       UpdateChatMessageOptions options,
                                                                       Context context) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.updateMessage(chatMessageId, options, context);
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId the message id.
     * @param options options for updating the message.
     * @param context the context to associate with this operation.
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    CompletableFuture<Response<Void>> updateMessage(String chatMessageId,
                                                    UpdateChatMessageOptions options,
                                                    Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.updateChatMessageWithResponseAsync(chatThreadId, chatMessageId, options, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId the message id.
     *
     * @return the {@link CompletableFuture} that signals the result of deletion.
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
     * @param chatMessageId the message id.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> deleteMessageWithResponse(String chatMessageId, Context context) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.deleteMessage(chatMessageId, context);
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId the message id.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    CompletableFuture<Response<Void>> deleteMessage(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.deleteChatMessageWithResponseAsync(chatThreadId, chatMessageId, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @return the {@link CompletableFuture} that signals the result of the operation.
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
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendTypingNotificationWithResponse(Context context) {
        return this.sendTypingNotification(context);
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    CompletableFuture<Response<Void>> sendTypingNotification(Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.sendTypingNotificationWithResponseAsync(chatThreadId, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId the id of the chat message that was read.
     *
     * @return the {@link CompletableFuture} that signals the result of the operation.
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
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendReadReceiptWithResponse(String chatMessageId, Context context) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.sendReadReceipt(chatMessageId, context);
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    CompletableFuture<Response<Void>> sendReadReceipt(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;
        SendReadReceiptRequest request = new SendReadReceiptRequest()
            .setChatMessageId(chatMessageId);
        return this.chatThreadClient.sendChatReadReceiptWithResponseAsync(chatThreadId, request, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets the list of thread read receipts in the first page.
     *
     * @return the {@link CompletableFuture} that emits list of thread read receipts in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatMessageReadReceipt>> getReadReceiptsFirstPage() {
        ListReadReceiptOptions listReadReceiptsOptions = new ListReadReceiptOptions();
        return this.getReadReceiptsFirstPage(listReadReceiptsOptions, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of thread read receipts in the first page.
     *
     * @param listReadReceiptOptions the list options.
     *
     * @return the {@link CompletableFuture} that emits list of thread read receipts in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatMessageReadReceipt>> getReadReceiptsFirstPage(
        ListReadReceiptOptions listReadReceiptOptions) {
        if (listReadReceiptOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listReadReceiptOptions is required."));
        }
        return this.getReadReceiptsFirstPage(listReadReceiptOptions, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of thread read receipts in the first page.
     *
     * @param listReadReceiptOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread read receipts
     * in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatMessageReadReceipt>> getReadReceiptsFirstPageWithResponse(
        ListReadReceiptOptions listReadReceiptOptions,
        Context context) {
        if (listReadReceiptOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listReadReceiptOptions is required."));
        }
        return this.getReadReceiptsFirstPage(listReadReceiptOptions, context);
    }

    /**
     * Gets the list of thread read receipts in the first page.
     *
     * @param listReadReceiptOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread read receipts
     * in the first page.
     */
    CompletableFuture<PagedResponse<ChatMessageReadReceipt>> getReadReceiptsFirstPage(
        ListReadReceiptOptions listReadReceiptOptions,
        Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.listChatReadReceiptsSinglePageAsync(this.chatThreadId,
            listReadReceiptOptions.getMaxPageSize(),
            listReadReceiptOptions.getSkip(), context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(response -> {
                List<ChatMessageReadReceipt> receipts = new ArrayList<>();
                if (response.getValue() != null) {
                    for (com.azure.android.communication.chat.implementation.models.ChatMessageReadReceipt innerReceipt
                        : response.getValue()) {
                        receipts.add(ChatMessageReadReceiptConverter.convert(innerReceipt, this.logger));
                    }
                }
                return new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    receipts,
                    response.getContinuationToken(),
                    null);
            });
    }

    /**
     * Gets the list of the thread read receipts in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the list of thread read receipts in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Page<String, ChatMessageReadReceipt>> getReadReceiptsNextPage(String nextLink) {
        return this.getReadReceiptsNextPage(nextLink, null)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken()));
    }

    /**
     * Gets the list of the thread read receipts in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread read receipts
     * in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatMessageReadReceipt>> getReadReceiptsNextPageWithResponse(
        String nextLink,
        Context context) {
        return this.getReadReceiptsNextPage(nextLink, context);
    }

    /**
     * Gets the list of the thread read receipts in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread read receipts
     * in the page.
     */
    CompletableFuture<PagedResponse<ChatMessageReadReceipt>> getReadReceiptsNextPage(String nextLink,
                                                                                     Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(nextLink, context)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(response -> {
                List<ChatMessageReadReceipt> receipts = new ArrayList<>();
                if (response.getValue() != null) {
                    for (com.azure.android.communication.chat.implementation.models.ChatMessageReadReceipt innerReceipt
                        : response.getValue()) {
                        receipts.add(ChatMessageReadReceiptConverter.convert(innerReceipt, this.logger));
                    }
                }
                return new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    receipts,
                    response.getContinuationToken(),
                    null);
            });
    }
}
