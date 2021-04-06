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
import java.util.concurrent.ExecutionException;

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
        ENDPOINT = endpoint != null ? endpoint : "https://chat-sdktester-e2e.communication.azure.com";

        final String accessKey = getConfig("COMMUNICATION_SERVICE_ACCESS_KEY");
        ACCESS_KEY = accessKey != null ? accessKey : "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMiIsIng1dCI6IjNNSnZRYzhrWVNLd1hqbEIySmx6NTRQVzNBYyIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOjM1N2UzOWQyLWEyOWEtNGJmNi04OGNjLWZkYTBhZmMyYzBlZF8wMDAwMDAwOS00N2ZjLTRjNzctZWRiZS1hNDNhMGQwMDNkZGYiLCJzY3AiOjE3OTIsImNzaSI6IjE2MTc3MjgwMzgiLCJpYXQiOjE2MTc3MjgwMzgsImV4cCI6MTYxNzgxNDQzOCwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6IjM1N2UzOWQyLWEyOWEtNGJmNi04OGNjLWZkYTBhZmMyYzBlZCJ9.TvtnkKtU2foL0yvfvShrqoOxcKJ7UeoTGrb99CZSZ6FZeG2jf8wEBUpz4n_j2Im_0Fo7PJXN2Y82PcwwRu17BB6TrgM7OapK4OVK5Jrg27KmgO3SoA1wCqvfBHUaa91LkTLF9Mb4gKY3m8FhY9yk6pW1oSrmCANwjXqDvFhOp3GPpyXDrRZTD3KkqY-U4d9r3IaDA-AbjQmifhVzLSc08uCgEkehI7evCBsI92BcWD3z3-GxXa5U4de9NyK8_4O1YivnL-_YeKG7aD0xGALOPkrGTrEKdRdbH1AE3XPCx1H5kYtEpqTMkciKmUKNS4qs-uryhvvFiTX4Pf3dkn2ZHg";

        final String threadMember1 = getConfig("COMMUNICATION_CHAT_THREAD_MEMBER_1");
        THREAD_MEMBER_1 = threadMember1 != null ? threadMember1 : "8:acs:357e39d2-a29a-4bf6-88cc-fda0afc2c0ed_00000009-47fb-c12e-edbe-a43a0d003dda";

        final String threadMember2 = getConfig("COMMUNICATION_CHAT_THREAD_MEMBER_2");
        THREAD_MEMBER_2 = threadMember2 != null ? threadMember2 : "8:acs:357e39d2-a29a-4bf6-88cc-fda0afc2c0ed_00000009-47fc-4c77-edbe-a43a0d003ddf";
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
