// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.UpdateChatMessageOptions;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.test.TestBase;
import com.azure.android.core.test.TestMode;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ChatClientTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String ENDPOINT;
    protected static final String ACCESS_KEY;
    protected static final String THREAD_PARTICIPANT_1;
    protected static final String THREAD_PARTICIPANT_2;

    static {
        final String endpoint = getConfig("COMMUNICATION_SERVICE_ENDPOINT");
        ENDPOINT = endpoint != null ? endpoint : "https://playback.chat.azurefd.net";

        final String accessKey = getConfig("COMMUNICATION_SERVICE_ACCESS_KEY");
        ACCESS_KEY = accessKey != null ? accessKey : "pw==";

        final String threadParticipant1 = getConfig("COMMUNICATION_CHAT_THREAD_MEMBER_1");
        THREAD_PARTICIPANT_1 = threadParticipant1 != null ? threadParticipant1 : "1:acs:00000000-0000-0000-0000-000000000000_00000000-0000-0000-0000-000000000001";

        final String threadParticipant2 = getConfig("COMMUNICATION_CHAT_THREAD_MEMBER_2");
        THREAD_PARTICIPANT_2 = threadParticipant2 != null ? threadParticipant2 : "2:acs:00000000-0000-0000-0000-000000000000_00000000-0000-0000-0000-000000000002";
    }

    @Override
    protected URL getUnitTestOutDir() {
        return this.getClass().getResource(".");
    }

    public ChatClientBuilder getChatClientBuilder(HttpClient httpClient) {
        ChatClientBuilder builder = new ChatClientBuilder();

        builder
            .endpoint(ENDPOINT)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new CommunicationTokenCredential(generateRawToken()));
            return builder;
        } else {
            builder.credential(new CommunicationTokenCredential(ACCESS_KEY));
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    // TODO: anuchan: remove this and make the TestBase::getConfig protected
    protected static String getConfig(String name) {
        String value = System.getProperty(name);

        if (value != null) {
            return value;
        }

        return System.getenv(name);
    }

    public String generateRawToken() {
        String id = "communication:resourceId.userIdentity";
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("id", id);
        LocalDateTime expiresOnTimestamp = LocalDateTime.now().plusSeconds(100);
        ZonedDateTime ldtUTC = expiresOnTimestamp.atZone(ZoneId.of("UTC"));
        long expSeconds = ldtUTC.toInstant().toEpochMilli() / 1000;
        builder.claim("exp", expSeconds);

        JWTClaimsSet claims =  builder.build();
        JWT idToken = new PlainJWT(claims);
        return idToken.serialize();
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

    public static Iterable<ChatParticipant> addParticipants(String userId1, String userId2) {
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();
        participants.add(generateParticipant(
            userId1,
            "Added Tester 1"));
        participants.add(generateParticipant(
            userId2,
            "Added Tester 2"));

        return participants;
    }

    public static SendChatMessageOptions sendMessageOptions() {
        SendChatMessageOptions options = new SendChatMessageOptions();
        options.setContent("Content");
        options.setSenderDisplayName("Tester");
        options.setType(ChatMessageType.TEXT);
        options.setMetadata(new HashMap<String, String>() {
            {
                put("tags", "tags value");
                put("deliveryMode", "deliveryMode value");
                put("onedriveReferences", "onedriveReferences");
                put("amsreferences", "[\\\"test url file 3\\\"]");
                put("key", "value key");
            }
        });

        return options;
    }

    public static UpdateChatMessageOptions updateMessageOptions() {
        UpdateChatMessageOptions options = new UpdateChatMessageOptions();
        options.setContent("Update Test");
        options.setMetadata(new HashMap<String, String>() {
            {
                put("tags", "");
                put("deliveryMode", "deliveryMode value - updated");
                put("onedriveReferences", "onedriveReferences - updated");
                put("amsreferences", "[\\\"test url file 3\\\"]");
            }
        });

        return options;
    }

    public static ChatParticipant generateParticipant(String id, String displayName) {
        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setCommunicationIdentifier(new CommunicationUserIdentifier(id));
        chatParticipant.setDisplayName(displayName);

        return chatParticipant;
    }

    public boolean checkParticipantsListContainsParticipantId(List<ChatParticipant> participantList,
                                                              String participantId) {
        for (ChatParticipant participant: participantList) {
            if (((CommunicationUserIdentifier) participant.getCommunicationIdentifier())
                .getId().equals(participantId)) {
                return true;
            }
        }
        return false;
    }

    static void awaitOnLatch(CountDownLatch latch, String method) {
        long timeoutInSec = 2;
        try {
            latch.await(timeoutInSec, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't complete within " + timeoutInSec + " minutes");
        }
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(ChatClientTestBase.class);
        String azureTestMode = getConfig("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            System.out.println("azureTestMode: " + azureTestMode);
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        } else {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
            return TestMode.PLAYBACK;
        }
    }
}