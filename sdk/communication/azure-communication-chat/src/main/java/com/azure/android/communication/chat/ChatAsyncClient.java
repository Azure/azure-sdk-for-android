// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.ChatImpl;
import com.azure.android.communication.chat.implementation.converters.ChatThreadConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadOptionsConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadResultConverter;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.ChatThreadInfo;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.PagedResponse;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.SimpleResponse;
import com.azure.android.core.rest.annotation.ReturnType;
import com.azure.android.core.rest.annotation.ServiceClient;
import com.azure.android.core.rest.annotation.ServiceMethod;
import com.azure.android.core.util.Context;

import java.util.List;

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
     * @return the {@link CompletableFuture} that emits the thread created.
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
     * @param options Options for creating the chat thread.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the thread created along with the response.
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
     * @param chatThreadId the id of the Chat thread to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the thread.
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
     * @param chatThreadId the id of the Chat thread to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the response containing the thread.
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
     * @param chatThreadId the id of the Chat thread to retrieve.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing the thread.
     */
    CompletableFuture<Response<ChatThread>> getChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatClient.getChatThreadWithResponseAsync(chatThreadId, context)
            .thenApply(result -> {
                return new SimpleResponse<ChatThread>(result,
                    ChatThreadConverter.convert(result.getValue(), this.logger));
            });
    }

    /**
     * Gets the list of chat threads in the first page.
     *
     * @return the {@link CompletableFuture} that emits list of chat threads in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<List<ChatThreadInfo>> getChatThreadsFirstPage() {
        ListChatThreadsOptions listThreadsOptions = new ListChatThreadsOptions();
        return this.getChatThreadsFirstPage(listThreadsOptions, null)
            .thenApply(response -> response.getValue());
    }

    /**
     * Gets the list of chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     *
     * @return the {@link CompletableFuture} that emits list of chat threads in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<List<ChatThreadInfo>> getChatThreadsFirstPage(ListChatThreadsOptions listThreadsOptions) {
        if (listThreadsOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listThreadsOptions is required."));
        }
        return this.getChatThreadsFirstPage(listThreadsOptions, null)
            .thenApply(response -> response.getValue());
    }

    /**
     * Gets the list of chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of chat threads in the first page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatThreadInfo>> getChatThreadsFirstPageWithResponse(
        ListChatThreadsOptions listThreadsOptions) {
        if (listThreadsOptions == null) {
            return CompletableFuture.failedFuture(new NullPointerException("listThreadsOptions is required."));
        }
        return this.getChatThreadsFirstPage(listThreadsOptions, null);
    }

    /**
     * Gets the list of chat threads in the first page.
     *
     * @param listThreadsOptions the list options.
     * @param context the context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of chat threads in the first page.
     */
    CompletableFuture<PagedResponse<ChatThreadInfo>> getChatThreadsFirstPage(ListChatThreadsOptions listThreadsOptions,
                                                                             Context context) {
        return this.chatClient.listChatThreadsSinglePageAsync(listThreadsOptions.getMaxPageSize(),
            listThreadsOptions.getStartTime(), context);
    }

    /**
     * Gets the list of the chat threads in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the list of chat threads in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<List<ChatThreadInfo>> getChatThreadsNextPage(String nextLink) {
        return this.getChatThreadsNextPage(nextLink, null)
            .thenApply(response -> response.getValue());
    }

    /**
     * Gets the list of the chat threads in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of chat threads in the page.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompletableFuture<PagedResponse<ChatThreadInfo>> listChatThreadsNextPageWithResponse(
        String nextLink) {
        return this.getChatThreadsNextPage(nextLink, null);
    }

    /**
     * Gets the list of the chat threads in the page with given id.
     *
     * @param nextLink the identifier for the page to retrieve.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits the response containing list of chat threads in the page.
     */
    CompletableFuture<PagedResponse<ChatThreadInfo>> getChatThreadsNextPage(String nextLink,
                                                                            Context context) {
        return this.chatClient.listChatThreadsNextSinglePageAsync(nextLink, context);
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
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
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
     * @param chatThreadId the id of the Chat thread to delete.
     * @param context The context to associate with this operation.
     *
     * @return the {@link CompletableFuture} that emits response of the delete request.
     */
    CompletableFuture<Response<Void>> deleteChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatClient.deleteChatThreadWithResponseAsync(chatThreadId, context);
    }
}
