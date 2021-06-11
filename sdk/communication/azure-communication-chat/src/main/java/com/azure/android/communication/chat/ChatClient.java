// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import android.content.Context;

import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatErrorResponseException;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.rest.util.paging.PagedIterable;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.util.RequestContext;
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
     * @throws NullPointerException if chatThreadId is null
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response containing the thread created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CreateChatThreadResult> createChatThreadWithResponse(CreateChatThreadOptions options,
                                                                         RequestContext requestContext) {
        return block(this.client.createChatThread(options, requestContext)
            .thenApply(result -> {
                return new SimpleResponse<>(result, result.getValue());
            }));
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteChatThread(String chatThreadId) {
        block(this.client.deleteChatThread(chatThreadId));
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId the id of the Chat thread to delete.
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response of the delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteChatThreadWithResponse(String chatThreadId, RequestContext requestContext) {
        return block(this.client.deleteChatThread(chatThreadId, requestContext));
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadItem> listChatThreads() {
        return this.listChatThreads(new ListChatThreadsOptions(), RequestContext.NONE);
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @param listThreadsOptions The request options.
     * @param requestContext The context to associate with this operation.
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadItem> listChatThreads(ListChatThreadsOptions listThreadsOptions,
                                                         RequestContext requestContext) {
        final Function<String, PagedResponse<ChatThreadItem>> pageRetriever = (String pageId) -> {
            if (pageId == null) {
                return this.getChatThreadsFirstPageWithResponse(listThreadsOptions, requestContext);
            } else {
                return this.getChatThreadsNextPageWithResponse(pageId, requestContext);
            }
        };
        return new PagedIterable<>(pageRetriever, pageId -> pageId != null, this.logger);
    }

    /**
     * Gets the list of Chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     * @param requestContext the context to associate with this operation.
     *
     * @return the response containing the list of Chat threads in the first page.
     */
    private PagedResponse<ChatThreadItem> getChatThreadsFirstPageWithResponse(
        ListChatThreadsOptions listThreadsOptions,
        RequestContext requestContext) {
        return block(this.client.getChatThreadsFirstPage(listThreadsOptions, requestContext));
    }

    /**
     * Gets the page with given id containing list of chat threads.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param requestContext the context to associate with this operation.
     *
     * @return the response containing the list of Chat threads in the page.
     */
    private PagedResponse<ChatThreadItem> getChatThreadsNextPageWithResponse(
        String nextLink,
        RequestContext requestContext) {
        return block(this.client.getChatThreadsNextPage(nextLink, requestContext));
    }

    /**
     * Receive real-time notifications.
     * @param skypeUserToken the skype user token
     * @param context the app context of the app
     * @throws RuntimeException if real-time notifications failed to start.
     */
    public void startRealtimeNotifications(String skypeUserToken, Context context) {
        this.client.startRealtimeNotifications(skypeUserToken, context);
    }

    /**
     * Stop receiving real-time notifications.
     */
    public void stopRealtimeNotifications() {
        client.stopRealtimeNotifications();
    }

    /**
     * Add handler for a chat event.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     * @throws IllegalStateException if real-time notifications has not started yet.
     * @return the listener id for the recently added listener.
     */
    public String addEventHandler(ChatEventType chatEventType, RealTimeNotificationCallback listener) {
        return this.client.addEventHandler(chatEventType, listener);
    }

    /**
     * Remove handler from a chat event.
     * @param chatEventType the chat event type
     * @param listenerId the listener id that is to be removed
     */
    public void removeEventHandler(ChatEventType chatEventType, String listenerId) {
        this.client.removeEventHandler(chatEventType, listenerId);
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
