package com.azure.android.communication.androidTests;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.azure.android.communication.chat.AzureCommunicationChatServiceClient;
import com.azure.android.communication.chat.models.AddChatThreadMembersRequest;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatMessagePriority;
import com.azure.android.communication.chat.models.ChatThreadInfo;
import com.azure.android.communication.chat.models.ChatThreadMember;
import com.azure.android.communication.chat.models.CreateChatThreadRequest;
import com.azure.android.communication.chat.models.MultiStatusResponse;
import com.azure.android.communication.chat.models.ReadReceipt;
import com.azure.android.communication.chat.models.SendChatMessageRequest;
import com.azure.android.communication.chat.models.SendChatMessageResult;
import com.azure.android.communication.chat.models.SendReadReceiptRequest;
import com.azure.android.communication.chat.models.UpdateChatMessageRequest;
import com.azure.android.communication.chat.models.UpdateChatThreadRequest;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;
import com.azure.communication.androidTests.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ChatSyncClientTests {

    private static String getStringFromLocalProperties(int stringId) {
        return getInstrumentation().getContext().getResources().getString(stringId);
    }

    private final String targetService = getStringFromLocalProperties(R.string.syncClientTest_siteUrl);
    private final String validToken = "Bearer " + getStringFromLocalProperties(R.string.syncClientTest_token);
    private final String myMemberId =  getStringFromLocalProperties(R.string.syncClientTest_threadOwnerId);
    private final String hisMemberId = getStringFromLocalProperties(R.string.syncClientTest_threadMemberId);

    private String myTopic = "Places to go";
    private String hisTopic = "Food to try";

    private AzureCommunicationChatServiceClient syncClient;

    @Before
    public void setupClient() {
        AzureCommunicationChatServiceClient.Builder chatServiceBuilder = new AzureCommunicationChatServiceClient.Builder();
        chatServiceBuilder.endpoint(targetService)
            .credentialInterceptor(chain -> chain.proceed(chain.request()
                .newBuilder()
                .header(HttpHeader.AUTHORIZATION, validToken)
                .build()));
        syncClient = chatServiceBuilder.build();
    }

    @Test
    public void threadCreateReadUpdateDeleteTest() {
        final Page<ChatThreadInfo> initalThreads = syncClient.listChatThreads();
        assertNotNull(initalThreads);

        Response<MultiStatusResponse> createChatThreadResult = syncClient.createChatThreadWithRestResponse(makeThreadCreationRequest());
        assertEquals(207, createChatThreadResult.getStatusCode());

        String threadId = createChatThreadResult.getValue().getMultipleStatus().get(1).getId();

        Page<ChatThreadInfo> afterCreateThreads = syncClient.listChatThreads();
        while (!afterCreateThreads.getItems().stream().anyMatch(t -> threadId.equals(t.getId()))) {
            afterCreateThreads = syncClient.listChatThreadsNextWithRestResponse(afterCreateThreads.getNextPageId()).getValue();
        }

        ChatThreadInfo threadCreated = afterCreateThreads.getItems()
            .stream()
            .filter(t -> t.getId().equals(threadId))
            .findFirst()
            .get();
        syncClient.updateChatThreadWithRestResponse(threadId, makeUpdateThreadRequest());

        Page<ChatThreadInfo> afterUpdateThread = syncClient.listChatThreads();
        while (!afterUpdateThread.getItems().stream().anyMatch(t -> threadId.equals(t.getId()))) {
            afterUpdateThread = syncClient.listChatThreadsNextWithRestResponse(afterUpdateThread.getNextPageId()).getValue();
        }
        ChatThreadInfo updatedThread = afterUpdateThread.getItems()
            .stream()
            .filter(t -> t.getId().equals(threadId))
            .findFirst()
            .get();
        assertEquals(myTopic, updatedThread.getTopic());

        final Response<Void> deleteResponse = syncClient.deleteChatThreadWithRestResponse(threadId);
        assertEquals(204, deleteResponse.getStatusCode());

        Page<ChatThreadInfo> afterDeleteThreads = syncClient.listChatThreads();
        while (!afterDeleteThreads.getItems().stream().anyMatch(t -> threadId.equals(t.getId()))) {
            afterDeleteThreads = syncClient.listChatThreadsNextWithRestResponse(afterDeleteThreads.getNextPageId()).getValue();
        }
        ChatThreadInfo deletedThread = afterDeleteThreads.getItems()
            .stream()
            .filter(t -> t.getId().equals(threadId))
            .findFirst()
            .get();
        assertTrue(deletedThread.isDeleted());
    }

    @Test
    public void threadMemberCreateReadDeleteTest() {
        final Response<MultiStatusResponse> threadCreated = syncClient.createChatThreadWithRestResponse(makeThreadCreationRequest());

        String threadId = threadCreated.getValue().getMultipleStatus().get(1).getId();
        final Response<Page<ChatThreadMember>> threadMembers = syncClient.listChatThreadMembersWithRestResponse(threadId);
        assertEquals(1, threadMembers.getValue().getItems().size());

        syncClient.addChatThreadMembersWithRestResponse(threadId, makeAddMemberRequest());
        final Response<Page<ChatThreadMember>> membersAfterAdd = syncClient.listChatThreadMembersWithRestResponse(threadId);
        assertEquals(2, membersAfterAdd.getValue().getItems().size());
        assertTrue(membersAfterAdd.getValue().getItems().stream().anyMatch(m -> m.getId().equals(hisMemberId)));

        final Response<Void> removeResponse = syncClient.removeChatThreadMemberWithRestResponse(threadId, hisMemberId);
        assertEquals(204, removeResponse.getStatusCode());

        final Response<Page<ChatThreadMember>> membersAfterDelete = syncClient.listChatThreadMembersWithRestResponse(threadId);
        assertFalse(membersAfterDelete.getValue().getItems().stream().anyMatch(m -> m.getId().equals(hisMemberId)));
    }

    @Test
    public void threadMessageCreateReadUpdateDeleteTest() {
        final Response<MultiStatusResponse> threadCreated = syncClient.createChatThreadWithRestResponse(makeTwoMemberThreadCreationRequest());
        final String threadId = threadCreated.getValue().getMultipleStatus().get(2).getId();

        final Page<ChatMessage> messagesAfterCreate = syncClient.listChatMessages(threadId);
        // when thread is created it has initially two.
        assertEquals(2, messagesAfterCreate.getItems().size());

        final Response<Void> respTypeing = syncClient.sendTypingNotificationWithRestResponse(threadId);
        assertEquals(200, respTypeing.getStatusCode());

        final Response<SendChatMessageResult> firstMessageResponse = syncClient.sendChatMessageWithRestResponse(threadId, makeSendChatMessageRequest());
        assertEquals(201, firstMessageResponse.getStatusCode());

        final Page<ChatMessage> afterSendMessages = syncClient.listChatMessages(threadId);
        assertEquals(3, afterSendMessages.getItems().size());

        final Response<Void> respUpdate = syncClient.updateChatMessageWithRestResponse(threadId, firstMessageResponse.getValue().getId(), createUpdateMessageRequest());
        assertEquals(200, respUpdate.getStatusCode());

        final Page<ChatMessage> afterUpdateMessages = syncClient.listChatMessages(threadId);
        assertFalse(afterUpdateMessages.getItems().stream().anyMatch(m -> m.getContent().equals(afterSendMessages.getItems().get(0).getContent())));

        syncClient.deleteChatMessageWithRestResponse(threadId, firstMessageResponse.getValue().getId());

        final Page<ChatMessage> afterRemoveMessages = syncClient.listChatMessages(threadId);
        // message is not really removed, it only gets delete time set
        assertFalse(afterRemoveMessages.getItems().stream().anyMatch(m -> m.getDeletedOn() == null
            && m.getId().equals(firstMessageResponse.getValue().getId())));
    }

    @Test
    public void threadReceiptNotificationTest() {
        final Response<MultiStatusResponse> threadCreated = syncClient.createChatThreadWithRestResponse(makeTwoMemberThreadCreationRequest());
        final String threadId = threadCreated.getValue().getMultipleStatus().get(2).getId();

        final Response<SendChatMessageResult> firstMessage = syncClient.sendChatMessageWithRestResponse(threadId, makeSendChatMessageRequest());
        //assertEquals(messageSequenceIdString(), firstMessage.getValue().getId());

        syncClient.sendChatReadReceiptWithRestResponse(threadId, makeMessageReadRequest(firstMessage.getValue()));

        final Response<Page<ReadReceipt>> receipts = syncClient.listChatReadReceiptsWithRestResponse(threadId);
        //assertEquals(1, receipts.getValue().getItems().size());
        //assertTrue(receipts.getValue().getItems().stream().anyMatch(r -> r.getSenderId().equals(myMemberId)));
    }

    private SendReadReceiptRequest makeMessageReadRequest(SendChatMessageResult SendChatMessageResult) {
        ++messageSequenceId;
        return new SendReadReceiptRequest()
            .setChatMessageId(SendChatMessageResult.getId());
    }

    private UpdateChatMessageRequest createUpdateMessageRequest() {
        return new UpdateChatMessageRequest().setContent(String.format("message %1$s", ++messageSequenceId));
    }

    static int messageSequenceId = 60;
    private static String messageSequenceIdString() {
        return String.format("%1$s", messageSequenceId);
    }

    private SendChatMessageRequest makeSendChatMessageRequest() {
        ++messageSequenceId;
        return new SendChatMessageRequest()
            .setContent(String.format("message %1$s", messageSequenceId))
            .setPriority(ChatMessagePriority.HIGH)
            .setSenderDisplayName("me-myself");
    }

    private AddChatThreadMembersRequest makeAddMemberRequest() {
        return new AddChatThreadMembersRequest()
            .setMembers(new ArrayList<ChatThreadMember>(Arrays.asList(
                new ChatThreadMember().setId(hisMemberId).setDisplayName("him-himself"))));
    }

    private UpdateChatThreadRequest makeUpdateThreadRequest() {
        myTopic = "Places to go " + messageSequenceIdString();
        return new UpdateChatThreadRequest().setTopic(myTopic);
    }

    private CreateChatThreadRequest makeThreadCreationRequest() {
        hisTopic = "Food to try " + messageSequenceIdString();
        return new CreateChatThreadRequest()
            .setTopic(hisTopic)
            .setMembers(new ArrayList<ChatThreadMember>(Arrays.asList(
                new ChatThreadMember().setId(myMemberId).setDisplayName("me-myself"))));
    }

    private CreateChatThreadRequest makeTwoMemberThreadCreationRequest() {
        hisTopic = "Food to try " + messageSequenceIdString();
        return new CreateChatThreadRequest()
            .setTopic(hisTopic)
            .setMembers(new ArrayList<ChatThreadMember>(Arrays.asList(
                new ChatThreadMember().setId(myMemberId).setDisplayName("me-myself"),
                new ChatThreadMember().setId(hisMemberId).setDisplayName("him-himself"))));
    }

}
