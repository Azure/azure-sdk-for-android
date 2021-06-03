// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.CFBackedPageAsyncStream;
import com.azure.android.communication.chat.implementation.ChatImpl;
import com.azure.android.communication.chat.implementation.converters.CommunicationErrorResponseExceptionConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadOptionsConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadResultConverter;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.chat.implementation.signaling.SignalingClient;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.rest.util.paging.PagedAsyncStream;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.RequestContext;
import com.azure.android.core.util.Function;
import com.azure.android.core.util.paging.Page;

import java.util.List;

import java9.util.concurrent.CompletableFuture;

/**
 * Async Client that supports chat operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;
    private final SignalingClient signalingClient;
    private final ChatImpl chatClient;

    ChatAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient, SignalingClient signalingClient) {
        this.chatServiceClient = chatServiceClient;
        this.signalingClient = signalingClient;
        this.chatClient = chatServiceClient.getChatClient();
    }

    /**
     * Creates a chat thread client.
     *
     * @param chatThreadId The id of the thread.
     * @return the client.
     */
    public ChatThreadAsyncClient getChatThreadClient(String chatThreadId) {
        if (chatThreadId == null) {
            throw logger.logExceptionAsError(new NullPointerException("'chatThreadId' cannot be null."));
        }
        return new ChatThreadAsyncClient(this.chatServiceClient, chatThreadId);
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @return the {@link CompletableFuture} that emits the thread created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<CreateChatThreadResult> createChatThread(CreateChatThreadOptions options) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.createChatThread(options, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the thread created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<CreateChatThreadResult>> createChatThreadWithResponse(
        CreateChatThreadOptions options,
        RequestContext requestContext) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.createChatThread(options, requestContext);
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating the chat thread.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the thread created along with the response.
     */
    CompletableFuture<Response<CreateChatThreadResult>> createChatThread(CreateChatThreadOptions options,
                                                                         RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatClient.createChatThreadWithResponseAsync(
            CreateChatThreadOptionsConverter.convert(options, this.logger),
            options.getIdempotencyToken(),
            requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            }).thenApply(result -> {
                return new SimpleResponse<>(result,
                    CreateChatThreadResultConverter.convert(result.getValue(), this.logger));
            });
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @return the paged stream of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatThreadItem> listChatThreads() {
        return this.listChatThreads(new ListChatThreadsOptions(), RequestContext.NONE);
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @param listThreadsOptions The request options.
     * @param requestContext The context to associate with this operation.
     * @return the paged stream of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedAsyncStream<ChatThreadItem> listChatThreads(ListChatThreadsOptions listThreadsOptions,
                                                       RequestContext requestContext) {
        final Function<String, CompletableFuture<PagedResponse<ChatThreadItem>>> pageRetriever = (String pageId) -> {
            if (pageId == null) {
                return this.getChatThreadsFirstPage(listThreadsOptions, requestContext);
            } else {
                return this.getChatThreadsNextPage(pageId, requestContext);
            }
        };

        final Function<String, AsyncStream<PagedResponse<ChatThreadItem>>> streamRetriever = (String pageId) -> {
            return new CFBackedPageAsyncStream<>(pageRetriever, id -> id != null, pageId, this.logger);
        };

        return new PagedAsyncStream<>(streamRetriever, this.logger);
    }

    /**
     * Gets the list of chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     * @param requestContext the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of chat threads in the first page.
     */
    CompletableFuture<PagedResponse<ChatThreadItem>> getChatThreadsFirstPage(ListChatThreadsOptions listThreadsOptions,
                                                                             RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatClient.listChatThreadsSinglePageAsync(listThreadsOptions.getMaxPageSize(),
            listThreadsOptions.getStartTime(), requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Gets the list of the chat threads in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of chat threads in the page.
     */
    CompletableFuture<PagedResponse<ChatThreadItem>> getChatThreadsNextPage(String nextLink,
                                                                            RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatClient.listChatThreadsNextSinglePageAsync(nextLink, requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     *
     * @return the {@link CompletableFuture} that signals the result of deletion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Void> deleteChatThread(String chatThreadId) {
        if (chatThreadId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatThreadId' cannot be null."));
        }
        return this.deleteChatThread(chatThreadId, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> deleteChatThreadWithResponse(String chatThreadId,
                                                                          RequestContext requestContext) {
        if (chatThreadId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatThreadId' cannot be null."));
        }
        return this.deleteChatThread(chatThreadId, requestContext);
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     * @param requestContext The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    CompletableFuture<Response<Void>> deleteChatThread(String chatThreadId, RequestContext requestContext) {
        requestContext = requestContext == null ? RequestContext.NONE : requestContext;
        return this.chatClient.deleteChatThreadWithResponseAsync(chatThreadId, requestContext)
            .exceptionally(throwable -> {
                throw logger.logExceptionAsError(CommunicationErrorResponseExceptionConverter.convert(throwable));
            });
    }

    /**
     * Receive real-time messages and notifications.
     */
    public void startRealtimeNotifications() {
        if (signalingClient == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Signaling client not initialized"));
        }

        if (this.signalingClient.hasStarted()) {
            return;
        }

        this.signalingClient.start();
    }

    /**
     * Stop receiving real-time messages and notifications.
     */
    public void stopRealtimeNotifications() {
        if (signalingClient == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Signaling client not initialized"));
        }

        this.signalingClient.stop();
    }

    /**
     * Listen to a chat event.
     * @param chatEventType the chat event kind
     * @param listenerId a listener id that is used to identify the listner
     * @param listener the listener callback function
     */
    public void on(ChatEventType chatEventType, String listenerId, RealTimeNotificationCallback listener) {
        if (signalingClient == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Signaling client not initialized"));
        }

        if (!this.signalingClient.hasStarted()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "You must call startRealtimeNotifications before you can subscribe to events."
            ));
        }

        this.signalingClient.on(chatEventType, listenerId, listener);
    }

    /**
     * Stop listening to a chat event.
     * @param chatEventType the chat event kind
     * @param listenerId the listener id that is to off
     */
    public void off(ChatEventType chatEventType, String listenerId) {
        if (signalingClient == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Signaling client not initialized"));
        }

        this.signalingClient.off(chatEventType, listenerId);
    }

    static class PageImpl<T> implements Page<String, T> {
        private final List<T> items;
        private final String continuationToken;

        PageImpl(List<T> items, String continuationToken) {
            this.items = items;
            this.continuationToken = continuationToken;
        }

        @Override
        public List<T> getElements() {
            return this.items;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationToken;
        }
    }
}
