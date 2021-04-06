package com.azure.android.communication.chat.samples;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatClientBuilder;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.autoresttest.BuildConfig;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class CreateChatThread {
    protected static final String ENDPOINT = BuildConfig.COMMUNICATION_SERVICE_ENDPOINT;
    protected static final String ACCESS_KEY = BuildConfig.COMMUNICATION_SERVICE_ACCESS_KEY;
    protected static final String THREAD_MEMBER_1 = BuildConfig.COMMUNICATION_CHAT_THREAD_MEMBER_1;
    protected static final String THREAD_MEMBER_2 = BuildConfig.COMMUNICATION_CHAT_THREAD_MEMBER_2;
    private static CommunicationUserIdentifier secondThreadMember;
    private static CommunicationUserIdentifier firstThreadMember;

    private static ChatAsyncClient client;

    private static ChatClientBuilder getChatClientBuilder() {
        HttpClient httpClient = new OkHttpAsyncClientProvider().createInstance();
        ChatClientBuilder builder = new ChatClientBuilder();

        builder
            .endpoint(ENDPOINT)
            .httpClient(httpClient);

        builder.credentialPolicy(chain -> {
            HttpRequest request = chain.getRequest();
            request.getHeaders().put("Authorization", "Bearer " + ACCESS_KEY);
            chain.processNextPolicy(request);
        });

        return builder;
    }

    protected static CreateChatThreadOptions createThreadOptions(String userId1, String userId2) {
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();
        participants.add(generateParticipant(
            userId1,
            "Tester 1"));
        participants.add(generateParticipant(
            userId2,
            "Tester 2"));

        CreateChatThreadOptions options = new CreateChatThreadOptions()
            .setTopic("Test")
            .setParticipants(participants);

        return options;
    }

    private static ChatParticipant generateParticipant(String id, String displayName) {
        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setCommunicationIdentifier(new CommunicationUserIdentifier(id));
        chatParticipant.setDisplayName(displayName);

        return chatParticipant;
    }

    @BeforeClass
    public static void setup() {
        client = getChatClientBuilder().buildAsyncClient();
        firstThreadMember = new CommunicationUserIdentifier(THREAD_MEMBER_1);
        secondThreadMember = new CommunicationUserIdentifier(THREAD_MEMBER_2);
    }

    @Test
    public void createAndDeleteChatThread() throws ExecutionException, InterruptedException {
        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture);
        CreateChatThreadResult result = completableFuture.get();
        assertNotNull(result);
        assertNotNull(result.getChatThreadProperties());
        assertNotNull(result.getChatThreadProperties().getId());

        CompletableFuture<Void> completableFuture2
            = this.client.deleteChatThread(result.getChatThreadProperties().getId());

        assertNotNull(completableFuture2);
        Void result2 = completableFuture2.get();
        assertNull(result2);
    }

}
