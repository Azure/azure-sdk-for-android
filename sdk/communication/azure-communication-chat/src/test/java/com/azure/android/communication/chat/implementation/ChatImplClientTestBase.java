// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation;

import com.azure.android.communication.chat.implementation.models.ChatParticipant;
import com.azure.android.communication.chat.implementation.models.CommunicationIdentifierModel;
import com.azure.android.communication.chat.implementation.models.CommunicationUserIdentifierModel;
import com.azure.android.communication.chat.implementation.models.CreateChatThreadOptions;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpRequest;
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
import java.util.List;
import java.util.Locale;

public class ChatImplClientTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String ENDPOINT;
    protected static final String ACCESS_KEY;
    protected static final String THREAD_MEMBER_1;
    protected static final String THREAD_MEMBER_2;

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

    @Override
    protected URL getUnitTestOutDir() {
        return this.getClass().getResource(".");
    }

    public AzureCommunicationChatServiceImplBuilder getChatClientBuilder(HttpClient httpClient) {
        AzureCommunicationChatServiceImplBuilder builder = new AzureCommunicationChatServiceImplBuilder();

        builder
            .endpoint(ENDPOINT)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.addPolicy(chain -> {
                HttpRequest request = chain.getRequest();
                request.getHeaders().put("Authorization", "Bearer " + generateRawToken());
                chain.processNextPolicy(request);
            });
            return builder;
        } else {
            builder.addPolicy(chain -> {
                HttpRequest request = chain.getRequest();
                request.getHeaders().put("Authorization", "Bearer " + ACCESS_KEY);
                chain.processNextPolicy(request);
            });
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

    private static ChatParticipant generateParticipant(String id, String displayName) {
        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setCommunicationIdentifier(new CommunicationIdentifierModel()
            .setCommunicationUser(new CommunicationUserIdentifierModel().setId(id)));
        chatParticipant.setDisplayName(displayName);

        return chatParticipant;
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(ChatImplClientTestBase.class);
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