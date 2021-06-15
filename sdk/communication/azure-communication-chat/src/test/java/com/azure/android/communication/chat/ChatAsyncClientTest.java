// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatErrorResponseException;
import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.util.paging.PagedAsyncStream;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.util.AsyncStreamHandler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import java9.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatAsyncClientTest extends ChatClientTestBase {
    private ClientLogger logger = new ClientLogger(ChatAsyncClientTest.class);

    private ChatAsyncClient client;
    private CommunicationUserIdentifier firstThreadParticipant;
    private CommunicationUserIdentifier secondThreadParticipant;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient) {
        this.client = super.getChatClientBuilder(httpClient).buildAsyncClient();
        this.firstThreadParticipant = new CommunicationUserIdentifier(THREAD_PARTICIPANT_1);
        this.secondThreadParticipant = new CommunicationUserIdentifier(THREAD_PARTICIPANT_2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canCreateThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture);
        CreateChatThreadResult result = completableFuture.get();
        assertNotNull(result);
        assertNotNull(result.getChatThreadProperties());
        assertNotNull(result.getChatThreadProperties().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture);
        Response<CreateChatThreadResult> response = completableFuture.get();
        assertNotNull(response);
        CreateChatThreadResult result = response.getValue();
        assertNotNull(result);
        assertNotNull(result.getChatThreadProperties());
        assertNotNull(result.getChatThreadProperties().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canRepeatCreateThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        UUID uuid = UUID.randomUUID();

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId())
            .setIdempotencyToken(uuid.toString());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture1
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture1);
        Response<CreateChatThreadResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        CreateChatThreadResult result1 = response1.getValue();
        assertNotNull(result1);
        assertNotNull(result1.getChatThreadProperties());
        assertNotNull(result1.getChatThreadProperties().getId());

        String expectedThreadId = response1.getValue().getChatThreadProperties().getId();

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture2
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture2);
        Response<CreateChatThreadResult> response2 = completableFuture2.get();
        assertNotNull(response2);
        CreateChatThreadResult result2 = response2.getValue();
        assertNotNull(result2);
        assertNotNull(result2.getChatThreadProperties());
        assertNotNull(result2.getChatThreadProperties().getId());
        assertEquals(expectedThreadId, result2.getChatThreadProperties().getId());

        threadRequest.setIdempotencyToken(UUID.randomUUID().toString());
        CompletableFuture<Response<CreateChatThreadResult>> completableFuture3
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture3);
        Response<CreateChatThreadResult> response3 = completableFuture3.get();
        assertNotNull(response3);
        CreateChatThreadResult result3 = response3.getValue();
        assertNotNull(result3);
        assertNotNull(result3.getChatThreadProperties());
        assertNotNull(result3.getChatThreadProperties().getId());
        assertNotEquals(expectedThreadId, result3.getChatThreadProperties().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canCreateNewThreadWithoutSettingRepeatabilityID(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest1 = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture1
            = this.client.createChatThreadWithResponse(threadRequest1, null);

        assertNotNull(completableFuture1);
        Response<CreateChatThreadResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        CreateChatThreadResult result1 = response1.getValue();
        assertNotNull(result1);
        assertNotNull(result1.getChatThreadProperties());
        assertNotNull(result1.getChatThreadProperties().getId());

        String threadId1 = response1.getValue().getChatThreadProperties().getId();

        // Create new CreateChatThreadOptions to get new RepeatabilityID.
        final CreateChatThreadOptions threadRequest2 = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture2
            = this.client.createChatThreadWithResponse(threadRequest2, null);

        assertNotNull(completableFuture2);
        Response<CreateChatThreadResult> response2 = completableFuture2.get();
        assertNotNull(response2);
        CreateChatThreadResult result2 = response2.getValue();
        assertNotNull(result2);
        assertNotNull(result2.getChatThreadProperties());
        assertNotNull(result2.getChatThreadProperties().getId());
        assertNotEquals(threadId1, result2.getChatThreadProperties().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithNullOptions(HttpClient httpClient) {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.createChatThread(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithResponseWithNullOptions(HttpClient httpClient) {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.createChatThreadWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithInvalidUser(HttpClient httpClient) {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            "8:acs:invalidUserId", secondThreadParticipant.getId());

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.createChatThread(threadRequest).get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof ChatErrorResponseException);

        ChatErrorResponseException exception = (ChatErrorResponseException) cause;
        assertNotNull(exception.getResponse());
        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithResponseWithInvalidUser(HttpClient httpClient) {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            "8:acs:invalidUserId", secondThreadParticipant.getId());

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.createChatThreadWithResponse(threadRequest, null).get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof ChatErrorResponseException);

        ChatErrorResponseException exception = (ChatErrorResponseException) cause;
        assertNotNull(exception.getResponse());
        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canGetChatThreadClient(HttpClient httpClient) {
        setupTest(httpClient);
        String threadId = "19:fe0a2f65a7834185b29164a7de57699c@thread.v2";

        ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(threadId);
        assertNotNull(chatThreadClient);
        assertEquals(chatThreadClient.getChatThreadId(), threadId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canDeleteChatThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThreadProperties());
        assertNotNull(result1.getChatThreadProperties().getId());

        CompletableFuture<Void> completableFuture2
            = this.client.deleteChatThread(result1.getChatThreadProperties().getId());

        assertNotNull(completableFuture2);
        Void result2 = completableFuture2.get();
        assertNull(result2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture1
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture1);
        Response<CreateChatThreadResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        CreateChatThreadResult result1 = response1.getValue();
        assertNotNull(result1);
        assertNotNull(result1.getChatThreadProperties());
        assertNotNull(result1.getChatThreadProperties().getId());

        CompletableFuture<Response<Void>> completableFuture2
            = this.client.deleteChatThreadWithResponse(result1.getChatThreadProperties().getId(), null);

        assertNotNull(completableFuture2);
        Response<Void> response2 = completableFuture2.get();
        assertNotNull(response2);
        Void result2 = response2.getValue();
        assertNull(result2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithNullId(HttpClient httpClient) {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.deleteChatThread(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithResponseWithNullId(HttpClient httpClient) {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.deleteChatThreadWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithInvalidThreadId(HttpClient httpClient) {
        setupTest(httpClient);

        final String invalidChatThreadId = "invalid_chat_thread_id";

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.deleteChatThread(invalidChatThreadId).get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof ChatErrorResponseException);

        ChatErrorResponseException exception = (ChatErrorResponseException) cause;
        assertNotNull(exception.getResponse());
        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithResponseWithInvalidThreadId(HttpClient httpClient) {
        setupTest(httpClient);

        final String invalidChatThreadId = "invalid_chat_thread_id";

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            client.deleteChatThreadWithResponse(invalidChatThreadId, null).get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof ChatErrorResponseException);

        ChatErrorResponseException exception = (ChatErrorResponseException) cause;
        assertNotNull(exception.getResponse());
        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThreadProperties());
        assertNotNull(result1.getChatThreadProperties().getId());

        CompletableFuture<CreateChatThreadResult> completableFuture2
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture2);
        CreateChatThreadResult result2 = completableFuture1.get();
        assertNotNull(result2);
        assertNotNull(result2.getChatThreadProperties());
        assertNotNull(result2.getChatThreadProperties().getId());


        PagedAsyncStream<ChatThreadItem> chatAsyncStream
            = this.client.listChatThreads(new ListChatThreadsOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        AtomicBoolean gotNullResponse = new AtomicBoolean();
        AtomicBoolean gotNullList = new AtomicBoolean();
        List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();

        chatAsyncStream.byPage().forEach(new AsyncStreamHandler<PagedResponse<ChatThreadItem>>() {

            @Override
            public void onNext(PagedResponse<ChatThreadItem> response) {
                if (!gotNullResponse.get()) {
                    gotNullResponse.set(response == null);
                }
                if (response != null) {
                    List<ChatThreadItem> threadsInPage = response.getValue();
                    if (!gotNullList.get()) {
                        gotNullResponse.set(threadsInPage == null);
                    }
                    if (threadsInPage != null) {
                        for (ChatThreadItem thread : threadsInPage) {
                            returnedThreads.add(thread);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });


        awaitOnLatch(latch, "canListChatThreads");

        Assertions.assertFalse(gotNullResponse.get());
        Assertions.assertFalse(gotNullList.get());

        // REVISIT: Unreliable assert
        // assertTrue(returnedThreads.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListChatThreadsWithMaxPage(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadParticipant.getId(), secondThreadParticipant.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThreadProperties());
        assertNotNull(result1.getChatThreadProperties().getId());

        CompletableFuture<CreateChatThreadResult> completableFuture2
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture2);
        CreateChatThreadResult result2 = completableFuture1.get();
        assertNotNull(result2);
        assertNotNull(result2.getChatThreadProperties());
        assertNotNull(result2.getChatThreadProperties().getId());

        PagedAsyncStream<ChatThreadItem> chatAsyncStream
            = this.client.listChatThreads(new ListChatThreadsOptions().setMaxPageSize(2), null);

        CountDownLatch latch = new CountDownLatch(1);

        AtomicBoolean gotNullResponse = new AtomicBoolean();
        AtomicBoolean gotNullList = new AtomicBoolean();
        List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();

        chatAsyncStream.byPage().forEach(new AsyncStreamHandler<PagedResponse<ChatThreadItem>>() {

            @Override
            public void onNext(PagedResponse<ChatThreadItem> response) {
                if (!gotNullResponse.get()) {
                    gotNullResponse.set(response == null);
                }
                if (response != null) {
                    List<ChatThreadItem> threadsInPage = response.getValue();
                    if (!gotNullList.get()) {
                        gotNullResponse.set(threadsInPage == null);
                    }
                    if (threadsInPage != null) {
                        for (ChatThreadItem thread : threadsInPage) {
                            returnedThreads.add(thread);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "canListChatThreads");

        Assertions.assertFalse(gotNullResponse.get());
        Assertions.assertFalse(gotNullList.get());

        // REVISIT: Unreliable assert
        // assertTrue(returnedThreads.size() > 0);
    }
}
