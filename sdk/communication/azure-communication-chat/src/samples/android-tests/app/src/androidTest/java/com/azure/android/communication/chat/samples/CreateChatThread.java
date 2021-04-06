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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class CreateChatThread {
    protected static final String ENDPOINT;
    protected static final String ACCESS_KEY;
    protected static final String THREAD_MEMBER_1;
    protected static final String THREAD_MEMBER_2;
    private static CommunicationUserIdentifier secondThreadMember;
    private static CommunicationUserIdentifier firstThreadMember;

    static {
        final String endpoint = getConfig("COMMUNICATION_SERVICE_ENDPOINT");
        ENDPOINT = endpoint != null ? endpoint : "https://playback.chat.azurefd.net";

        final String accessKey = getConfig("COMMUNICATION_SERVICE_ACCESS_KEY");
        ACCESS_KEY = accessKey != null ? accessKey : "pw==";

        final String threadMember1 = getConfig("COMMUNICATION_CHAT_THREAD_MEMBER_1");
        THREAD_MEMBER_1 = threadMember1 != null ? threadMember1 : "1:acs:00000000-0000-0000-0000-000000000000_00000000-0000-0000-0000-000000000001";

        final String threadMember2 = getConfig("COMMUNICATION_CHAT_THREAD_MEMBER_2");
        THREAD_MEMBER_2 = threadMember2 != null ? threadMember2 : "2:acs:00000000-0000-0000-0000-000000000000_00000000-0000-0000-0000-000000000002";
    }

    protected static String getConfig(String name) {
        String value = System.getProperty(name);

        if (value != null) {
            return value;
        }

        return System.getenv(name);
    }

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

    public static ChatParticipant generateParticipant(String id, String displayName) {
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
    public void createChatClient() {
        final CreateChatThreadOptions threadRequest = createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        CompletableFuture<CreateChatThreadResult> completableFuture
            = this.client.createChatThread(threadRequest);

        assertNotNull(completableFuture);
    }

}
