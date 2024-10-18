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
