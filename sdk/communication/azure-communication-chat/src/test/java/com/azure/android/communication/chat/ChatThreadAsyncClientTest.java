// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatErrorResponseException;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThreadProperties;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatMessagesOptions;
import com.azure.android.communication.chat.models.ListParticipantsOptions;
import com.azure.android.communication.chat.models.ListReadReceiptOptions;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.SendChatMessageResult;
import com.azure.android.communication.chat.models.TypingNotificationOptions;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.util.paging.PagedAsyncStream;
import com.azure.android.core.test.TestMode;
import com.azure.android.core.test.http.NoOpHttpClient;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.RequestContext;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatThreadAsyncClientTest extends ChatClientTestBase {
    private ClientLogger logger = new ClientLogger(ChatAsyncClientTest.class);

    private ChatAsyncClient client;
    private ChatThreadAsyncClient chatThreadClient;
    private CommunicationUserIdentifier firstThreadParticipant;
    private CommunicationUserIdentifier secondThreadParticipant;
    private String threadId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient) throws ExecutionException, InterruptedException {
        this.client = super.getChatClientBuilder(httpClient).buildAsyncClient();
        this.firstThreadParticipant = new CommunicationUserIdentifier(THREAD_PARTICIPANT_1);
        this.secondThreadParticipant = new CommunicationUserIdentifier(THREAD_PARTICIPANT_2);

        CreateChatThreadOptions threadRequest = createThreadOptions(this.firstThreadParticipant.getId(),
            this.secondThreadParticipant.getId());

        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest).get();
        this.chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThreadProperties().getId());
        this.threadId = chatThreadClient.getChatThreadId();
    }

    private void setupMockTest() {
        final NoOpHttpClient httpClient = new NoOpHttpClient() {
            @Override
            public void send(HttpRequest httpRequest,
                             CancellationToken cancellationToken,
                             HttpCallback httpCallback) {
               httpCallback.onSuccess(ChatResponseMocker.createReadReceiptsResponse(httpRequest));
            }
        };

        this.client = getChatClientBuilder(httpClient).buildAsyncClient();
        String mockThreadId = "19:4b72178530934b7790135dd9359205e0@thread.v2";
        this.chatThreadClient = client.getChatThreadClient(mockThreadId);
        this.threadId = chatThreadClient.getChatThreadId();
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
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

        String expectedThreadId = result1.getChatThreadProperties().getId();

        CompletableFuture<ChatThreadProperties> completableFuture2
            = this.client.getChatThreadClient(expectedThreadId).getProperties();

        assertNotNull(completableFuture2);
        ChatThreadProperties result2 = completableFuture2.get();
        assertNotNull(result2);
        assertNotNull(result2.getId());

        assertEquals(expectedThreadId, result2.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
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

        String expectedThreadId = result1.getChatThreadProperties().getId();

        CompletableFuture<Response<ChatThreadProperties>> completableFuture2
            = this.client.getChatThreadClient(expectedThreadId).getPropertiesWithResponse(null);

        assertNotNull(completableFuture2);
        Response<ChatThreadProperties> response2 = completableFuture2.get();
        assertNotNull(response2);
        ChatThreadProperties result2 = response2.getValue();
        assertNotNull(result2);
        assertNotNull(result2.getId());

        assertEquals(expectedThreadId, result2.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            CompletableFuture<ChatThreadProperties> completableFuture = client
                .getChatThreadClient("19:00000000000000000000000000000000@thread.v2")
                .getProperties();
            completableFuture.get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof ChatErrorResponseException);

        ChatErrorResponseException exception = (ChatErrorResponseException) cause;

        assertNotNull(exception.getResponse());
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            CompletableFuture<Response<ChatThreadProperties>> completableFuture = client
                .getChatThreadClient("19:00000000000000000000000000000000@thread.v2")
                .getPropertiesWithResponse(null);
            completableFuture.get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof ChatErrorResponseException);

        ChatErrorResponseException exception = (ChatErrorResponseException) cause;
        assertNotNull(exception.getResponse());
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotGetChatThreadWithNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        assertThrows(NullPointerException.class, () -> {
            client.getChatThreadClient(null);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        String newTopic = "Update Test";
        this.chatThreadClient.updateTopic(newTopic).get();
        ChatThreadProperties chatThreadProperties = this.client.getChatThreadClient(this.threadId)
            .getProperties().get();
        assertEquals(chatThreadProperties.getTopic(), newTopic);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        String newTopic = "Update Test";
        Response<Void> updateThreadResponse
            = this.chatThreadClient.updateTopicWithResponse(newTopic, null).get();
        assertEquals(204, updateThreadResponse.getStatusCode());
        ChatThreadProperties chatThreadProperties = this.client.getChatThreadClient(this.threadId)
            .getProperties().get();
        assertEquals(chatThreadProperties.getTopic(), newTopic);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotUpdateThreadWithNullTopic(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.updateTopic(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotUpdateThreadWithResponseWithNullTopic(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.updateTopicWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipants(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        Iterable<ChatParticipant> participants = addParticipants(this.firstThreadParticipant.getId(),
            this.secondThreadParticipant.getId());

        this.chatThreadClient.addParticipants(participants).get();

        PagedAsyncStream<ChatParticipant> participantPagedAsyncStream
            = this.chatThreadClient.listParticipants(new ListParticipantsOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        participantPagedAsyncStream.forEach(new AsyncStreamHandler<ChatParticipant>() {
            @Override
            public void onNext(ChatParticipant participant) {
                returnedParticipants.add(participant);
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

        awaitOnLatch(latch, "canAddListAndRemoveParticipants");

        assertTrue(returnedParticipants.size() > 0);
        for (ChatParticipant participant : returnedParticipants) {
            assertNotNull(participant);
            assertNotNull(participant.getDisplayName());
            assertNotNull(participant.getShareHistoryTime());
            assertNotNull(participant.getCommunicationIdentifier());
            assertTrue(participant.getCommunicationIdentifier() instanceof CommunicationUserIdentifier);
            assertNotNull(((CommunicationUserIdentifier)participant.getCommunicationIdentifier()).getId());
        }

        if (TEST_MODE != TestMode.PLAYBACK) {
            for (ChatParticipant participant : participants) {
                assertTrue(super.checkParticipantsListContainsParticipantId(returnedParticipants,
                    ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
            }

            for (ChatParticipant participant : participants) {
                final String id = ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId();
                assertNotNull(id);
                if (!id.equals(this.firstThreadParticipant.getId())) {
                    this.chatThreadClient.removeParticipant(participant.getCommunicationIdentifier()).get();
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipantsWithResponse(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        Iterable<ChatParticipant> participants = addParticipants(this.firstThreadParticipant.getId(),
            this.secondThreadParticipant.getId());

        Response<AddChatParticipantsResult> addResponse
            = this.chatThreadClient.addParticipantsWithResponse(participants, null).get();

        assertNotNull(addResponse);
        assertEquals(201, addResponse.getStatusCode());

        PagedAsyncStream<ChatParticipant> participantPagedAsyncStream
            = this.chatThreadClient.listParticipants(new ListParticipantsOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        participantPagedAsyncStream.forEach(new AsyncStreamHandler<ChatParticipant>() {
            @Override
            public void onNext(ChatParticipant participant) {
                returnedParticipants.add(participant);
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

        awaitOnLatch(latch, "canAddListAndRemoveParticipantsWithResponse");

        assertTrue(returnedParticipants.size() > 0);
        for (ChatParticipant participant : returnedParticipants) {
            assertNotNull(participant);
            assertNotNull(participant.getDisplayName());
            assertNotNull(participant.getShareHistoryTime());
            assertNotNull(participant.getCommunicationIdentifier());
            assertTrue(participant.getCommunicationIdentifier() instanceof CommunicationUserIdentifier);
            assertNotNull(((CommunicationUserIdentifier)participant.getCommunicationIdentifier()).getId());
        }

        if (TEST_MODE != TestMode.PLAYBACK) {
            for (ChatParticipant participant : participants) {
                assertTrue(super.checkParticipantsListContainsParticipantId(returnedParticipants,
                    ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
            }

            for (ChatParticipant participant : participants) {
                final String id = ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId();
                assertNotNull(id);
                if (!id.equals(this.firstThreadParticipant.getId())) {
                    this.chatThreadClient.removeParticipant(participant.getCommunicationIdentifier()).get();
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotAddParticipantsWithInvalidUser(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        Iterable<ChatParticipant> participants = addParticipants("8:acs:invalidUserId",
            this.secondThreadParticipant.getId());

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.addParticipants(participants).get();
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
    public void cannotAddParticipantsWithResponseWithInvalidUser(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        Iterable<ChatParticipant> participants = addParticipants("8:acs:invalidUserId",
            this.secondThreadParticipant.getId());

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.addParticipantsWithResponse(participants, null).get();
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
    public void canAddSingleParticipant(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        this.chatThreadClient.addParticipant(new ChatParticipant()
            .setCommunicationIdentifier(this.firstThreadParticipant))
            .get();

        PagedAsyncStream<ChatParticipant> participantPagedAsyncStream
            = this.chatThreadClient.listParticipants(new ListParticipantsOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        participantPagedAsyncStream.forEach(new AsyncStreamHandler<ChatParticipant>() {
            @Override
            public void onNext(ChatParticipant participant) {
                returnedParticipants.add(participant);
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

        awaitOnLatch(latch, "canAddSingleParticipant");

        assertTrue(returnedParticipants.size() > 0);
        for (ChatParticipant participant : returnedParticipants) {
            assertNotNull(participant);
            assertNotNull(participant.getDisplayName());
            assertNotNull(participant.getShareHistoryTime());
            assertNotNull(participant.getCommunicationIdentifier());
            assertTrue(participant.getCommunicationIdentifier() instanceof CommunicationUserIdentifier);
            assertNotNull(((CommunicationUserIdentifier)participant.getCommunicationIdentifier()).getId());
        }

        if (TEST_MODE != TestMode.PLAYBACK) {
            assertTrue(super.checkParticipantsListContainsParticipantId(returnedParticipants,
                this.firstThreadParticipant.getId()));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantWithResponse(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        Response<Void> addResponse = this.chatThreadClient
            .addParticipantWithResponse(
                new ChatParticipant().setCommunicationIdentifier(this.firstThreadParticipant), null)
            .get();

        assertNotNull(addResponse);
        assertEquals(201, addResponse.getStatusCode());

        PagedAsyncStream<ChatParticipant> participantPagedAsyncStream
            = this.chatThreadClient.listParticipants(new ListParticipantsOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        participantPagedAsyncStream.forEach(new AsyncStreamHandler<ChatParticipant>() {
            @Override
            public void onNext(ChatParticipant participant) {
                returnedParticipants.add(participant);
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

        awaitOnLatch(latch, "canAddSingleParticipantWithResponse");

        assertTrue(returnedParticipants.size() > 0);
        for (ChatParticipant participant : returnedParticipants) {
            assertNotNull(participant);
            assertNotNull(participant.getDisplayName());
            assertNotNull(participant.getShareHistoryTime());
            assertNotNull(participant.getCommunicationIdentifier());
            assertTrue(participant.getCommunicationIdentifier() instanceof CommunicationUserIdentifier);
            assertNotNull(((CommunicationUserIdentifier)participant.getCommunicationIdentifier()).getId());
        }

        if (TEST_MODE != TestMode.PLAYBACK) {
            assertTrue(super.checkParticipantsListContainsParticipantId(returnedParticipants,
                this.firstThreadParticipant.getId()));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotAddSingleParticipantWithInvalidUser(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.addParticipant(generateParticipant("8:acs:invalidUserId", "name"))
                .get();
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
    public void cannotAddSingleParticipantWithResponseWithInvalidUser(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.addParticipantWithResponse(
                generateParticipant("8:acs:invalidUserId", "name"),
                null).get();
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
    public void canSendThenGetHtmlMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = new SendChatMessageOptions()
            .setType(ChatMessageType.HTML)
            .setSenderDisplayName("John")
            .setContent("<div>test</div>")
            .setMetadata(new HashMap<String, String>() {
                {
                    put("tags", "");
                    put("deliveryMode", "deliveryMode value - updated");
                    put("onedriveReferences", "onedriveReferences - updated");
                    put("amsreferences", "[\\\"test url file 3\\\"]");
                }
            });

        final String messageId = this.chatThreadClient.sendMessage(messageRequest).get().getId();
        final ChatMessage message = this.chatThreadClient.getMessage(messageId).get();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getType(), messageRequest.getType());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
        assertEquals(message.getMetadata(), messageRequest.getMetadata());
    }


    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotAddParticipantsWithNullOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.addParticipants(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotAddParticipantsWithResponseWithNullOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.addParticipantsWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotRemoveParticipantWithNullUser(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.removeParticipant(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotRemoveParticipantWithNullUserWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.removeParticipantWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotRemoveParticipantWithInvalidUser(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.removeParticipant(new CommunicationUserIdentifier("8:acs:invalidUserId")).get();
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
    public void cannotRemoveParticipantWithResponseWithInvalidUser(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.removeParticipantWithResponse(new CommunicationUserIdentifier("8:acs:invalidUserId"), null).get();
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
    public void canSendThenGetMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        final String messageId = this.chatThreadClient.sendMessage(messageRequest).get().getId();
        final ChatMessage message = this.chatThreadClient.getMessage(messageId).get();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getType(), messageRequest.getType());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
        assertEquals(message.getMetadata(), messageRequest.getMetadata());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessageWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        CompletableFuture<Response<SendChatMessageResult>> completableFuture1
            = this.chatThreadClient.sendMessageWithResponse(messageRequest, null);

        Response<SendChatMessageResult> response1 = completableFuture1.get();
        assertNotNull(response1);
        final String messageId = response1.getValue().getId();

        CompletableFuture<Response<ChatMessage>> completableFuture2
            = this.chatThreadClient.getMessageWithResponse(messageId, null);

        Response<ChatMessage> response2 = completableFuture2.get();
        assertNotNull(response2);
        final ChatMessage message = response2.getValue();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getType(), messageRequest.getType());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
        assertEquals(message.getMetadata(), messageRequest.getMetadata());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotSendMessageWithNullOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.sendMessage(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotSendMessageWithResponseWithNullOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.sendMessageWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotGetMessageNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.getMessage(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotGetMessageWithResponseNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.getMessageWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotGetMessageWithInvalidId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.getMessage("invalid_chat_message_id").get();
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
    public void cannotGetMessageWithResponseWithInvalidId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.getMessageWithResponse("invalid_chat_message_id", null).get();
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
    public void canDeleteExistingMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        CompletableFuture<SendChatMessageResult> completableFuture1 = this.chatThreadClient.sendMessage(messageRequest);
        String messageId = completableFuture1.get().getId();
        assertNotNull(messageId);
        CompletableFuture<Void> completableFuture2 = this.chatThreadClient.deleteMessage(messageId);
        completableFuture2.get();
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteMessageNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.deleteMessage(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteMessageWithResponseNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.deleteMessageWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        String messageId = this.chatThreadClient.sendMessage(messageRequest).get().getId();
        CompletableFuture<Response<Void>> completableFuture = chatThreadClient.deleteMessageWithResponse(messageId,
            null);
        Response<Void> deleteResponse = completableFuture.get();
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotDeleteMessageWithInvalidId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.deleteMessage("invalid_chat_message_id").get();
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
    public void cannotDeleteMessageWithResponseWithInvalidId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.deleteMessageWithResponse("invalid_chat_message_id", null).get();
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
    public void canUpdateExistingMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();

        final String messageId = this.chatThreadClient.sendMessage(messageRequest).get().getId();
        this.chatThreadClient.updateMessage(messageId, updateMessageRequest).get();
        final ChatMessage message = chatThreadClient.getMessage(messageId).get();
        assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());
        assertEquals(message.getMetadata().containsKey("tags"), false);
        assertEquals(message.getMetadata().get("deliveryMode"), updateMessageRequest.getMetadata().get("deliveryMode"));
        assertEquals(message.getMetadata().get("onedriveReferences"), updateMessageRequest.getMetadata().get("onedriveReferences"));
        assertEquals(message.getMetadata().get("amsreferences"), messageRequest.getMetadata().get("amsreferences"));
        assertEquals(message.getMetadata().get("key"), messageRequest.getMetadata().get("key"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotUpdateMessageNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.updateMessage(null, updateMessageRequest).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotUpdateMessageWithResponseNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.updateMessageWithResponse(null, updateMessageRequest, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessageWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();

        CompletableFuture<Response<SendChatMessageResult>> completableFuture1 = this.chatThreadClient.sendMessageWithResponse(messageRequest, null);
        Response<SendChatMessageResult> sendResponse = completableFuture1.get();
        assertNotNull(sendResponse);
        final String messageId = sendResponse.getValue().getId();
        assertNotNull(messageId);

        CompletableFuture<Response<Void>> completableFuture2 = this.chatThreadClient.updateMessageWithResponse(messageId, updateMessageRequest, null);
        Response<Void> updateResponse = completableFuture2.get();
        assertNotNull(updateResponse);
        assertEquals(204, updateResponse.getStatusCode());

        CompletableFuture<Response<ChatMessage>> completableFuture3 = chatThreadClient.getMessageWithResponse(messageId, null);
        Response<ChatMessage> getResponse = completableFuture3.get();
        assertNotNull(getResponse);

        ChatMessage message = getResponse.getValue();
        assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());
        assertEquals(message.getMetadata().containsKey("tags"), false);
        assertEquals(message.getMetadata().get("deliveryMode"), updateMessageRequest.getMetadata().get("deliveryMode"));
        assertEquals(message.getMetadata().get("onedriveReferences"), updateMessageRequest.getMetadata().get("onedriveReferences"));
        assertEquals(message.getMetadata().get("amsreferences"), messageRequest.getMetadata().get("amsreferences"));
        assertEquals(message.getMetadata().get("key"), messageRequest.getMetadata().get("key"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotUpdateMessageWithInvalidId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.updateMessage("invalid_chat_message_id", updateMessageRequest).get();
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
    public void cannotUpdateMessageWithResponseWithInvalidId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.updateMessageWithResponse("invalid_chat_message_id", updateMessageRequest, null).get();
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
    public void canListMessages(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        this.chatThreadClient.sendMessage(messageRequest).get();
        this.chatThreadClient.sendMessage(messageRequest).get();

        PagedAsyncStream<ChatMessage> messagePagedAsyncStream
            = this.chatThreadClient.listMessages(new ListChatMessagesOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatMessage> returnedMessages = new ArrayList<ChatMessage>();
        messagePagedAsyncStream.forEach(new AsyncStreamHandler<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                if (message.getType().equals(ChatMessageType.TEXT)) {
                    returnedMessages.add(message);
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

        awaitOnLatch(latch, "canListMessages");

        assertTrue(returnedMessages.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListMessagesWithOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        ListChatMessagesOptions options = new ListChatMessagesOptions();
        options.setMaxPageSize(10);
        options.setStartTime(OffsetDateTime.parse("2020-09-08T01:02:14.387Z"));

        this.chatThreadClient.sendMessage(messageRequest).get();
        this.chatThreadClient.sendMessage(messageRequest).get();

        PagedAsyncStream<ChatMessage> messagePagedAsyncStream
            = this.chatThreadClient.listMessages(options, null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatMessage> returnedMessages = new ArrayList<ChatMessage>();
        messagePagedAsyncStream.forEach(new AsyncStreamHandler<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                if (message.getType().equals(ChatMessageType.TEXT)) {
                    returnedMessages.add(message);
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

        awaitOnLatch(latch, "canListMessagesWithOptions");

        assertTrue(returnedMessages.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        this.chatThreadClient.sendTypingNotification().get();
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        TypingNotificationOptions options = new TypingNotificationOptions();
        options.setSenderDisplayName("Sender Display Name");

        this.chatThreadClient.sendTypingNotification(options, RequestContext.NONE).get();
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        CompletableFuture<Response<Void>> completableFuture
            = this.chatThreadClient.sendTypingNotificationWithResponse(null);

        Response<Void> response = completableFuture.get();
        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithOptionsWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        TypingNotificationOptions options = new TypingNotificationOptions();
        options.setSenderDisplayName("Sender Display Name");

        CompletableFuture<Response<Void>> completableFuture
            = this.chatThreadClient.sendTypingNotificationWithResponse(options, null);

        Response<Void> response = completableFuture.get();
        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListReadReceipts(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupMockTest();

        PagedAsyncStream<ChatMessageReadReceipt> readReceiptPagedAsyncStream
            = this.chatThreadClient.listReadReceipts(new ListReadReceiptOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
        readReceiptPagedAsyncStream.forEach(new AsyncStreamHandler<ChatMessageReadReceipt>() {
            @Override
            public void onNext(ChatMessageReadReceipt readReceipt) {
                readReceiptList.add(readReceipt);
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

        awaitOnLatch(latch, "canListReadReceipts");

        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSenderCommunicationIdentifier());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListReadReceiptsWithOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupMockTest();

        PagedAsyncStream<ChatMessageReadReceipt> readReceiptPagedAsyncStream
            = this.chatThreadClient.listReadReceipts(new ListReadReceiptOptions(), null);

        CountDownLatch latch = new CountDownLatch(1);

        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
        readReceiptPagedAsyncStream.forEach(new AsyncStreamHandler<ChatMessageReadReceipt>() {
            @Override
            public void onNext(ChatMessageReadReceipt readReceipt) {
                readReceiptList.add(readReceipt);
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

        awaitOnLatch(latch, "canListReadReceiptsWithOptions");

        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSenderCommunicationIdentifier());
    }

    @Disabled("Unreliable test")
    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendReadReceipt(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        String messageId = this.chatThreadClient.sendMessage(messageRequest).get().getId();
        this.chatThreadClient.sendReadReceipt(messageId).get();
    }

    @Disabled("Unreliable test")
    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendReadReceiptWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        String messageId = this.chatThreadClient.sendMessage(messageRequest).get().getId();
        CompletableFuture<Response<Void>> completableFuture = this.chatThreadClient.sendReadReceiptWithResponse(messageId, null);
        Response<Void> response = completableFuture.get();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotSendReadReceiptWithNullMessageId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.sendReadReceipt(null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void cannotSendReadReceiptWithResponseWithNullMessageId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.sendReadReceiptWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }
}