// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import android.content.Context;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.CFBackedPageAsyncStream;
import com.azure.android.communication.chat.implementation.ChatImpl;
import com.azure.android.communication.chat.implementation.converters.CommunicationErrorResponseExceptionConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadOptionsConverter;
import com.azure.android.communication.chat.implementation.converters.CreateChatThreadResultConverter;
import com.azure.android.communication.chat.implementation.notifications.fcm.PushNotificationClient;
import com.azure.android.communication.chat.implementation.notifications.signaling.CommunicationSignalingClient;
import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatErrorResponseException;
import com.azure.android.communication.chat.models.ChatPushNotification;
import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.chat.implementation.notifications.signaling.SignalingClient;
import com.azure.android.communication.common.CommunicationTokenCredential;
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

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Consumer;

/**
 * Async Client that supports chat operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;
    private final SignalingClient signalingClient;
    private final PushNotificationClient pushNotificationClient;
    private final ChatImpl chatClient;

    ChatAsyncClient(
        AzureCommunicationChatServiceImpl chatServiceClient,
        CommunicationTokenCredential communicationTokenCredential) {
        this.chatServiceClient = chatServiceClient;
        this.signalingClient = new CommunicationSignalingClient(communicationTokenCredential);
        this.chatClient = chatServiceClient.getChatClient();
        this.pushNotificationClient = new PushNotificationClient(communicationTokenCredential);
    }

    /**
     * Creates a chat thread client.
     *
     * @param chatThreadId The id of the thread.
     * @throws NullPointerException if chatThreadId is null
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * Start receiving realtime notifications.
     * Until {@link ChatAsyncClient#stopRealtimeNotifications()} is called,
     * realtime notifications will be enabled and the corresponding registration will auto-renew.
     * If there's an error during registration initialization or renewal,
     * realtime notifications will be disabled and {@code errorHandler} will be called.
     * @param context the Android app context
     * @param errorHandler error handler callback for registration failures
     * @throws RuntimeException if realtime notifications failed to start.
     */
    public void startRealtimeNotifications(Context context, Consumer<Throwable> errorHandler) {
        if (this.signalingClient.hasStarted()) {
            return;
        }

        this.signalingClient.start(context, errorHandler);
    }

    /**
     * Start receiving realtime notifications.
     * Until {@link ChatAsyncClient#stopRealtimeNotifications()} is called,
     * realtime notifications will be enabled and the corresponding registration will auto-renew.
     * If there's an error during registration initialization or renewal, realtime notifications will be disabled.
     * @param skypeUserToken the skype user token
     * @param context the Android app context
     * @throws RuntimeException if realtime notifications failed to start.
     *
     * @deprecated Use {@link ChatAsyncClient#startRealtimeNotifications(Context, Consumer)} instead.
     */
    @Deprecated
    public void startRealtimeNotifications(String skypeUserToken, Context context) {
        if (this.signalingClient.hasStarted()) {
            return;
        }

        this.signalingClient.start(skypeUserToken, context);
    }

    /**
     * Stop receiving realtime notifications.
     * All registered handlers will be removed.
     */
    public void stopRealtimeNotifications() {
        this.signalingClient.stop();
    }

    /**
     * Register current device for receiving incoming push notifications via FCM.
     * Until {@link ChatAsyncClient#stopPushNotifications()} is called,
     * push notifications will be enabled and the corresponding registration will auto-renew.
     * If there's an error during registration initialization or renewal,
     * push notifications will be disabled and {@code errorHandler} will be called.
     * @param deviceRegistrationToken Device registration token obtained from the FCM SDK.
     * @param errorHandler error handler callback for registration failures
     * @throws RuntimeException if push notifications failed to start.
     * @deprecated The function will be replaced by
     * {@link ChatAsyncClient#startPushNotifications(String deviceRegistrationToken)}.
     */
    @Deprecated
    public void startPushNotifications(String deviceRegistrationToken, Consumer<Throwable> errorHandler) {
        if (this.pushNotificationClient.hasStarted()) {
            return;
        }

        this.pushNotificationClient.startPushNotifications(deviceRegistrationToken, errorHandler);
    }

    /**
     * Register current device for receiving incoming push notifications via FCM.
     * Until {@link ChatAsyncClient#stopPushNotifications()} is called,
     * push notifications will be enabled and the corresponding registration will auto-renew.
     * If there's an error during registration initialization or renewal,
     * push notifications will be disabled and {@code errorHandler} will be called.
     * @param deviceRegistrationToken Device registration token obtained from the FCM SDK.
     * @throws RuntimeException if push notifications failed to start.
     */
    public void startPushNotifications(String deviceRegistrationToken) {
        if (this.pushNotificationClient.hasStarted()) {
            return;
        }

        this.pushNotificationClient.startPushNotifications(deviceRegistrationToken);
    }

    /**
     * Unregister current device from receiving incoming push notifications.
     * All registered handlers will be removed.
     */
    public void stopPushNotifications() {
        this.pushNotificationClient.stopPushNotifications();
    }

    /**
     * Handle incoming push notification.
     * Invoke corresponding chat event handle if registered.
     * @param pushNotification Incoming push notification payload from the FCM SDK.
     *
     * @throws RuntimeException if the push notification could not be handled.
     * @return True if there's registered handler(s) for incoming push notification; otherwise, false.
     */
    public boolean handlePushNotification(ChatPushNotification pushNotification) {
        return this.pushNotificationClient.handlePushNotification(pushNotification);
    }

    /**
     * Add handler for a chat event for push notifications.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     * @throws IllegalStateException if push notifications has not started yet.
     */
    public void addPushNotificationHandler(ChatEventType chatEventType, Consumer<ChatEvent> listener) {
        if (!this.pushNotificationClient.hasStarted()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "You must call startPushNotifications(String) before you can subscribe to push notifications."
            ));
        }

        this.pushNotificationClient.addPushNotificationHandler(chatEventType, listener);
    }

    /**
     * Remove handler from a chat event for push notifications.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     */
    public void removePushNotificationHandler(ChatEventType chatEventType, Consumer<ChatEvent> listener) {
        this.pushNotificationClient.removePushNotificationHandler(chatEventType, listener);
    }

    /**
     * Add handler for a chat event for realtime notifications.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     * @throws IllegalStateException if realtime notifications has not started yet.
     */
    public void addEventHandler(ChatEventType chatEventType, RealTimeNotificationCallback listener) {
        if (!this.signalingClient.hasStarted()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "You must call startRealtimeNotifications(Context) before you can subscribe to events."
            ));
        }

        this.signalingClient.on(chatEventType, listener);
    }

    /**
     * Remove handler from a chat event for realtime notifications.
     * @param chatEventType the chat event type
     * @param listener the listener callback function
     */
    public void removeEventHandler(ChatEventType chatEventType, RealTimeNotificationCallback listener) {
        this.signalingClient.off(chatEventType, listener);
    }
}
