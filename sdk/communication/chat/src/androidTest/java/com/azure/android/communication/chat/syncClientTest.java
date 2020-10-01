package com.azure.android.communication.chat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.azure.android.communication.chat.models.AddChatThreadMembersOptions;
import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatMessagePriority;
import com.azure.android.communication.chat.models.ChatThreadInfo;
import com.azure.android.communication.chat.models.ChatThreadMember;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.MultiStatusResponse;
import com.azure.android.communication.chat.models.ReadReceipt;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.SendChatMessageResult;
import com.azure.android.communication.chat.models.SendReadReceiptRequest;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatThreadOptions;
import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class syncClientTest {

    private final String targetService = "https://chat-prod-e2e.communication.azure.com";
    private final String validToken = "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOmZhNWM0ZmMzLWEyNjktNDNlMi05ZWI2LTBjYTE3YjM4ODk5M18wMDAwMDAwNS04NWY2LTU5NjYtNzc0My0wODQ4MjIwMDAwYWYiLCJzY3AiOjE3OTIsImNzaSI6IjE2MDE1ODc5NjEiLCJpYXQiOjE2MDE1ODc5NjEsImV4cCI6MTYwMTY3NDM2MSwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6ImZhNWM0ZmMzLWEyNjktNDNlMi05ZWI2LTBjYTE3YjM4ODk5MyJ9.BswYJ0SsJjy_KaHoZNCb2Krzr4wt9UsjEMgTbQWpmj1Lq99lFhNV1GIp0yJFPCT_QfQXpVizUB2hQxf5tvI208SF-NgFwld3TR7HB6pHB5Y3f6rGK_yOvjTAItcyRDrc4TF8jyD-tNd4msLHpLIbw1SQNZDcEf7BEn1E60fGX2-XHoqVECYShIpgPyW0VAW2pPPBTstd1uvhDQ-TZoNFaRoi5YjM3jHpfTNwqaL38QBbexrixoddU8sD5gTz_ZtHP_AAQxHUAD2piej5ViSWo4MNmLuKrkXB0AVRJUOQh8i5bLVp09kDqFozF3LYOLKrPDPpekQPuMA2Idz1ysM6zw";

    private final String myMemberId =  "8:acs:fa5c4fc3-a269-43e2-9eb6-0ca17b388993_00000005-85f6-5966-7743-0848220000af";
    private final String hisMemberId = "8:acs:fa5c4fc3-a269-43e2-9eb6-0ca17b388993_00000005-85f5-d540-7743-0848220000ae";

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

        final Page<ChatThreadInfo> afterCreateThreads = syncClient.listChatThreads();
        assertTrue(afterCreateThreads.getItems().stream().anyMatch(t -> threadId.equals(t.getId())));

        ChatThreadInfo threadCreated = afterCreateThreads.getItems()
            .stream()
            .filter(t -> t.getId().equals(threadId))
            .findFirst()
            .get();
        syncClient.updateChatThreadWithRestResponse(threadCreated.getId(), makeUpdateThreadRequest());

        final Page<ChatThreadInfo> afterUpdateThread = syncClient.listChatThreads();
        ChatThreadInfo updatedThread = afterUpdateThread.getItems()
            .stream()
            .filter(t -> t.getId().equals(threadCreated.getId()))
            .findFirst()
            .get();
        assertEquals(myTopic, updatedThread.getTopic());

        final Response<Void> deleteResponse = syncClient.deleteChatThreadWithRestResponse(threadId);
        assertEquals(204, deleteResponse.getStatusCode());

        final Page<ChatThreadInfo> afterDeleteThreads = syncClient.listChatThreads();
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

        syncClient.removeChatThreadMemberWithRestResponse(threadId, hisMemberId);

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

        final Response<SendChatMessageResult> firstMessage = syncClient.sendChatMessageWithRestResponse(threadId, makeSendChatMessageRequest());
        //assertEquals(messageSequenceIdString(), firstMessage.getValue().getId());

        final Page<ChatMessage> afterSendMessages = syncClient.listChatMessages(threadId);
        assertEquals(3, afterSendMessages.getItems().size());

        final Response<Void> respUpdate = syncClient.updateChatMessageWithRestResponse(threadId, firstMessage.getValue().getId(), createUpdateMessageRequest());
        assertEquals(200, respUpdate.getStatusCode());

        final Page<ChatMessage> afterUpdateMessages = syncClient.listChatMessages(threadId);
        assertFalse(afterUpdateMessages.getItems().stream().anyMatch(m -> m.getContent().equals(afterSendMessages.getItems().get(0).getContent())));

        syncClient.deleteChatMessageWithRestResponse(threadId, firstMessage.getValue().getId());

        final Page<ChatMessage> afterRemoveMessages = syncClient.listChatMessages(threadId);
        // message is not really removed, it only gets delete time set
        assertFalse(afterRemoveMessages.getItems().stream().anyMatch(m -> m.getDeletedOn() == null && m.getId().equals(firstMessage.getValue().getId())));
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
        assertTrue(receipts.getValue().getItems().stream().anyMatch(r -> r.getSenderId().equals(myMemberId)));
    }

    private SendReadReceiptRequest makeMessageReadRequest(SendChatMessageResult SendChatMessageResult) {
        ++messageSequenceId;
        return new SendReadReceiptRequest()
            .setChatMessageId(SendChatMessageResult.getId());
    }

    private UpdateChatMessageOptions createUpdateMessageRequest() {
        return new UpdateChatMessageOptions().setContent(String.format("message %1$s", ++messageSequenceId));
    }

    static int messageSequenceId = 60;
    private static String messageSequenceIdString() {
        return String.format("%1$s", messageSequenceId);
    }

    private SendChatMessageOptions makeSendChatMessageRequest() {
        ++messageSequenceId;
        return new SendChatMessageOptions()
            .setContent(String.format("message %1$s", messageSequenceId))
            .setPriority(ChatMessagePriority.HIGH)
            .setSenderDisplayName("me-myself");
    }

    private AddChatThreadMembersOptions makeAddMemberRequest() {
        return new AddChatThreadMembersOptions()
            .setMembers(new ArrayList<ChatThreadMember>(Arrays.asList(
                new ChatThreadMember().setId(hisMemberId).setDisplayName("him-himself"))));
    }

    private UpdateChatThreadOptions makeUpdateThreadRequest() {
        myTopic = "Places to go " + messageSequenceIdString();
        return new UpdateChatThreadOptions().setTopic(myTopic);
    }

    private CreateChatThreadOptions makeThreadCreationRequest() {
        hisTopic = "Food to try " + messageSequenceIdString();
        return new CreateChatThreadOptions()
            .setTopic(hisTopic)
            .setMembers(new ArrayList<ChatThreadMember>(Arrays.asList(
                new ChatThreadMember().setId(myMemberId).setDisplayName("me-myself"))));
    }

    private CreateChatThreadOptions makeTwoMemberThreadCreationRequest() {
        hisTopic = "Food to try " + messageSequenceIdString();
        return new CreateChatThreadOptions()
            .setTopic(hisTopic)
            .setMembers(new ArrayList<ChatThreadMember>(Arrays.asList(
                new ChatThreadMember().setId(myMemberId).setDisplayName("me-myself"),
                new ChatThreadMember().setId(hisMemberId).setDisplayName("him-himself"))));
    }

}
