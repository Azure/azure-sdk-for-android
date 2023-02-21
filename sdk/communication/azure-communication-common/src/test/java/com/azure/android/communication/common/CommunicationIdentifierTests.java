// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunicationIdentifierTests {

    private final String userId = "user id";
    private String microsoftBotId = "45ab2481-1c1c-4005-be24-0ffb879b1130";
    private final String fullId = "some lengthy id string";

    @Test
    public void defaultCloudIsPublicForMicrosoftTeamsUserIdentifier() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftTeamsUserIdentifier(userId, true).setRawId(fullId).getCloudEnvironment());
    }

    @Test
    public void microsoftBotIdentifier_throwsMicrosoftBotIdNullOrEmpty() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new MicrosoftBotIdentifier(null));
        assertEquals("The initialization parameter [microsoftBotId] cannot be null or empty.", illegalArgumentException.getMessage());

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new MicrosoftBotIdentifier(""));
        assertEquals("The initialization parameter [microsoftBotId] cannot be null or empty.", illegalArgumentException.getMessage());
    }

    @Test
    public void microsoftBotIdentifier_defaultCloudIsPublic() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftBotIdentifier(userId, true).setRawId(fullId).getCloudEnvironment());

        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftBotIdentifier(userId, false).setRawId(fullId).getCloudEnvironment());

        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftBotIdentifier(userId).setRawId(fullId).getCloudEnvironment());
    }

    @Test
    public void microsoftBotIdentifier_defaultGlobalIsFalse() {
        assertFalse((new MicrosoftBotIdentifier(userId)).isGlobal());
        assertFalse((new MicrosoftBotIdentifier(userId)).setRawId(fullId).isGlobal());
    }

    @Test
    public void microsoftBotIdentifier_rawIdTakesPrecedenceInEqualityCheck() {
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, true),
            new MicrosoftBotIdentifier(microsoftBotId, true));


        String publicGlobalRawId = CommunicationIdentifier.BOT_PREFIX + microsoftBotId;
        assertNotEquals(new MicrosoftBotIdentifier(microsoftBotId, true)
                .setRawId(publicGlobalRawId),
            new MicrosoftBotIdentifier(microsoftBotId, true)
                .setRawId("raw id"));
        assertEquals(new MicrosoftBotIdentifier("override", true)
                .setRawId(publicGlobalRawId),
            new MicrosoftBotIdentifier(microsoftBotId, true));
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, true),
            new MicrosoftBotIdentifier("override", false)
                .setRawId(publicGlobalRawId));

        String gcchGlobalRawId = CommunicationIdentifier.BOT_GCCH_GLOBAL_PREFIX + microsoftBotId;
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, true)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftBotIdentifier("override", true)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
                .setRawId(gcchGlobalRawId));
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, true)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftBotIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
                .setRawId(gcchGlobalRawId));

        String gcchRawId = CommunicationIdentifier.BOT_GCCH_PREFIX + microsoftBotId;
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftBotIdentifier("override", true)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
                .setRawId(gcchRawId));
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftBotIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
                .setRawId(gcchRawId));

        String dodGlobalRawId = CommunicationIdentifier.BOT_DOD_GLOBAL_PREFIX + microsoftBotId;
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, true)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftBotIdentifier("override", true)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD)
                .setRawId(dodGlobalRawId));
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, true)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftBotIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD)
                .setRawId(dodGlobalRawId));

        String dodRawId = CommunicationIdentifier.BOT_DOD_PREFIX + microsoftBotId;
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftBotIdentifier("override", true)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD)
                .setRawId(dodRawId));
        assertEquals(new MicrosoftBotIdentifier(microsoftBotId, false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftBotIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD)
                .setRawId(dodRawId));
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
        assertIdentifier("4:112345556789", new PhoneNumberIdentifier("112345556789"));
        assertIdentifier("4:+112345556789", new PhoneNumberIdentifier("+112345556789"));
        assertIdentifier("4:otherFormat", new PhoneNumberIdentifier("otherFormat"));
        assertIdentifier("4:207ffef6-9444-41fb-92ab-20eacaae2768", new PhoneNumberIdentifier("207ffef6-9444-41fb-92ab-20eacaae2768"));
        assertIdentifier("4:207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768", new PhoneNumberIdentifier("207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768"));
        assertIdentifier("4:+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768", new PhoneNumberIdentifier("+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768"));

        assertIdentifier(CommunicationIdentifier.BOT_PREFIX + microsoftBotId, new MicrosoftBotIdentifier(microsoftBotId, true));
        assertIdentifier(CommunicationIdentifier.BOT_PUBLIC_PREFIX + microsoftBotId, new MicrosoftBotIdentifier(microsoftBotId, false));
        assertIdentifier(CommunicationIdentifier.BOT_DOD_PREFIX + microsoftBotId, new MicrosoftBotIdentifier(microsoftBotId, false).setCloudEnvironment(CommunicationCloudEnvironment.DOD));
        assertIdentifier(CommunicationIdentifier.BOT_DOD_GLOBAL_PREFIX + microsoftBotId, new MicrosoftBotIdentifier(microsoftBotId, true).setCloudEnvironment(CommunicationCloudEnvironment.DOD));
        assertIdentifier(CommunicationIdentifier.BOT_GCCH_PREFIX + microsoftBotId, new MicrosoftBotIdentifier(microsoftBotId, false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH));
        assertIdentifier(CommunicationIdentifier.BOT_GCCH_GLOBAL_PREFIX + microsoftBotId, new MicrosoftBotIdentifier(microsoftBotId, true).setCloudEnvironment(CommunicationCloudEnvironment.GCCH));

        assertIdentifier("48:45ab2481-1c1c-4005-be24-0ffb879b1130", new UnknownIdentifier("48:45ab2481-1c1c-4005-be24-0ffb879b1130"));

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> CommunicationIdentifier.fromRawId(null));
        assertEquals("The parameter [rawId] cannot be null to empty.", illegalArgumentException.getMessage());
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
        assertRoundTrip("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:orgid:legacyFormat");
        assertRoundTrip("4:+112345556789");
        assertRoundTrip("4:112345556789");
        assertRoundTrip("4:otherFormat");
        assertRoundTrip("4:207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRoundTrip("4:207ffef6-9444-41fb-92ab-20eacaae2768_207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRoundTrip("4:+112345556789_207ffef6-9444-41fb-92ab-20eacaae2768");
        assertRoundTrip(CommunicationIdentifier.BOT_PREFIX + microsoftBotId);
        assertRoundTrip(CommunicationIdentifier.BOT_PUBLIC_PREFIX + microsoftBotId);
        assertRoundTrip(CommunicationIdentifier.BOT_DOD_PREFIX + microsoftBotId);
        assertRoundTrip(CommunicationIdentifier.BOT_DOD_GLOBAL_PREFIX + microsoftBotId);
        assertRoundTrip(CommunicationIdentifier.BOT_GCCH_PREFIX + microsoftBotId);
        assertRoundTrip(CommunicationIdentifier.BOT_DOD_GLOBAL_PREFIX + microsoftBotId);
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
