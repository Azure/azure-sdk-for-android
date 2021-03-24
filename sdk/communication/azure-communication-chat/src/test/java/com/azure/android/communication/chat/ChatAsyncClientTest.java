// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatThreadItem;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatThreadsOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.PagedResponse;
import com.azure.android.core.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
    private CommunicationUserIdentifier firstThreadMember;
    private CommunicationUserIdentifier secondThreadMember;

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
        this.firstThreadMember = new CommunicationUserIdentifier(THREAD_MEMBER_1);
        this.secondThreadMember = new CommunicationUserIdentifier(THREAD_MEMBER_2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canCreateThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture);
        CreateChatThreadResult result = completableFuture.get();
        assertNotNull(result);
        assertNotNull(result.getChatThread());
        assertNotNull(result.getChatThread().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture);
        Response<CreateChatThreadResult> response = completableFuture.get();
        assertNotNull(response);
        CreateChatThreadResult result = response.getValue();
        assertNotNull(result);
        assertNotNull(result.getChatThread());
        assertNotNull(result.getChatThread().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canRepeatCreateThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        UUID uuid = UUID.randomUUID();

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId())
            .setRepeatabilityRequestId(uuid.toString());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture1
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture1);
        Response<CreateChatThreadResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        CreateChatThreadResult result1 = response1.getValue();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        String expectedThreadId = response1.getValue().getChatThread().getId();

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture2
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture2);
        Response<CreateChatThreadResult> response2 = completableFuture2.get();
        assertNotNull(response2);
        CreateChatThreadResult result2 = response2.getValue();
        assertNotNull(result2);
        assertNotNull(result2.getChatThread());
        assertNotNull(result2.getChatThread().getId());
        assertEquals(expectedThreadId, result2.getChatThread().getId());

        threadRequest.setRepeatabilityRequestId(UUID.randomUUID().toString());
        CompletableFuture<Response<CreateChatThreadResult>> completableFuture3
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture3);
        Response<CreateChatThreadResult> response3 = completableFuture3.get();
        assertNotNull(response3);
        CreateChatThreadResult result3 = response3.getValue();
        assertNotNull(result3);
        assertNotNull(result3.getChatThread());
        assertNotNull(result3.getChatThread().getId());
        assertNotEquals(expectedThreadId, result3.getChatThread().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canCreateNewThreadWithoutSettingRepeatabilityID(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest1 = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture1
            = this.client.createChatThreadWithResponse(threadRequest1, null);

        assertNotNull(completableFuture1);
        Response<CreateChatThreadResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        CreateChatThreadResult result1 = response1.getValue();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        String threadId1 = response1.getValue().getChatThread().getId();

        // Create new CreateChatThreadOptions to get new RepeatabilityID.
        final CreateChatThreadOptions threadRequest2 = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture2
            = this.client.createChatThreadWithResponse(threadRequest2, null);

        assertNotNull(completableFuture2);
        Response<CreateChatThreadResult> response2 = completableFuture2.get();
        assertNotNull(response2);
        CreateChatThreadResult result2 = response2.getValue();
        assertNotNull(result2);
        assertNotNull(result2.getChatThread());
        assertNotNull(result2.getChatThread().getId());
        assertNotEquals(threadId1, result2.getChatThread().getId());
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
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        CompletableFuture<Void> completableFuture2
            = this.client.deleteChatThread(result1.getChatThread().getId());

        assertNotNull(completableFuture2);
        Void result2 = completableFuture2.get();
        assertNull(result2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<Response<CreateChatThreadResult>> completableFuture1
            = this.client.createChatThreadWithResponse(threadRequest, null);

        assertNotNull(completableFuture1);
        Response<CreateChatThreadResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        CreateChatThreadResult result1 = response1.getValue();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        CompletableFuture<Response<Void>> completableFuture2
            = this.client.deleteChatThreadWithResponse(result1.getChatThread().getId(), null);

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
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        CompletableFuture<CreateChatThreadResult> completableFuture2
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture2);
        CreateChatThreadResult result2 = completableFuture1.get();
        assertNotNull(result2);
        assertNotNull(result2.getChatThread());
        assertNotNull(result2.getChatThread().getId());

        CompletableFuture<PagedResponse<ChatThreadItem>> completableFuture
            = this.client.getChatThreadsFirstPageWithResponse(new ListChatThreadsOptions(), null);

        String nextLink;
        List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();
        do {
            PagedResponse<ChatThreadItem> response = completableFuture.get();
            assertNotNull(response);
            List<ChatThreadItem> threadsInPage = response.getValue();
            assertNotNull(threadsInPage);
            for (ChatThreadItem thread : threadsInPage) {
                returnedThreads.add(thread);
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.client.getChatThreadsNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

// REVISIT: Unreliable assert
        // assertTrue(returnedThreads.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListChatThreadsWithMaxPage(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        CompletableFuture<CreateChatThreadResult> completableFuture2
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture2);
        CreateChatThreadResult result2 = completableFuture1.get();
        assertNotNull(result2);
        assertNotNull(result2.getChatThread());
        assertNotNull(result2.getChatThread().getId());

        CompletableFuture<PagedResponse<ChatThreadItem>> completableFuture
            = this.client.getChatThreadsFirstPageWithResponse(new ListChatThreadsOptions().setMaxPageSize(2), null);

        String nextLink;
        List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();
        do {
            PagedResponse<ChatThreadItem> response = completableFuture.get();
            assertNotNull(response);
            List<ChatThreadItem> threadsInPage = response.getValue();
            assertNotNull(threadsInPage);
            for (ChatThreadItem thread : threadsInPage) {
                returnedThreads.add(thread);
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.client.getChatThreadsNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

// REVISIT: Unreliable assert
        // assertTrue(returnedThreads.size() > 0);
    }
}
