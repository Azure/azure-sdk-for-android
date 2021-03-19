// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.AddChatParticipantsOptions;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.CommunicationErrorResponseException;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListChatMessagesOptions;
import com.azure.android.communication.chat.models.ListParticipantsOptions;
import com.azure.android.communication.chat.models.ListReadReceiptOptions;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.PagedResponse;
import com.azure.android.core.rest.Response;
import com.azure.android.core.test.TestMode;
import com.azure.android.core.test.http.NoOpHttpClient;
import com.azure.android.core.util.CancellationToken;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;
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
    private CommunicationUserIdentifier firstThreadMember;
    private CommunicationUserIdentifier secondThreadMember;
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
        this.firstThreadMember = new CommunicationUserIdentifier(THREAD_MEMBER_1);
        this.secondThreadMember = new CommunicationUserIdentifier(THREAD_MEMBER_2);

        CreateChatThreadOptions threadRequest = createThreadOptions(this.firstThreadMember.getId(),
            this.secondThreadMember.getId());

        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest).get();
        this.chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
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
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture1
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture1);
        CreateChatThreadResult result1 = completableFuture1.get();
        assertNotNull(result1);
        assertNotNull(result1.getChatThread());
        assertNotNull(result1.getChatThread().getId());

        String expectedThreadId = result1.getChatThread().getId();

        CompletableFuture<ChatThread> completableFuture2
            = this.client.getChatThreadClient(expectedThreadId).getChatThreadProperties();

        assertNotNull(completableFuture2);
        ChatThread result2 = completableFuture2.get();
        assertNotNull(result2);
        assertNotNull(result2.getId());

        assertEquals(expectedThreadId, result2.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
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

        String expectedThreadId = result1.getChatThread().getId();

        CompletableFuture<Response<ChatThread>> completableFuture2
            = this.client.getChatThreadClient(expectedThreadId).getChatThreadPropertiesWithResponse(null);

        assertNotNull(completableFuture2);
        Response<ChatThread> response2 = completableFuture2.get();
        assertNotNull(response2);
        ChatThread result2 = response2.getValue();
        assertNotNull(result2);
        assertNotNull(result2.getId());

        assertEquals(expectedThreadId, result2.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThread(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            CompletableFuture<ChatThread> completableFuture = client
                .getChatThreadClient("19:00000000000000000000000000000000@thread.v2")
                .getChatThreadProperties();
            completableFuture.get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof HttpResponseException);

        HttpResponseException exception = (HttpResponseException) cause;

        assertNotNull(exception.getResponse());
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            CompletableFuture<Response<ChatThread>> completableFuture = client
                .getChatThreadClient("19:00000000000000000000000000000000@thread.v2")
                .getChatThreadPropertiesWithResponse(null);
            completableFuture.get();
        });

        Throwable cause = executionException.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof HttpResponseException);

        HttpResponseException exception = (HttpResponseException) cause;
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
        ChatThread chatThread = this.client.getChatThreadClient(this.threadId)
            .getChatThreadProperties().get();
        assertEquals(chatThread.getTopic(), newTopic);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        String newTopic = "Update Test";
        Response<Void> updateThreadResponse
            = this.chatThreadClient.updateTopicWithResponse(newTopic, null).get();
        assertEquals(204, updateThreadResponse.getStatusCode());
        ChatThread chatThread = this.client.getChatThreadClient(this.threadId)
            .getChatThreadProperties().get();
        assertEquals(chatThread.getTopic(), newTopic);
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
    public void canAddListAndRemoveMembers(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        AddChatParticipantsOptions options = addParticipantsOptions(this.firstThreadMember.getId(),
            this.secondThreadMember.getId());

        this.chatThreadClient.addParticipants(options).get();


        CompletableFuture<PagedResponse<ChatParticipant>> completableFuture
            = this.chatThreadClient.getParticipantsFirstPageWithResponse(new ListParticipantsOptions(), null);


        String nextLink;
        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        do {
            PagedResponse<ChatParticipant> response = completableFuture.get();
            assertNotNull(response);
            List<ChatParticipant> participantsInPage = response.getValue();
            assertNotNull(participantsInPage);
            for (ChatParticipant thread : participantsInPage) {
                returnedParticipants.add(thread);
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.chatThreadClient.getParticipantsNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

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
            for (ChatParticipant participant : options.getParticipants()) {
                assertTrue(super.checkParticipantsListContainsParticipantId(returnedParticipants,
                    ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
            }

            for (ChatParticipant participant : options.getParticipants()) {
                final String id = ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId();
                assertNotNull(id);
                if (!id.equals(this.firstThreadMember.getId())) {
                    this.chatThreadClient.removeParticipant(participant.getCommunicationIdentifier()).get();
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithResponse(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        AddChatParticipantsOptions options = addParticipantsOptions(this.firstThreadMember.getId(),
            this.secondThreadMember.getId());

        Response<AddChatParticipantsResult> addResponse
            = this.chatThreadClient.addParticipantsWithResponse(options, null).get();

        assertNotNull(addResponse);
        assertEquals(201, addResponse.getStatusCode());

        CompletableFuture<PagedResponse<ChatParticipant>> completableFuture
            = this.chatThreadClient.getParticipantsFirstPageWithResponse(new ListParticipantsOptions(), null);


        String nextLink;
        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        do {
            PagedResponse<ChatParticipant> response = completableFuture.get();
            assertNotNull(response);
            List<ChatParticipant> participantsInPage = response.getValue();
            assertNotNull(participantsInPage);
            for (ChatParticipant thread : participantsInPage) {
                returnedParticipants.add(thread);
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.chatThreadClient.getParticipantsNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

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
            for (ChatParticipant participant : options.getParticipants()) {
                assertTrue(super.checkParticipantsListContainsParticipantId(returnedParticipants,
                    ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
            }

            for (ChatParticipant participant : options.getParticipants()) {
                final String id = ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId();
                assertNotNull(id);
                if (!id.equals(this.firstThreadMember.getId())) {
                    this.chatThreadClient.removeParticipant(participant.getCommunicationIdentifier()).get();
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipant(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        this.chatThreadClient.addParticipant(new ChatParticipant()
            .setCommunicationIdentifier(this.firstThreadMember))
            .get();

        CompletableFuture<PagedResponse<ChatParticipant>> completableFuture
            = this.chatThreadClient.getParticipantsFirstPageWithResponse(new ListParticipantsOptions(), null);


        String nextLink;
        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        do {
            PagedResponse<ChatParticipant> response = completableFuture.get();
            assertNotNull(response);
            List<ChatParticipant> participantsInPage = response.getValue();
            assertNotNull(participantsInPage);
            for (ChatParticipant thread : participantsInPage) {
                returnedParticipants.add(thread);
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.chatThreadClient.getParticipantsNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

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
                this.firstThreadMember.getId()));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantWithResponse(HttpClient httpClient) throws InterruptedException, ExecutionException {
        setupTest(httpClient);

        Response<AddChatParticipantsResult> addResponse = this.chatThreadClient
            .addParticipantWithResponse(
                new ChatParticipant().setCommunicationIdentifier(this.firstThreadMember), null)
            .get();

        assertNotNull(addResponse);
        assertEquals(201, addResponse.getStatusCode());

        CompletableFuture<PagedResponse<ChatParticipant>> completableFuture
            = this.chatThreadClient.getParticipantsFirstPageWithResponse(new ListParticipantsOptions(), null);

        String nextLink;
        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        do {
            PagedResponse<ChatParticipant> response = completableFuture.get();
            assertNotNull(response);
            List<ChatParticipant> participantsInPage = response.getValue();
            assertNotNull(participantsInPage);
            for (ChatParticipant thread : participantsInPage) {
                returnedParticipants.add(thread);
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.chatThreadClient.getParticipantsNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

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
                this.firstThreadMember.getId()));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendThenGetHtmlMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = new SendChatMessageOptions()
            .setType(ChatMessageType.HTML)
            .setSenderDisplayName("John")
            .setContent("<div>test</div>");

        final String messageId = this.chatThreadClient.sendMessage(messageRequest).get();
        final ChatMessage message = this.chatThreadClient.getMessage(messageId).get();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getType(), messageRequest.getType());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
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
    public void canSendThenGetMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        final String messageId = this.chatThreadClient.sendMessage(messageRequest).get();
        final ChatMessage message = this.chatThreadClient.getMessage(messageId).get();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getType(), messageRequest.getType());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessageWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        CompletableFuture<Response<String>> completableFuture1
            = this.chatThreadClient.sendMessageWithResponse(messageRequest, null);

        Response<String> response1 = completableFuture1.get();
        assertNotNull(response1);
        final String messageId = response1.getValue();

        CompletableFuture<Response<ChatMessage>> completableFuture2
            = this.chatThreadClient.getMessageWithResponse(messageId, null);

        Response<ChatMessage> response2 = completableFuture2.get();
        assertNotNull(response2);
        final ChatMessage message = response2.getValue();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getType(), messageRequest.getType());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
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
    public void cannotGetMessageWithReponseNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.getMessageWithResponse(null, null).get();
        });

        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        CompletableFuture<String> completableFuture1 = this.chatThreadClient.sendMessage(messageRequest);
        String messageId = completableFuture1.get();
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
    public void cannotDeleteMessageWithRepsonseNullId(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> {
            this.chatThreadClient.deleteMessageWithResponse(null, null).get();
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        String messageId = this.chatThreadClient.sendMessage(messageRequest).get();
        CompletableFuture<Response<Void>> completableFuture = chatThreadClient.deleteMessageWithResponse(messageId,
            null);
        Response<Void> deleteResponse = completableFuture.get();
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessage(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = super.updateMessageOptions();

        final String messageId = this.chatThreadClient.sendMessage(messageRequest).get();
        this.chatThreadClient.updateMessage(messageId, updateMessageRequest).get();
        final ChatMessage message = chatThreadClient.getMessage(messageId).get();
        assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());
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

        CompletableFuture<Response<String>> completableFuture1 = this.chatThreadClient.sendMessageWithResponse(messageRequest, null);
        Response<String> sendResponse = completableFuture1.get();
        assertNotNull(sendResponse);
        final String messageId = sendResponse.getValue();
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
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListMessages(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();

        this.chatThreadClient.sendMessage(messageRequest).get();
        this.chatThreadClient.sendMessage(messageRequest).get();

         CompletableFuture<PagedResponse<ChatMessage>> completableFuture
            = this.chatThreadClient.getMessagesFirstPageWithResponse(new ListChatMessagesOptions(), null);

        String nextLink;
        List<ChatMessage> returnedThreads = new ArrayList<ChatMessage>();
        do {
            PagedResponse<ChatMessage> response = completableFuture.get();
            assertEquals(200, response.getStatusCode());
            assertNotNull(response);
            List<ChatMessage> messagesInPage = response.getValue();
            assertNotNull(messagesInPage);
            for (ChatMessage thread : messagesInPage) {
                if (thread.getType().equals(ChatMessageType.TEXT)) {
                    returnedThreads.add(thread);
                }
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.chatThreadClient.getMessagesNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

        assertTrue(returnedThreads.size() > 0);
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

        CompletableFuture<PagedResponse<ChatMessage>> completableFuture
            = this.chatThreadClient.getMessagesFirstPageWithResponse(options, null);

        String nextLink;
        List<ChatMessage> returnedThreads = new ArrayList<ChatMessage>();
        do {
            PagedResponse<ChatMessage> response = completableFuture.get();
            assertEquals(200, response.getStatusCode());
            assertNotNull(response);
            List<ChatMessage> messagesInPage = response.getValue();
            assertNotNull(messagesInPage);
            for (ChatMessage thread : messagesInPage) {
                if (thread.getType().equals(ChatMessageType.TEXT)) {
                    returnedThreads.add(thread);
                }
            }

            nextLink = response.getContinuationToken();
            if (nextLink != null) {
                completableFuture
                    = this.chatThreadClient.getMessagesNextPageWithResponse(nextLink, null);
            }
        } while (nextLink != null);

        assertTrue(returnedThreads.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        this.chatThreadClient.sendTypingNotification().get();
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
    public void canListReadReceipts(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupMockTest();

        CompletableFuture<PagedResponse<ChatMessageReadReceipt>> completableFuture
            = this.chatThreadClient.getReadReceiptsFirstPageWithResponse(new ListReadReceiptOptions(), null);

        PagedResponse<ChatMessageReadReceipt> response = completableFuture.get();
        List<ChatMessageReadReceipt> readReceiptList = response.getValue();
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSenderCommunicationIdentifier());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canListReadReceiptsWithOptions(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupMockTest();

        CompletableFuture<PagedResponse<ChatMessageReadReceipt>> completableFuture
            = this.chatThreadClient.getReadReceiptsFirstPageWithResponse(new ListReadReceiptOptions(), null);

        PagedResponse<ChatMessageReadReceipt> response = completableFuture.get();
        List<ChatMessageReadReceipt> readReceiptList = response.getValue();
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSenderCommunicationIdentifier());
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendReadReceipt(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        String messageId = this.chatThreadClient.sendMessage(messageRequest).get();
        this.chatThreadClient.sendReadReceipt(messageId).get();
    }

    @ParameterizedTest
    @MethodSource("com.azure.android.core.test.TestBase#getHttpClients")
    public void canSendReadReceiptWithResponse(HttpClient httpClient) throws ExecutionException, InterruptedException {
        setupTest(httpClient);

        SendChatMessageOptions messageRequest = super.sendMessageOptions();
        String messageId = this.chatThreadClient.sendMessage(messageRequest).get();
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
