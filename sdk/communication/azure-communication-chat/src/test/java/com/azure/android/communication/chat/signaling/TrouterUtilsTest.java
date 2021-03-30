// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling;

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
    public void canParseTeamsPublicUserMri() {
        final String teamsPublicUserMri = "8:orgid:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsPublicUserMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.PUBLIC, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertEquals(false, microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsPublicUserMri, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseTeamsDodUserMri() {
        final String teamsDodUserMri = "8:dod:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsDodUserMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.DOD, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertFalse(microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsDodUserMri, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseTeamsGcchUserMri() {
        final String teamsGcchUserMri = "8:gcch:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsGcchUserMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.GCCH, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertFalse(microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsGcchUserMri, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseTeamsVisitorUserMri() {
        final String teamsVisitorUserMri = "8:teamsvisitor:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(teamsVisitorUserMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof MicrosoftTeamsUserIdentifier);
        MicrosoftTeamsUserIdentifier microsoftTeamsUserIdentifier =
            (MicrosoftTeamsUserIdentifier) communicationIdentifier;
        assertEquals(CommunicationCloudEnvironment.PUBLIC, microsoftTeamsUserIdentifier.getCloudEnvironment());
        assertTrue(microsoftTeamsUserIdentifier.isAnonymous());
        assertEquals(USER_ID, microsoftTeamsUserIdentifier.getUserId());
        assertEquals(teamsVisitorUserMri, microsoftTeamsUserIdentifier.getRawId());
    }

    @Test
    public void canParseAcsUserMri() {
        final String acsUserMri = "8:acs:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(acsUserMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(acsUserMri, communicationUserIdentifier.getId());
    }

    @Test
    public void canParseSpoolUserMri() {
        final String spoolUserMri = "8:spool:" + USER_ID;
        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(spoolUserMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof CommunicationUserIdentifier);
        CommunicationUserIdentifier communicationUserIdentifier =
            (CommunicationUserIdentifier) communicationIdentifier;
        assertEquals(spoolUserMri, communicationUserIdentifier.getId());
    }

    @Test
    public void canParsePhoneNumberMri() {
        final String phoneNumber = "+1234567890";
        final String phoneNumberMri = "4:" + phoneNumber;

        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(phoneNumberMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof PhoneNumberIdentifier);
        PhoneNumberIdentifier phoneNumberIdentifier = (PhoneNumberIdentifier) communicationIdentifier;
        assertEquals(phoneNumber, phoneNumberIdentifier.getPhoneNumber());
        assertEquals(phoneNumberMri, phoneNumberIdentifier.getRawId());
    }

    @Test
    public void fallbackToUnknownIdentifierForUnknownMri() {
        final String unknownMri = "unknown_mri";

        CommunicationIdentifier communicationIdentifier = TrouterUtils.getCommunicationIdentifier(unknownMri);

        assertNotNull(communicationIdentifier);
        assertTrue(communicationIdentifier instanceof UnknownIdentifier);
        UnknownIdentifier unknownIdentifier = (UnknownIdentifier) communicationIdentifier;
        assertEquals(unknownMri, unknownIdentifier.getId());
    }
}
