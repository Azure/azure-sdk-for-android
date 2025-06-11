// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommunicationIdentifierTests {

    private final String microsoftTeamsAppId = "45ab2481-1c1c-4005-be24-0ffb879b1130";

    @Test
    public void exceptionThrownForNullOrEmptyParameters() {
        assertThrows(IllegalArgumentException.class, () -> new CommunicationUserIdentifier(""));
        assertThrows(IllegalArgumentException.class, () -> new MicrosoftTeamsUserIdentifier(""));
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumberIdentifier(""));
        assertThrows(IllegalArgumentException.class, () -> new MicrosoftTeamsAppIdentifier(""));
        assertThrows(IllegalArgumentException.class, () -> new TeamsExtensionUserIdentifier("", "", ""));

        assertThrows(IllegalArgumentException.class, () -> new CommunicationUserIdentifier(null));
        assertThrows(IllegalArgumentException.class, () -> new MicrosoftTeamsUserIdentifier(null));
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumberIdentifier(null));
        assertThrows(IllegalArgumentException.class, () -> new MicrosoftTeamsAppIdentifier(null));
        assertThrows(IllegalArgumentException.class, () -> new TeamsExtensionUserIdentifier(null, "b", "c"));
        assertThrows(IllegalArgumentException.class, () -> new TeamsExtensionUserIdentifier("a", null, "c"));
        assertThrows(IllegalArgumentException.class, () -> new TeamsExtensionUserIdentifier("a", "b", null));
    }

    @Test
    public void defaultCloudIsPublicForAllIdentifiers() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true).getCloudEnvironment());
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftTeamsAppIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130").getCloudEnvironment());
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new TeamsExtensionUserIdentifier("207ffef6-9444-41fb-92ab-20eacaae2768", "45ab2481-1c1c-4005-be24-0ffb879b1130", "bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd").getCloudEnvironment());
    }

    @Test
    public void microsoftTeamsAppIdentifier_throwsMicrosoftTeamsAppIdNullOrEmpty() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new MicrosoftTeamsAppIdentifier(null));
        assertEquals("The initialization parameter [appId] cannot be null or empty.", illegalArgumentException.getMessage());

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new MicrosoftTeamsAppIdentifier(""));
        assertEquals("The initialization parameter [appId] cannot be null or empty.", illegalArgumentException.getMessage());
    }

    @Test
    public void microsoftTeamsAppIdentifier_cloudEnvironmentIsConsideredInEqualityCheck() {
        assertEquals(new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId),
            new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId));

        assertNotEquals(new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.PUBLIC),
            new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.GCCH));
        assertNotEquals(new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.PUBLIC),
            new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.DOD));
        assertNotEquals(new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.GCCH),
            new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.DOD));
    }

    @Test
    public void microsoftTeamsUserIdentifier_rawIdTakesPrecedenceInEqualityCheck() {
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true),
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true));
        assertNotEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true)
                .setRawId("Raw Id"),
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true)
                .setRawId("Another Raw Id"));
        assertEquals(new MicrosoftTeamsUserIdentifier("override", true)
                .setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"),
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true),
            new MicrosoftTeamsUserIdentifier("override", true).setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH).setRawId("8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH).setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD).setRawId("8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD).setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setRawId("8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true)
                .setRawId("test raw id")
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH).setRawId("test raw id"));
    }

    @Test
    public void TeamsExtensionIdentifier_rawIdTakesPrecedenceInEqualityCheck() {
        // Public Cloud
        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));

        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37"));

        assertNotEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC)
                .setRawId("Raw Id"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC)
                .setRawId("AnotherRaw Id"));

        assertEquals( new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.PUBLIC)
                .setRawId("8:acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));

        assertEquals( new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC)
                .setRawId("8:acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37"));

        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.PUBLIC),
            new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.PUBLIC)
                .setRawId("8:acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"));

        // GCCH
        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.GCCH),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.GCCH));

        assertNotEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.GCCH),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.PUBLIC));

        assertNotEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
                .setRawId("Raw Id"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.GCCH)
                .setRawId("AnotherRaw Id"));

        assertEquals( new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.GCCH)
                .setRawId("8:gcch-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.GCCH));

        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.GCCH)
                .setRawId("8:gcch-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"));

        // DOD
        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD));

        assertNotEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.PUBLIC));

        assertNotEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.GCCH));

        assertNotEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD)
                .setRawId("Raw Id"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD)
                .setRawId("AnotherRaw Id"));

        assertEquals( new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD)
                .setRawId("8:dod-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"),
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment( CommunicationCloudEnvironment.DOD));

        assertEquals( new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new TeamsExtensionUserIdentifier("override", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d",
                "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.DOD)
                .setRawId("8:dod-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130"));
    }

    @Test
    public void phoneNumberIdentifier_rawIdTakesPrecedenceInEqualityCheck() {
        assertEquals(new PhoneNumberIdentifier("+14255550123"), new PhoneNumberIdentifier("+14255550123"));
        assertNotEquals(new PhoneNumberIdentifier("+14255550123").setRawId("Raw Id"),
            new PhoneNumberIdentifier("+14255550123").setRawId("Another Raw Id"));

        assertEquals(new PhoneNumberIdentifier("+override").setRawId("4:14255550123"),
            new PhoneNumberIdentifier("14255550123"));
        assertEquals(new PhoneNumberIdentifier("+14255550123"),
            new PhoneNumberIdentifier("+override").setRawId("4:+14255550123"));
    }

    @Test
    public void phoneNumberIdentifier_getParametersCheck() {
        assertFalse(new PhoneNumberIdentifier("14255550123").isAnonymous());
        assertTrue(new PhoneNumberIdentifier("anonymous").isAnonymous());
        assertTrue(new PhoneNumberIdentifier("14255550123").setRawId("4:anonymous").isAnonymous());
        assertFalse(new PhoneNumberIdentifier("anonymous").setRawId("14255550123").isAnonymous());

        assertNull(new PhoneNumberIdentifier("14255550121").getAssertedId());
        assertNull(new PhoneNumberIdentifier("14255550121.123").getAssertedId());
        assertNull(new PhoneNumberIdentifier("14255550121-123").getAssertedId());
        assertEquals("123", new PhoneNumberIdentifier("14255550121_123").getAssertedId());
        assertEquals("456", new PhoneNumberIdentifier("14255550121_123_456").getAssertedId());

        // Ensure getAssertedId() returns the same value on repeated calls
        PhoneNumberIdentifier staticPhoneNumberIdentifier = new PhoneNumberIdentifier("14255550121_123");
        assertEquals("123", staticPhoneNumberIdentifier.getAssertedId());
        assertEquals("123", staticPhoneNumberIdentifier.getAssertedId());
    }

    @Test
    public void teamsExtensionUserIdentifier_getParametersCheck()
    {
        String userId = "45ab2481-1c1c-4005-be24-0ffb879b1130";
        String tenantId= "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d";
        String resourceId= "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37";

        TeamsExtensionUserIdentifier identifier = new TeamsExtensionUserIdentifier(userId, tenantId, resourceId);

        assertEquals(identifier.getResourceId(), resourceId);
        assertEquals(identifier.getTenantId(), tenantId);
        assertEquals(identifier.getUserId(), userId);
        assertEquals(identifier.getCloudEnvironment(), CommunicationCloudEnvironment.PUBLIC);
    }

    @Test
    public void teamsExtensionUserIdentifier_SetCloudEnvironment_NullValue_SetToPublic()
    {
        String userId = "45ab2481-1c1c-4005-be24-0ffb879b1130";
        String tenantId= "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d";
        String resourceId= "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37";

        TeamsExtensionUserIdentifier identifier = new TeamsExtensionUserIdentifier(userId, tenantId, resourceId).setCloudEnvironment(null);

        assertEquals(identifier.getCloudEnvironment(), CommunicationCloudEnvironment.PUBLIC);
    }

    @Test
    public void teamsExtensionUserIdentifier_getParametersCheck_DOD()
    {
        String userId = "45ab2481-1c1c-4005-be24-0ffb879b1130";
        String tenantId= "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d";
        String resourceId= "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37";
        CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.DOD;

        TeamsExtensionUserIdentifier identifier = new TeamsExtensionUserIdentifier(userId, tenantId, resourceId).setCloudEnvironment(cloudEnvironment);

        assertEquals(identifier.getResourceId(), resourceId);
        assertEquals(identifier.getTenantId(), tenantId);
        assertEquals(identifier.getUserId(), userId);
        assertEquals(identifier.getCloudEnvironment(), cloudEnvironment);
    }

    @Test
    public void teamsExtensionUserIdentifier_getParametersCheck_GCCH()
    {
        String userId = "45ab2481-1c1c-4005-be24-0ffb879b1130";
        String tenantId= "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d";
        String resourceId= "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37";
        CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.GCCH;

        TeamsExtensionUserIdentifier identifier = new TeamsExtensionUserIdentifier(userId, tenantId, resourceId).setCloudEnvironment(cloudEnvironment);

        assertEquals(identifier.getResourceId(), resourceId);
        assertEquals(identifier.getTenantId(), tenantId);
        assertEquals(identifier.getUserId(), userId);
        assertEquals(identifier.getCloudEnvironment(), cloudEnvironment);
    }

    @Test
    public void getRawIdOfIdentifier()
    {
        assertRawId(new CommunicationUserIdentifier("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new CommunicationUserIdentifier("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new CommunicationUserIdentifier("someFutureFormat"), "someFutureFormat");

        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130").setCloudEnvironment(CommunicationCloudEnvironment.DOD), "8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130").setCloudEnvironment(CommunicationCloudEnvironment.GCCH), "8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false), "8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true), "8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true).setRawId("8:orgid:legacyFormat"), "8:orgid:legacyFormat");

        assertRawId(new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d", "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC),
            "8:acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d", "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            "8:dod-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d", "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            "8:gcch-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130");

        assertRawId(new PhoneNumberIdentifier("+112345556789"), "4:+112345556789");
        assertRawId(new PhoneNumberIdentifier("112345556789"), "4:112345556789");
        assertRawId(new PhoneNumberIdentifier("+112345556789").setRawId("4:otherFormat"), "4:otherFormat");
        assertRawId(new PhoneNumberIdentifier("otherFormat").setRawId("4:207ffef6-9444-41fb-92ab-20eacaae2768"), "4:207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRawId(new PhoneNumberIdentifier("otherFormat").setRawId("4:207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768"), "4:207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRawId(new PhoneNumberIdentifier("otherFormat").setRawId("4:+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768"), "4:+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768");

        assertRawId(new UnknownIdentifier("48:45ab2481-1c1c-4005-be24-0ffb879b1130"), "48:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new UnknownIdentifier("someFutureFormat"), "someFutureFormat");
    }

    @Test
    public void createIdentifierFromRawId()
    {
        assertIdentifier("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:spool:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:spool:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:acs:something", new CommunicationUserIdentifier("8:acs:something"));

        assertIdentifier("8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", false).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", false).setCloudEnvironment(CommunicationCloudEnvironment.DOD));
        assertIdentifier("8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH));
        assertIdentifier("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("8:orgid:legacyFormat", new MicrosoftTeamsUserIdentifier("legacyFormat", false).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));

        assertIdentifier("8:acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130",
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d", "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("8:dod-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130",
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d", "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.DOD));
        assertIdentifier("8:gcch-acs:3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37_7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d_45ab2481-1c1c-4005-be24-0ffb879b1130",
            new TeamsExtensionUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", "7f43a902-8c6f-4b77-91e1-67d3f3f4bb1d", "3e74a9c2-5d0b-4f88-936c-7ea2b21d4b37").setCloudEnvironment(CommunicationCloudEnvironment.GCCH));

        assertIdentifier("4:112345556789", new PhoneNumberIdentifier("112345556789"));
        assertIdentifier("4:+112345556789", new PhoneNumberIdentifier("+112345556789"));
        assertIdentifier("4:otherFormat", new PhoneNumberIdentifier("otherFormat"));
        assertIdentifier("4:207ffef6-9444-41fb-92ab-20eacaae2768", new PhoneNumberIdentifier("207ffef6-9444-41fb-92ab-20eacaae2768"));
        assertIdentifier("4:207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768", new PhoneNumberIdentifier("207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768"));
        assertIdentifier("4:+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768", new PhoneNumberIdentifier("+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768"));

        assertIdentifier("28:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId));
        assertIdentifier("28:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("28:dod:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.DOD));
        assertIdentifier("28:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsAppIdentifier(microsoftTeamsAppId, CommunicationCloudEnvironment.GCCH));

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> CommunicationIdentifier.fromRawId(null));
        assertEquals("The parameter [rawId] cannot be null or empty.", illegalArgumentException.getMessage());
    }

    @Test
    public void createIdentifierFromRawId_unknownIdentifierForInvalidMRI() {
        String [] invalidRawIDs = {
            "48:45ab2481-1c1c-4005-be24-0ffb879b1130",
            "28:45ab2481-1c1c-4005-be24-0ffb879b1130:newFormat",
            "28:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130:newFormat:with more segments",
            "28:gcch:abc-global:45ab2481-1c1c-4005-be24-0ffb879b1130",
            "28:gcch-global:abc-global:45ab2481-1c1c-4005-be24-0ffb879b1130",
            "8:gcch-acs:abc:def:1234",
            "8:acs::1:2:3:4:5:6:7:8:9",
            "8:dod-acs::1:2:3",
            "8:spool:: other format: :123: 90",
            "8:orgid:abc:def:1234"
        };
        for (String invalidRawID: invalidRawIDs) {
            assertIdentifier(invalidRawID, new UnknownIdentifier(invalidRawID));
        }
    }


    @Test
    public void rawIdStaysTheSameAfterConversionToIdentifierAndBack()
    {
        assertRoundTrip("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:spool:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:acs:something");
        assertRoundTrip("8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:orgid:legacyFormat");
        assertRoundTrip("4:+112345556789");
        assertRoundTrip("4:112345556789");
        assertRoundTrip("4:otherFormat");
        assertRoundTrip("4:207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRoundTrip("4:207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRoundTrip("4:+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRoundTrip("28:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("28:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("28:dod:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("28:dod-global:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("28:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("28:gcch-global:45ab2481-1c1c-4005-be24-0ffb879b1130");
    }

    private void assertRawId(CommunicationIdentifier identifier, String expectedRawId)  {
        assertEquals(identifier.getRawId(), expectedRawId);
    }

    private void assertIdentifier(String rawId, CommunicationIdentifier expectedIdentifier)
    {
        assertEquals(CommunicationIdentifier.fromRawId(rawId), expectedIdentifier);
        assertEquals(CommunicationIdentifier.fromRawId(rawId).hashCode(), expectedIdentifier.hashCode());
    }

    private void assertRoundTrip(String rawId)
    {
        assertEquals(CommunicationIdentifier.fromRawId(rawId).getRawId(), rawId);
    }
}
