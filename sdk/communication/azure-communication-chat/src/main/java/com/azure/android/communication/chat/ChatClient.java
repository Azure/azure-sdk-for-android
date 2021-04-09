// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.chat.models.signaling.RealTimeNotificationCallback;
import com.azure.android.communication.chat.models.signaling.ChatEventId;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Page;
import com.azure.android.core.rest.PagedResponse;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.util.Context;

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
     * Gets the list of Chat threads in the first page.
     *
     * @return the first page with list of Chat threads.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Page<ChatThreadItem> getChatThreadsFirstPage() {
        return block(this.client.getChatThreadsFirstPage());
    }

    /**
     * Gets the list of Chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return @return the first page with list of Chat threads.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Page<ChatThreadItem> getChatThreadsFirstPage(ListChatThreadsOptions listThreadsOptions, Context context) {
        return block(this.client.getChatThreadsFirstPage(listThreadsOptions, context)
            .thenApply(response -> new ChatAsyncClient.PageImpl<>(response.getValue(),
                response.getContinuationToken())));
    }

    /**
     * Gets the list of Chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the response containing the list of Chat threads in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedResponse<ChatThreadItem> getChatThreadsFirstPageWithResponse(
        ListChatThreadsOptions listThreadsOptions,
        Context context) {
        return block(this.client.getChatThreadsFirstPage(listThreadsOptions, context));
    }

    /**
     * Gets the page with given id containing list of chat threads.
     *
     * @param nextLink the identifier for the page to retrieve.
     *
     * @return the page with the list of Chat threads.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Page<ChatThreadItem> getChatThreadsNextPage(String nextLink) {
        return block(this.client.getChatThreadsNextPage(nextLink));
    }

    /**
     * Gets the page with given id containing list of chat threads.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context the context to associate with this operation.
     *
     * @return the response containing the list of Chat threads in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedResponse<ChatThreadItem> getChatThreadsNextPageWithResponse(
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
     * @param chatEventId the chat event id
     * @param listenerId the listener id that is used to identify a listener
     * @param listener the listener callback function
     */
    public void on(ChatEventId chatEventId, String listenerId, RealTimeNotificationCallback listener) {
        this.client.on(chatEventId, listenerId, listener);
    }

    /**
     * Stop listening to a chat event.
     * @param chatEventId the chat event id
     * @param listenerId the listener id that is to off
     */
    public void off(ChatEventId chatEventId, String listenerId) {
        client.off(chatEventId, listenerId);
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
