// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notification;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.chat.implementation.notifications.NotificationUtils.CloudType;
import com.azure.android.communication.common.CommunicationCloudEnvironment;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;
import com.azure.android.core.util.Base64Util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class NotificationUtilsTest {
    private static final String USER_ID = "user_id";
    private static final String VALID_ENCRYPTED_PUSH_NOTIFICATION_PAYLOAD = "cHypjvRkIsqImAf98fJcBxRjpwL69uB6jObZc" +
        "ohMkgS0/VKs96/YuesHi6Hzz/AJ7eHHW3YQLeAJZKUFoTGUij7i/4m7fsT8C/GN/ZaXPe9SmqtUJXWC97TlOU+OtimNhy+BzWEwOC4aXq" +
        "GU79TIznZ9x11Ri0KRG6xrCddVbFbHyKeMiXAtAKPl2EAQ9sQzp24CHvHu+yvpWfiGl15KS2KKfP78nV6JO7Oe7FbMfnO3iI7PpojvGHM" +
        "T61XNvsB+vB7f+AgxdzklKZzOTfgtamoy6r2odYYygyKZIfpON/aYGx+L3VY/0bXyKfE4A7HMH/jUCmL0KiGw2OPnOZK7/8oCjvgaT+hL" +
        "yikJfmFG06yawrsAi7I1s+2Yx7vZf4ZIKviB1QY0RWXUJa22b5QsNg5JcQPsLIEZic+ytldikP1QfUtyE9Huk9Vzr0yQ2MPOR28nu2bV6" +
        "pvQ7yn30GgqGQP/b4pL6Vq3q3qJQ6vQCaGqaXfQaxKieT4K3kC9/VYFsYDLunObSwkgmLrFg/PpuUmaqx25WnzJD/fd0eAjQb8EaqKN2n" +
        "U6UnXtEL/hr8+MnExCSfV7dh9du2xM7t16/KppetJO0xDlR0EZG1kD/Nq9dnzfb5wdPVLNQ9hQqrgH/xUL2wYh5CMpwJsOSfQLqbJUlWc" +
        "EhZ8MfDY38HLOVdXp9aqf+CRthHjqiRVWhyHSOLRLHWItHaYrJG6mr8EOms5eWOpXKYdfC4PyobPLpT86oQSCLaKmv9AqaisML6ZYrgQv" +
        "itcPnf9R5S5bsG3OMubAGkFvhT1N8YHvl9fjPmrkrDHZj3jBugVc2OUNb68j+Vv+FVhRfL1HOpyYgnYB8eQybiPEUsFeeOm1Ho0ROXU59" +
        "fcRhJZL0NKNZ7oQ0j0ZkNT5jt6zdDKJeuLjUNJxovN5HzKYigaSrii9yJAnlKYZ7gNCHTCfTVhuMsHf3Qco/U5jJ+POSooJOOPtQYQE4a" +
        "jYCs8hu+IwQqJrwJ/hJzxF";
    private static final String VALID_DECRYPTED_PUSH_NOTIFICATION_PAYLOAD = "{" +
        "\"senderId\": \"8:acs:188d4cea-0a9b-4840-8fdc-7a5c71fe9bd0_0000000d-58e9-9adb-b0b7-3a3a0d0021b2\"," +
        "\"recipientId\": \"8:acs:188d4cea-0a9b-4840-8fdc-7a5c71fe9bd0_0000000d-58e9-9adb-b0b7-3a3a0d0021b2\"," +
        "\"transactionId\": \"9E3//s5sCEyPMTUeGqWQIg.1.1.1.1.1316030596.1.0\"," +
        "\"groupId\": \"19:47b098d56d084eb4a790726c93ac03a0@thread.v2\"," +
        "\"messageId\": \"1636001350674\",\"collapseId\":\"W3wRKlXaL6ZKuFkxHnaRL3Kk+BTEAjAoSndseSpL5dg=\"," +
        "\"messageType\": \"Text\",\"messageBody\": \"Test message 1\",\"senderDisplayName\": \"First participant\"," +
        "\"clientMessageId\": \"\",\"originalArrivalTime\": \"2021-11-04T04:49:10.674Z\",\"priority\": \"\"," +
        "\"version\": \"1636001350674\"," +
        "\"acsChatMessageMetadata\": \"{\\\"deliveryMode\\\":\\\"deliveryMode value\\\",\\\"tags\\\":\\\"tag1\\\"}\"}";

    @Test
    public void canParseMessageMetadata() {
        final String messageMetadata = "{\"deliveryMode\":\"deliveryMode value\",\"tags\":\"tag1\"}";
        Map<String,String> metadataMap = NotificationUtils.parseChatMessageMetadata(messageMetadata);

        assertNotNull(metadataMap);
        assertEquals(2, metadataMap.size());
        assertEquals(metadataMap.get("deliveryMode"), "deliveryMode value");
        assertEquals(metadataMap.get("tags"), "tag1");
    }

    @Test
    public void canParseTokenForRegion() {
        final String usaSkypeToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjY5RUU4QzJFNjUwNzI1OUJBMEYxREQ3MDU0Njk0QjdDQTI4QkQxQkYiLCJ4NXQiOiJhZTZNTG1VSEpadWc4ZDF3VkdsTGZLS0wwYjgiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjE4OGQ0Y2VhLTBhOWItNDg0MC04ZmRjLTdhNWM3MWZlOWJkMF8wMDAwMDAxYS1kZDM2LTE0OTktNzdkNC0wMjQ4MjIwMDA1YmUiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2NzM3NjYiLCJleHAiOjE2OTU3NjAxNjYsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQiLCJyZXNvdXJjZUlkIjoiMTg4ZDRjZWEtMGE5Yi00ODQwLThmZGMtN2E1YzcxZmU5YmQwIiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTY5NTY3Mzc2Nn0.OmjphUp-TEze3R4a1mo0puUdn7bYw-rFWheodWoe5H-uwKQkTQ_9BzreUJ4qefrXinNlgyVz7DdHAp-_fyh5EQyN6CTwpZdvE4qqmFexk7LJC53GGtGPJVRM_JIXN6vKMDFFrR6wWqwZ22Urr0LpvB2xQRQJJmd9uSOj5TfilJOZrgGTGC0LuKTGLC6kEsAtbDod7zzclZWiApnsG7hgClx86C3fa7-WxLrR_gvqD77HUQOZoNu13uAAVkpFpC1Dk78IhuJfu_m_yEJK0D0z80y0yWN-WE0WLjfnaJNCk8aFbfJdEmvRBQP_mBHBKMVoRDwVYkvVGcXdDYnankYgCw";
        assertEquals("unitedstates", NotificationUtils.decodeResourceLocationFromJwtToken(usaSkypeToken));

        final String frenchToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjRiZGMwZDhmLWRhMTgtNDkyYy1iMmIyLWYwYTI0ZWI1YTFkZF8wMDAwMDAxYi02ZWQyLTc3OWItODI2ZC05ZjQ4MjIwMGU4YTEiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2ODkwMDMiLCJleHAiOjE2OTU3NzU0MDMsInJnbiI6ImZyIiwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6IjRiZGMwZDhmLWRhMTgtNDkyYy1iMmIyLWYwYTI0ZWI1YTFkZCIsInJlc291cmNlTG9jYXRpb24iOiJmcmFuY2UiLCJpYXQiOjE2OTU2ODkwMDN9.IGb4wD24W8inEp7UpDTEuI91-IaNjIrrHIMGbS238JKJTTGO9A8vFcg8hErkTDcZiREq5i0PdkIlqkgk5_fiVEGpj2mHLJPN7Yer6Mw_muy3zVgUVZRjwI3OS7dn8qRBD6J-p6UDL5yKdRPvmwc4eqDTnnYRx1JcYyzdKJGBpYP90yvFShnShf3iDsBFBhpUEN01Oi-LrmbhfPO-y8WudAaTOfZU9nUo8rI5cA2LWoF9dVaQb3Jhf5obNJUs6t9OTXiArSb1QIn2_Ezv8rmQSxFGk2mgAZUrUrWKvSBqUfS-dtzAcWhtibr-rc3otRpMP3Lmo9p7mtXhhFp98f8PMQ";
        assertEquals("france", NotificationUtils.decodeResourceLocationFromJwtToken(frenchToken));

        final String germanToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjUxN2M1Yzg1LTBlYzItNDUzZS1hZTk2LTc3ZGJlMDkzYWUwZF8wMDAwMDAxYi02ZWY0LTcyM2MtYmY2OC05ZjQ4MjIwMGU4YjMiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2OTEyMzAiLCJleHAiOjE2OTU3Nzc2MzAsInJnbiI6ImRlIiwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6IjUxN2M1Yzg1LTBlYzItNDUzZS1hZTk2LTc3ZGJlMDkzYWUwZCIsInJlc291cmNlTG9jYXRpb24iOiJnZXJtYW55IiwiaWF0IjoxNjk1NjkxMjMwfQ.YSgCpbwh9DkP5kZ4GepkRBEuarc2CYzXGvvbw7HkeGjW6y5anFy4Zx0kYebS7w7zYYGwzlmyagxbHzm7Ejtr9VmJYS82JtC9UjNlWwNBKuHeq3ek8G284hSWRorz_v-hYiNyLVtwGSnPHUmWl4Rn6w7dI7Poqfzi6IfdyB9Ood5EIomFvxWuDa822WDMmBeiSLoD4AAE_cTMNK73j42uQo_bsX23fdlZYXpHJq5Q3CUEu-XQgVtqvPGDMb6Ej8blCyC1EKH6rFWaCpqWT5BZbjfU5pTAQMP0qL7XOlohxXgf2_9XIyAATuQs1_hpGvAvXSYYcNvEj0-Dyu_ERTsP9g";
        assertEquals("germany", NotificationUtils.decodeResourceLocationFromJwtToken(germanToken));

        final String norwayToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjQ1NGY2ZGQ1LTc3MTQtNDhmOC1hNDJlLWY3MTViMjI5MmNjMV8wMDAwMDAxYi02ZWY1LTQ2YzgtMTM2ZC05ZjQ4MjIwMGQ5ZTIiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2OTEyODUiLCJleHAiOjE2OTU3Nzc2ODUsInJnbiI6Im5vIiwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6IjQ1NGY2ZGQ1LTc3MTQtNDhmOC1hNDJlLWY3MTViMjI5MmNjMSIsInJlc291cmNlTG9jYXRpb24iOiJub3J3YXkiLCJpYXQiOjE2OTU2OTEyODV9.Y3AW-d2PSYhm1762u1WtgtLLuFVEUHVT1TxthtqEaVOZy8rpnNtD86lVHBrSVPleeF4jQkZbfhjBlMXSpv56RpBvcNbMWvivp6giWdqp96GXG0NChy03JneDUrrNQGLJw-Xp7mTkom10WSO4MMyr1VAqg9wb9QW0oDYpYqJ46qx7hN9AkOYjuK2TpgPIN5xvF65zXsvapo8zdmAbCe4KHvm50f6__co5ji2OCFyMOUiShdQK0mYLf-a55YdFuBWsdRfZFyNj3foevCNZxjZngp33LB6fgxzApdnt0sDtO1NnJxh4-3Fi9qZd0yzailKxGrF4vnoYj26LK25aSd653w";
        assertEquals("norway", NotificationUtils.decodeResourceLocationFromJwtToken(norwayToken));

        final String switzerlandToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjkyMGQzMDZlLTY2MjAtNGRiYy04MjI1LWU2NWM0NTExZTdmNF8wMDAwMDAxYi02ZWY2LTEzMmItZDk5OS1kOTNhMGQwMGRjZTEiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2OTEzMzciLCJleHAiOjE2OTU3Nzc3MzcsInJnbiI6ImNoIiwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6IjkyMGQzMDZlLTY2MjAtNGRiYy04MjI1LWU2NWM0NTExZTdmNCIsInJlc291cmNlTG9jYXRpb24iOiJzd2l0emVybGFuZCIsImlhdCI6MTY5NTY5MTMzN30.FvpD1_3EvdXQRbKl0Iqr8z08BEEWqAOR50rf0TbZ8uiinidgbm92NiTgVIPqrihJ4t9WfQtzaUaCbxXCAGaj5U-vcJTUN7dLE-V0AODQYDtzx79CEcZV57AwQWSgOFlZR6FNNyN00ySAt4N1oerckt9a7nrC_MhfSh8bTfA_XTn2ukDn91HH0WcAzhZR3IeAt0llMXiWXdh6pyGEtZW8TglauyhodVcMWJkHY9KfgI4ynV0yvdtQYW8AJltjC3fqXstV-lftbkdVE3Ay7Vq-tiAbB2FqZtx2hfDqcjS3d16LE6Z-As_u_ej99kAQRX8EYupnAj-LW6nIY7mIxyQYmA";
        assertEquals("switzerland", NotificationUtils.decodeResourceLocationFromJwtToken(switzerlandToken));

        final String swedenToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjY3MDZiMDQxLTdmYzktNDU3OS1hZGZlLWRjNjQ0MmU0YTljYV8wMDAwMDAxYi02ZjAyLWNmOTktZmY2My1kYTNhMGQwMGQzOWYiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2OTIxNzIiLCJleHAiOjE2OTU3Nzg1NzIsInJnbiI6InNlIiwiYWNzU2NvcGUiOiJjaGF0IiwicmVzb3VyY2VJZCI6IjY3MDZiMDQxLTdmYzktNDU3OS1hZGZlLWRjNjQ0MmU0YTljYSIsInJlc291cmNlTG9jYXRpb24iOiJzd2VkZW4iLCJpYXQiOjE2OTU2OTIxNzJ9.RR4V4objIDg9hYwvuQXuNoDnySdNHTlzQTX47u18_intR5L2prZwvHXv9mLUnA_emGwwCwcBdBQzIWbLEWpq161U7dya_ZC6G4wL0MAsFz9gOPeD_odFTeCuloocqMhft0rLSXCqTB5RHAV7ZnbkPs4D1m4bsdxajqqcpOEg2YNYiIpOOJxZKSKkEjqxd9c5bzTsgvcm3g90uTEHSz3PvF-9Ny1Bk8HvXmvCanzd-ox2fHop9Onh0drymvOfmN9QqJU4xGSPuV00ZESADCue-b9nbdiDaTESsUq8kz2AURJuQtLhiEyPa1Qv9B5Kp4MidfCHViEXN1jvV8mK3FYHXQ";
        assertEquals("sweden", NotificationUtils.decodeResourceLocationFromJwtToken(swedenToken));

        final String emeaToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjExM2IzNGI2LTUyNzAtNDVhOS1iODFhLWZiMDE0MzkzMGU2MF8wMDAwMDAxYi02ZWYzLTRiYmEtNDk2Yy05ZjQ4MjIwMGQ0YTUiLCJzY3AiOjE3OTIsImNzaSI6IjE2OTU2OTExNTUiLCJleHAiOjE2OTU3Nzc1NTUsInJnbiI6ImVtZWEiLCJhY3NTY29wZSI6ImNoYXQiLCJyZXNvdXJjZUlkIjoiMTEzYjM0YjYtNTI3MC00NWE5LWI4MWEtZmIwMTQzOTMwZTYwIiwicmVzb3VyY2VMb2NhdGlvbiI6ImV1cm9wZSIsImlhdCI6MTY5NTY5MTE1NX0.NYahai2sbBw6Tu1KwVUGspNCESFM4wCg5xzWVFuLXg3SjKm5mg65LM5jdP0mU9bmDDafdUJv6oi5-VVsFdi50IblHNMs495yhg7dlbSyUa5LSXLEM7_R5Nwt_yltIthgTK6Zbf6WlHhNs6krhPHCFLUuy-K1ZgGA-mcXcgtwQVFIhc27pul59Zk5iYBuP6EZNtoY4anP0kg0cKkCix0Btzv2tv99sMlTy-tHjElCT_KWRiAW9fVdw9oJbYZiFYt9QTNBKAhFEqlh7G05uF3iIER5-OwvnhPh1mrvOgbARFXEQ4A_maoPkbEQKnicPiYwKMvrV6H3PQGvzczSbzIy4w";
        assertEquals("europe", NotificationUtils.decodeResourceLocationFromJwtToken(emeaToken));
    }

    @Test
    public void canParseTeamsPublicUserRawId() {
        final String teamsPublicUserRawId = "8:orgid:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(teamsPublicUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.PUBLIC, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertEquals(false, microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsPublicUserRawId, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseTeamsDodUserRawId() {
        final String teamsDodUserRawId = "8:dod:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(teamsDodUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.DOD, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertFalse(microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsDodUserRawId, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseTeamsGcchUserRawId() {
        final String teamsGcchUserRawId = "8:gcch:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(teamsGcchUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.GCCH, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertFalse(microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsGcchUserRawId, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseTeamsVisitorUserRawId() {
        final String teamsVisitorUserRawId = "8:teamsvisitor:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(teamsVisitorUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.PUBLIC, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertTrue(microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsVisitorUserRawId, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseAcsUserRawId() {
        final String acsUserRawId = "8:acs:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(acsUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(acsUserRawId, communicationUserIdentifier.getId());
    }

    @Test
    public void canParseAcsGcchUserRawId() {
        final String acsUserRawId = "8:gcch-acs:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(acsUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(acsUserRawId, communicationUserIdentifier.getId());
    }

    @Test
    public void canParseAcsDodUserRawId() {
        final String acsUserRawId = "8:dod-acs:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(acsUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(acsUserRawId, communicationUserIdentifier.getId());
    }

    @Test
    public void canParseSpoolUserRawId() {
        final String spoolUserRawId = "8:spool:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(spoolUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(spoolUserRawId, communicationUserIdentifier.getId());
    }

    @Test
    public void canParsePhoneNumberRawId() {
        final String phoneNumber = "+1234567890";
        final String phoneNumberRawId = "4:" + phoneNumber;

        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(phoneNumberRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof PhoneNumberIdentifier);
        PhoneNumberIdentifier phoneNumberIdentifier = (PhoneNumberIdentifier) communicationIdentifier;
        assertEquals(phoneNumber, phoneNumberIdentifier.getPhoneNumber());
        assertEquals(phoneNumberRawId, phoneNumberIdentifier.getRawId());
    }

    @Test
    public void fallbackToUnknownIdentifierForUnknownRawId() {
        final String unknownRawId = "unknown_raw_id";

        CommunicationIdentifier communicationIdentifier = NotificationUtils.getCommunicationIdentifier(unknownRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof UnknownIdentifier);
        UnknownIdentifier unknownIdentifier = (UnknownIdentifier) communicationIdentifier;
        assertEquals(unknownRawId, unknownIdentifier.getId());
    }

    @Test
    public void canResolvePublicSkypeToken() {
        final String spoolSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoic3Bvb2w6bXktcmVzb3VyY2UtaWRfYWJjZGVmLTAxMjM0NTY3ODkifQ.";
        final String acsSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOm15LXJlc291cmNlLWlkX2FiY2RlZi0wMTIzNDU2Nzg5In0.";
        final String teamsSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoib3JnaWQ6bXktcmVzb3VyY2UtaWRfYWJjZGVmLTAxMjM0NTY3ODkifQ.";

        assertEquals(CloudType.Public, NotificationUtils.getUserCloudTypeFromSkypeToken(spoolSkypeToken));
        assertEquals(CloudType.Public, NotificationUtils.getUserCloudTypeFromSkypeToken(acsSkypeToken));
        assertEquals(CloudType.Public, NotificationUtils.getUserCloudTypeFromSkypeToken(teamsSkypeToken));
    }

    @Test
    public void canResolveDodSkypeToken() {
        final String dodSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiZG9kOm15LXJlc291cmNlLWlkX2FiY2RlZi0wMTIzNDU2Nzg5In0.";
        final String dodAcsSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiZG9kLWFjczpteS1yZXNvdXJjZS1pZF9hYmNkZWYtMDEyMzQ1Njc4OSJ9.";

        assertEquals(CloudType.Dod, NotificationUtils.getUserCloudTypeFromSkypeToken(dodSkypeToken));
        assertEquals(CloudType.Dod, NotificationUtils.getUserCloudTypeFromSkypeToken(dodAcsSkypeToken));
    }

    @Test
    public void canResolveGcchSkypeToken() {
        final String gcchSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiZ2NjaDpteS1yZXNvdXJjZS1pZF9hYmNkZWYtMDEyMzQ1Njc4OSJ9.";
        final String gcchAcsSkypeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiZ2NjaC1hY3M6bXktcmVzb3VyY2UtaWRfYWJjZGVmLTAxMjM0NTY3ODkifQ.";

        assertEquals(CloudType.Gcch, NotificationUtils.getUserCloudTypeFromSkypeToken(gcchSkypeToken));
        assertEquals(CloudType.Gcch, NotificationUtils.getUserCloudTypeFromSkypeToken(gcchAcsSkypeToken));
    }

    @Test
    public void canVerifyValidEncryptedPayload() throws Throwable {
        final byte[] authKeyBytes = Base64Util.decodeString("AVj04vV5fTriO33yPz6+ZdrM1rv/n+dNHcKiCA29V1I=");
        final String encryptedPayload = VALID_ENCRYPTED_PUSH_NOTIFICATION_PAYLOAD;

        byte[] encryptedBytes = Base64Util.decodeString(encryptedPayload);
        byte[] f = NotificationUtils.extractEncryptionKey(encryptedBytes);
        byte[] iv = NotificationUtils.extractInitializationVector(encryptedBytes);
        byte[] cipherText = NotificationUtils.extractCipherText(encryptedBytes);
        byte[] hmac = NotificationUtils.extractHmac(encryptedBytes);

        assertTrue(NotificationUtils.verifyEncryptedPayload(f, iv, cipherText, hmac, new SecretKeySpec(authKeyBytes, "AES")));
    }


    @Test
    public void canVerifyInvalidEncryptedPayload() throws Throwable {
        final byte[] authKeyBytes = Base64Util.decodeString("AVj04vV5fTriO33yPz6+ZdrM1rv/n+dNHcKiCA29V1I=");
        final String encryptedPayload = VALID_ENCRYPTED_PUSH_NOTIFICATION_PAYLOAD + "INVALID";

        byte[] encryptedBytes = Base64Util.decodeString(encryptedPayload);
        byte[] f = NotificationUtils.extractEncryptionKey(encryptedBytes);
        byte[] iv = NotificationUtils.extractInitializationVector(encryptedBytes);
        byte[] cipherText = NotificationUtils.extractCipherText(encryptedBytes);
        byte[] hmac = NotificationUtils.extractHmac(encryptedBytes);

        assertFalse(NotificationUtils.verifyEncryptedPayload(f, iv, cipherText, hmac, new SecretKeySpec(authKeyBytes, "AES")));
    }

    @Test
    public void canDecryptEncryptedPayload() throws Throwable {
        final byte[] cryptoKeyBytes = Base64Util.decodeString("hhAWluHcNoBmt18D92+AquAELFLlK7WZEuPHzJ2jc+s=");
        final String encryptedPayload = VALID_ENCRYPTED_PUSH_NOTIFICATION_PAYLOAD;

        byte[] encryptedBytes = Base64Util.decodeString(encryptedPayload);
        byte[] iv = NotificationUtils.extractInitializationVector(encryptedBytes);
        byte[] cipherText = NotificationUtils.extractCipherText(encryptedBytes);

        String decryptedPayload = NotificationUtils.decryptPushNotificationPayload(iv, cipherText, new SecretKeySpec(cryptoKeyBytes, "AES"));
        assertEquals(VALID_DECRYPTED_PUSH_NOTIFICATION_PAYLOAD, decryptedPayload);
    }
}
