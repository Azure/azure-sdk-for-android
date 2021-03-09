// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.ChatImpl;
import com.azure.android.communication.chat.implementation.converters.ChatThreadConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadOptionsConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadResultConverter;
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

import java.util.Objects;

import java9.util.concurrent.CompletableFuture;

/**
 * Async Client that supports chat operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;
    private final ChatImpl chatClient;

    ChatAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient) {
        this.chatServiceClient = chatServiceClient;
        this.chatClient = chatServiceClient.getChatClient();
    }

    /**
     * Creates a chat thread client.
     *
     * @param chatThreadId The id of the thread.
     * @return the client.
     */
    public ChatThreadAsyncClient getChatThreadClient(String chatThreadId) {
        Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
        return new ChatThreadAsyncClient(chatServiceClient, chatThreadId);
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @return the response.
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
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<CreateChatThreadResult>> createChatThreadWithResponse(
        CreateChatThreadOptions options) {
        if (options == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'options' cannot be null."));
        }
        return this.createChatThread(options, null);
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @param context The context to associate with this operation.
     * @return the response.
     */
    CompletableFuture<Response<CreateChatThreadResult>> createChatThread(CreateChatThreadOptions options,
                                                                         Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatClient.createChatThreadWithResponseAsync(
            CreateChatThreadOptionsConverter.convert(options, this.logger),
            options.getRepeatabilityRequestId(),
            context).thenApply(result -> {
                return new SimpleResponse<>(result,
                    CreateChatThreadResultConverter.convert(result.getValue(), this.logger));
            });
    }

    /**
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @return a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<ChatThread> getChatThread(String chatThreadId) {
        if (chatThreadId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatThreadId' cannot be null."));
        }
        return this.getChatThread(chatThreadId, null)
            .thenApply(response -> {
                return response.getValue();
            });
    }

    /**
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @return a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<ChatThread>> getChatThreadWithResponse(String chatThreadId) {
        if (chatThreadId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatThreadId' cannot be null."));
        }
        return this.getChatThread(chatThreadId, null);
    }

    /**
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @param context The context to associate with this operation.
     * @return a chat thread.
     */
    CompletableFuture<Response<ChatThread>> getChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatClient.getChatThreadWithResponseAsync(chatThreadId, context)
            .thenApply(result -> {
                return new SimpleResponse<ChatThread>(result,
                    ChatThreadConverter.convert(result.getValue(), this.logger));
            });
    }

//    /**
//     * Gets the list of chat threads of a user.
//     *
//     * @return the paged list of chat threads of a user.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatThreadInfo> listChatThreads() {
//        ListChatThreadsOptions listThreadsOptions = new ListChatThreadsOptions();
//        try {
//            return new PagedFlux<>(
//                () -> withContext(context ->  this.chatClient.listChatThreadsSinglePageAsync(
//                    listThreadsOptions.getMaxPageSize(), listThreadsOptions.getStartTime(), context)),
//                nextLink -> withContext(context -> this.chatClient.listChatThreadsNextSinglePageAsync(
//                    nextLink, context)));
//        } catch (RuntimeException ex) {
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    /**
//     * Gets the list of chat threads of a user.
//     *
//     * @param listThreadsOptions The request options.
//     * @return the paged list of chat threads of a user.
//     */
//    @ServiceMethod(returns = ReturnType.COLLECTION)
//    public PagedFlux<ChatThreadInfo> listChatThreads(ListChatThreadsOptions listThreadsOptions) {
//        final ListChatThreadsOptions serviceListThreadsOptions
//            = listThreadsOptions == null ? new ListChatThreadsOptions() : listThreadsOptions;
//        try {
//            return new PagedFlux<>(
//                () -> withContext(context ->  this.chatClient.listChatThreadsSinglePageAsync(
//                    serviceListThreadsOptions.getMaxPageSize(), serviceListThreadsOptions.getStartTime(), context)),
//                nextLink -> withContext(context -> this.chatClient.listChatThreadsNextSinglePageAsync(
//                    nextLink, context)));
//        } catch (RuntimeException ex) {
//            return new PagedFlux<>(() -> monoError(logger, ex));
//        }
//    }
//
//    /**
//     * Gets the list of chat threads of a user.
//     *
//     * @param listThreadsOptions The request options.
//     * @return the paged list of chat threads of a user.
//     */
//    PagedFlux<ChatThreadInfo> listChatThreads(ListChatThreadsOptions listThreadsOptions, Context context) {
//        final Context serviceContext = context == null ? Context.NONE : context;
//        final ListChatThreadsOptions serviceListThreadsOptions
//            = listThreadsOptions == null ? new ListChatThreadsOptions() : listThreadsOptions;
//
//        return this.chatClient.listChatThreadsAsync(
//            serviceListThreadsOptions.getMaxPageSize(), serviceListThreadsOptions.getStartTime(), serviceContext);
//    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @return the completion.
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
     * @param chatThreadId Chat thread id to delete.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<Response<Void>> deleteChatThreadWithResponse(String chatThreadId) {
        if (chatThreadId == null) {
            return CompletableFuture.failedFuture(new NullPointerException("'chatThreadId' cannot be null."));
        }
        return this.deleteChatThread(chatThreadId, null);
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    CompletableFuture<Response<Void>> deleteChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatClient.deleteChatThreadWithResponseAsync(chatThreadId, context);
    }
}
