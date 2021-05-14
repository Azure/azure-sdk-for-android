// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.signaling;

import com.azure.android.communication.common.CommunicationCloudEnvironment;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TrouterUtilsTest {
    private static final String USER_ID = "user_id";

    @Test
    public void canParseTeamsPublicUserRawId() {
        final String teamsPublicUserRawId = "8:orgid:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsPublicUserRawId);

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
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsDodUserRawId);

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
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsGcchUserRawId);

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
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsVisitorUserRawId);

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
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(acsUserRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(acsUserRawId, communicationUserIdentifier.getId());
    }

    @Test
    public void canParseSpoolUserRawId() {
        final String spoolUserRawId = "8:spool:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(spoolUserRawId);

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

        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(phoneNumberRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof PhoneNumberIdentifier);
        PhoneNumberIdentifier phoneNumberIdentifier = (PhoneNumberIdentifier) communicationIdentifier;
        assertEquals(phoneNumber, phoneNumberIdentifier.getPhoneNumber());
        assertEquals(phoneNumberRawId, phoneNumberIdentifier.getRawId());
    }

    @Test
    public void fallbackToUnknownIdentifierForUnknownRawId() {
        final String unknownRawId = "unknown_raw_id";

        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(unknownRawId);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof UnknownIdentifier);
        UnknownIdentifier unknownIdentifier = (UnknownIdentifier) communicationIdentifier;
        assertEquals(unknownRawId, unknownIdentifier.getId());
    }
}
