// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.CFBackedPageAsyncStream;
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
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatError;
import com.azure.android.communication.chat.models.ChatErrorResponseException;
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
import com.azure.android.communication.chat.models.TypingNotificationOptions;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatThreadOptions;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.rest.util.paging.PagedAsyncStream;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.rest.util.paging.PagedResponseBase;
import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.RequestContext;
import com.azure.android.core.util.Function;

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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> updateTopicWithResponse(String topic, RequestContext requestContext) {
        if (topic == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'topic' cannot be null."));
        }
        return this.updateTopic(topic, requestContext);
    }

    /**
     * Gets chat thread properties.
     *
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits the response containing the thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<ChatThreadProperties>> getPropertiesWithResponse(RequestContext requestContext) {
        return this.getProperties(requestContext);
    }

    /**
     * Gets chat thread properties.
     *
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the thread properties.
     */
    CompletableFuture<Response<ChatThreadProperties>> getProperties(RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.getChatThreadPropertiesWithResponseAsync(this.chatThreadId, requestContext)
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
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    CompletableFuture<Response<Void>> updateTopic(String topic, RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.updateChatThreadPropertiesWithResponseAsync(
            chatThreadId,
            new UpdateChatThreadOptions().setTopic(topic),
            requestContext
        ).exceptionally(throwable -> {
            throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
        });
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Participants to add.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that signals the operation result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<AddChatParticipantsResult> addParticipants(Iterable<ChatParticipant> participants) {
        if (participants == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'participants' cannot be null."));
        }
        return this.addParticipants(participants, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Participants to add.
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<AddChatParticipantsResult>> addParticipantsWithResponse(
        Iterable<ChatParticipant> participants, RequestContext requestContext) {
        if (participants == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'participants' cannot be null."));
        }
        return this.addParticipants(participants, requestContext);
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws InvalidParticipantException if the participant is rejected by the server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws InvalidParticipantException if the participant is rejected by the server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> addParticipantWithResponse(
        ChatParticipant participant, RequestContext requestContext) {
        return this.addParticipant(participant, requestContext);
    }

    /**
     * Adds a participant to a thread. If participants already exist, no change occurs.
     *
     * @param participant The new participant.
     * @param requestContext The context to associate with this operation.
     * @throws InvalidParticipantException if the participant is rejected by the server.
     *
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    CompletableFuture<Response<Void>> addParticipant(ChatParticipant participant, RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.addParticipants(
            Collections.singletonList(participant),
            requestContext)
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
     * @param participants Collection of participants to add.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response containing the operation result.
     */
    CompletableFuture<Response<AddChatParticipantsResult>> addParticipants(Iterable<ChatParticipant> participants,
                                                                           RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.addChatParticipantsWithResponseAsync(
            this.chatThreadId, AddChatParticipantsOptionsConverter.convert(participants, this.logger), requestContext)
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the remove request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> removeParticipantWithResponse(CommunicationIdentifier identifier,
                                                                           RequestContext requestContext) {
        if (identifier == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'identifier' cannot be null."));
        }
        return this.removeParticipant(identifier, requestContext);
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier identity of the participant to remove from the thread.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the remove request.
     */
    CompletableFuture<Response<Void>> removeParticipant(CommunicationIdentifier identifier,
                                                        RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.removeChatParticipantWithResponseAsync(
            chatThreadId, CommunicationIdentifierConverter.convert(identifier, this.logger), requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets the list of the thread participants.
     *
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged stream of participants in the thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatParticipant> listParticipants() {
        return this.listParticipants(new ListParticipantsOptions(), RequestContext.NONE);
    }

    /**
     * Gets the list of the thread participants.
     *
     * @param listParticipantsOptions the list options.
     * @param requestContext the context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged stream of participants in the thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatParticipant> listParticipants(
        ListParticipantsOptions listParticipantsOptions,
        RequestContext requestContext) {
        final Function<String, CompletableFuture<PagedResponse<ChatParticipant>>> pageRetriever = (String pageId) -> {
            if (pageId == null) {
                return this.getParticipantsFirstPage(listParticipantsOptions, requestContext);
            } else {
                return this.getParticipantsNextPage(pageId, requestContext);
            }
        };

        final Function<String, AsyncStream<PagedResponse<ChatParticipant>>> streamRetriever = (String pageId) -> {
            return new CFBackedPageAsyncStream<>(pageRetriever, id -> id != null, pageId, this.logger);
        };

        return new PagedAsyncStream<>(streamRetriever, this.logger);
    }

    /**
     * Gets the list of the thread participants in the first page.
     *
     * @param listParticipantsOptions the list options.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread participants
     * in the first page.
     */
    CompletableFuture<PagedResponse<ChatParticipant>> getParticipantsFirstPage(
        ListParticipantsOptions listParticipantsOptions,
        RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.listChatParticipantsSinglePageAsync(this.chatThreadId,
            listParticipantsOptions.getMaxPageSize(),
            listParticipantsOptions.getSkip(), requestContext)
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
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread participants in the page.
     */
    CompletableFuture<PagedResponse<ChatParticipant>> getParticipantsNextPage(String nextLink,
                                                                              RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, requestContext)
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext the context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits the response containing the id of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<SendChatMessageResult>> sendMessageWithResponse(SendChatMessageOptions options,
                                                                       RequestContext requestContext) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.sendMessage(options, requestContext);
    }

    /**
     * Sends a message to a thread.
     *
     * @param options options for sending the message.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the id of the message.
     */
    CompletableFuture<Response<SendChatMessageResult>> sendMessage(SendChatMessageOptions options,
                                                                   RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.sendChatMessageWithResponseAsync(this.chatThreadId, options, requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext the context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits the response containing the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<ChatMessage>> getMessageWithResponse(String chatMessageId,
                                                                           RequestContext requestContext) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.getMessage(chatMessageId, requestContext);
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId the message id.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the message.
     */
    CompletableFuture<Response<ChatMessage>> getMessage(String chatMessageId,
                                                        RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.getChatMessageWithResponseAsync(chatThreadId, chatMessageId, requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(result -> new SimpleResponse<>(result, ChatMessageConverter.convert(result.getValue(),
                this.logger)));
    }

    /**
     * Gets the list of thread messages.
     *
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged stream of messages in the thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatMessage> listMessages() {
        return this.listMessages(new ListChatMessagesOptions(), RequestContext.NONE);
    }

    /**
     * Gets the list of thread messages.
     *
     * @param listMessagesOptions the list options.
     * @param requestContext the context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged stream of messages in the thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatMessage> listMessages(
        ListChatMessagesOptions listMessagesOptions,
        RequestContext requestContext) {
        final Function<String, CompletableFuture<PagedResponse<ChatMessage>>> pageRetriever = (String pageId) -> {
            if (pageId == null) {
                return this.getMessagesFirstPage(listMessagesOptions, requestContext);
            } else {
                return this.getMessagesNextPage(pageId, requestContext);
            }
        };

        final Function<String, AsyncStream<PagedResponse<ChatMessage>>> streamRetriever = (String pageId) -> {
            return new CFBackedPageAsyncStream<>(pageRetriever, id -> id != null, pageId, this.logger);
        };

        return new PagedAsyncStream<>(streamRetriever, this.logger);
    }

    /**
     * Gets the list of thread messages in the first page.
     *
     * @param listChatMessagesOptions the list options.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread messages
     * in the first page.
     */
    CompletableFuture<PagedResponse<ChatMessage>> getMessagesFirstPage(
        ListChatMessagesOptions listChatMessagesOptions,
        RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.listChatMessagesSinglePageAsync(this.chatThreadId,
            listChatMessagesOptions.getMaxPageSize(),
            listChatMessagesOptions.getStartTime(), requestContext)
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
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread messages in the page.
     */
    CompletableFuture<PagedResponse<ChatMessage>> getMessagesNextPage(String nextLink,
                                                                      RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.listChatMessagesNextSinglePageAsync(nextLink, requestContext)
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext the context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> updateMessageWithResponse(String chatMessageId,
                                                                       UpdateChatMessageOptions options,
                                                                       RequestContext requestContext) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.updateMessage(chatMessageId, options, requestContext);
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId the message id.
     * @param options options for updating the message.
     * @param requestContext the context to associate with this operation.
     * @return the {@link CompletableFuture} that emits response of the update request.
     */
    CompletableFuture<Response<Void>> updateMessage(String chatMessageId,
                                                    UpdateChatMessageOptions options,
                                                    RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.updateChatMessageWithResponseAsync(chatThreadId, chatMessageId, options,
            requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId the message id.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> deleteMessageWithResponse(String chatMessageId,
                                                                       RequestContext requestContext) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.deleteMessage(chatMessageId, requestContext);
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId the message id.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    CompletableFuture<Response<Void>> deleteMessage(String chatMessageId,
                                                    RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.deleteChatMessageWithResponseAsync(chatThreadId, chatMessageId, requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that signals the result of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> sendTypingNotification() {
        return this.sendTypingNotification(null, (RequestContext) null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param typingNotificationOptions the options for sending the typing notification.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that signals the result of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> sendTypingNotification(TypingNotificationOptions typingNotificationOptions) {
        return this.sendTypingNotification(typingNotificationOptions, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendTypingNotificationWithResponse(RequestContext requestContext) {
        return this.sendTypingNotification(null, requestContext);
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param typingNotificationOptions the options for sending the typing notification.
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendTypingNotificationWithResponse(
        TypingNotificationOptions typingNotificationOptions, RequestContext requestContext) {
        return this.sendTypingNotification(typingNotificationOptions, requestContext);
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    CompletableFuture<Response<Void>> sendTypingNotification(
        TypingNotificationOptions typingNotificationOptions, RequestContext requestContext) {
        typingNotificationOptions = typingNotificationOptions == null
            ? new TypingNotificationOptions() : typingNotificationOptions;
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.sendTypingNotificationWithResponseAsync(
            chatThreadId, typingNotificationOptions, requestContext)
                .exceptionally(throwable -> {
                    throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
                });
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId the id of the chat message that was read.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> sendReadReceiptWithResponse(String chatMessageId,
                                                                         RequestContext requestContext) {
        if (chatMessageId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatMessageId' cannot be null."));
        }
        return this.sendReadReceipt(chatMessageId, requestContext);
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the operation.
     */
    CompletableFuture<Response<Void>> sendReadReceipt(String chatMessageId, RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        SendReadReceiptRequest request = new SendReadReceiptRequest()
            .setChatMessageId(chatMessageId);
        return this.chatThreadClient.sendChatReadReceiptWithResponseAsync(chatThreadId, request, requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets the list of thread read receipts.
     *
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged stream of read receipts in the thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatMessageReadReceipt> listReadReceipts() {
        return this.listReadReceipts(new ListReadReceiptOptions(), RequestContext.NONE);
    }

    /**
     * Gets the list of thread read receipts.
     *
     * @param listReadReceiptOptions the list options.
     * @param requestContext the context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged stream of read receipts in the thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatMessageReadReceipt> listReadReceipts(
        ListReadReceiptOptions listReadReceiptOptions,
        RequestContext requestContext) {
        final Function<String, CompletableFuture<PagedResponse<ChatMessageReadReceipt>>> pageRetriever =
            (String pageId) -> {
                if (pageId == null) {
                    return this.getReadReceiptsFirstPage(listReadReceiptOptions, requestContext);
                } else {
                    return this.getReadReceiptsNextPage(pageId, requestContext);
                }
            };

        final Function<String, AsyncStream<PagedResponse<ChatMessageReadReceipt>>> streamRetriever =
            (String pageId) -> {
                return new CFBackedPageAsyncStream<>(pageRetriever, id -> id != null, pageId, this.logger);
            };

        return new PagedAsyncStream<>(streamRetriever, this.logger);
    }

    /**
     * Gets the list of thread read receipts in the first page.
     *
     * @param listReadReceiptOptions the list options.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread read receipts
     * in the first page.
     */
    CompletableFuture<PagedResponse<ChatMessageReadReceipt>> getReadReceiptsFirstPage(
        ListReadReceiptOptions listReadReceiptOptions,
        RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.listChatReadReceiptsSinglePageAsync(this.chatThreadId,
            listReadReceiptOptions.getMaxPageSize(),
            listReadReceiptOptions.getSkip(), requestContext)
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
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of thread read receipts
     * in the page.
     */
    CompletableFuture<PagedResponse<ChatMessageReadReceipt>> getReadReceiptsNextPage(String nextLink,
                                                                                     RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(nextLink, requestContext)
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
