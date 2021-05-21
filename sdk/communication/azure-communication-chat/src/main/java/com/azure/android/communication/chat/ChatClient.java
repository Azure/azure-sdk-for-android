// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.rest.util.paging.PagedIterable;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.util.Context;
import com.azure.android.core.util.Function;

import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

/**
 * Sync Client that supports chat operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = false)
public final class ChatClient {
    private final ClientLogger logger = new ClientLogger(ChatClient.class);
    private final ChatAsyncClient client;

    /**
     * Creates a ChatClient that sends requests to the chat service.
     *
     * @param client The {@link ChatAsyncClient} that the client routes its request through.
     */
    ChatClient(ChatAsyncClient client) {
        this.client = client;
    }

    /**
     * Creates a chat thread client.
     *
     * @param chatThreadId The id of the chat thread.
     *
     * @return the client.
     */
    public ChatThreadClient getChatThreadClient(String chatThreadId) {
        ChatThreadAsyncClient chatThreadAsyncClient = this.client.getChatThreadClient(chatThreadId);
        return new ChatThreadClient(chatThreadAsyncClient);
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     *
     * @return the thread created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CreateChatThreadResult createChatThread(CreateChatThreadOptions options) {
        return block(this.client.createChatThread(options));
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @param context The context to associate with this operation.
     *
     * @return the response containing the thread created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CreateChatThreadResult> createChatThreadWithResponse(CreateChatThreadOptions options,
                                                                         Context context) {
        return block(this.client.createChatThread(options, context)
            .thenApply(result -> {
                return new SimpleResponse<>(result, result.getValue());
            }));
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteChatThread(String chatThreadId) {
        block(this.client.deleteChatThread(chatThreadId));
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     * @param context The context to associate with this operation.
     *
     * @return the response of the delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteChatThreadWithResponse(String chatThreadId, Context context) {
        return block(this.client.deleteChatThread(chatThreadId, context));
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadItem> listChatThreads() {
        return this.listChatThreads(new ListChatThreadsOptions(), Context.NONE);
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @param listThreadsOptions The request options.
     * @param context The context to associate with this operation.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadItem> listChatThreads(ListChatThreadsOptions listThreadsOptions,
                                                         Context context) {
        final Function<String, PagedResponse<ChatThreadItem>> pageRetriever = (String pageId) -> {
            if (pageId == null) {
                return this.getChatThreadsFirstPageWithResponse(listThreadsOptions, context);
            } else {
                return this.getChatThreadsNextPageWithResponse(pageId, context);
            }
        };
        return new PagedIterable<>(pageRetriever, pageId -> pageId != null, this.logger);
    }

    /**
     * Gets the list of Chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the response containing the list of Chat threads in the first page.
     */
    private PagedResponse<ChatThreadItem> getChatThreadsFirstPageWithResponse(
        ListChatThreadsOptions listThreadsOptions,
        Context context) {
        return block(this.client.getChatThreadsFirstPage(listThreadsOptions, context));
    }

    /**
     * Gets the page with given id containing list of chat threads.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the response containing the list of Chat threads in the page.
     */
    private PagedResponse<ChatThreadItem> getChatThreadsNextPageWithResponse(
        String nextLink,
        Context context) {
        return block(this.client.getChatThreadsNextPage(nextLink, context));
    }

    /**
     * Receive real-time messages and notifications.
     */
    public void startRealtimeNotifications() {
        this.client.startRealtimeNotifications();
    }

    /**
     * Stop receiving real-time messages and notifications.
     */
    public void stopRealtimeNotifications() {
        client.stopRealtimeNotifications();
    }

    /**
     * Listen to a chat event.
     * @param chatEventType the chat event kind
     * @param listenerId the listener id that is used to identify a listener
     * @param listener the listener callback function
     */
    public void on(ChatEventType chatEventType, String listenerId, RealTimeNotificationCallback listener) {
        this.client.on(chatEventType, listenerId, listener);
    }

    /**
     * Stop listening to a chat event.
     * @param chatEventType the chat event kind
     * @param listenerId the listener id that is to off
     */
    public void off(ChatEventType chatEventType, String listenerId) {
        client.off(chatEventType, listenerId);
    }

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
