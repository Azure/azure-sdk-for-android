// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.core.logging.ClientLogger;
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
     * Creates a ChatClient that sends requests to the chat service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
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
     * @return the response.
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
     * @return the response.
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
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @return a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatThread getChatThread(String chatThreadId) {
        return block(this.client.getChatThread(chatThreadId));
    }

    /**
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @param context The context to associate with this operation.
     * @return a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ChatThread> getChatThreadWithResponse(String chatThreadId, Context context) {
        return block(this.client.getChatThread(chatThreadId, context));
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteChatThread(String chatThreadId) {
        block(this.client.deleteChatThread(chatThreadId));
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteChatThreadWithResponse(String chatThreadId, Context context) {
        return block(this.client.deleteChatThread(chatThreadId, context));
    }

//    /**
//     * Gets the list of chat threads of a user.
//     *
//     * @return the paged list of chat threads of a user.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatThreadInfo> listChatThreads() {
//
//        return new PagedIterable<>(this.client.listChatThreads());
//    }
//
//    /**
//     * Gets the list of chat threads of a user.
//     *
//     * @param listThreadsOptions The request options.
//     * @param context The context to associate with this operation.
//     * @return the paged list of chat threads of a user.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedIterable<ChatThreadInfo> listChatThreads(ListChatThreadsOptions listThreadsOptions, Context context) {
//
//        return new PagedIterable<>(this.client.listChatThreads(listThreadsOptions, context));
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

