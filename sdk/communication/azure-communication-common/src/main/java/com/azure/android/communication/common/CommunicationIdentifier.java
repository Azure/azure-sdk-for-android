// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {
    private static final String ACS_USER_PREFIX = "8:acs:";
    private static final String ACS_GCCH_USER_PREFIX = "8:gcch-acs:";
    private static final String ACS_DOD_USER_PREFIX = "8:dod-acs:";
    private static final String SPOOL_USER_PREFIX = "8:spool:";
    protected static final String TEAMS_PUBLIC_USER_PREFIX = "8:orgid:";
    protected static final String TEAMS_GCCH_USER_PREFIX = "8:gcch:";
    protected static final String TEAMS_DOD_USER_PREFIX = "8:dod:";
    protected static final String TEAMS_ANONYMOUS_USER_PREFIX = "8:teamsvisitor:";
    private static final String BOT_PUBLIC_PREFIX = "28:orgid:";
    private static final String BOT_DOD_PREFIX = "28:dod:";
    private static final String BOT_DOD_GLOBAL_PREFIX = "28:dod-global:";
    private static final String BOT_GCCH_PREFIX = "28:gcch";
    private static final String BOT_GCCH_GLOBAL_PREFIX = "28:gcch-global:";
    protected static final String PHONE_NUMBER_PREFIX = "4:";
    private String rawId;

    /**
     * When storing rawIds, use this function to restore the identifier that was encoded in the rawId.
     *
     * @param rawId raw id.
     * @return CommunicationIdentifier
     * @throws IllegalArgumentException raw id is null or empty.
     */
    public static CommunicationIdentifier fromRawId(String rawId) {
        if (rawId == null || rawId.trim().length() == 0) {
            throw new IllegalArgumentException("The parameter [rawId] cannot be null to empty.");
        }

        if (rawId.startsWith(PHONE_NUMBER_PREFIX)) {
            return new PhoneNumberIdentifier(rawId.substring(PHONE_NUMBER_PREFIX.length()));
        }
        final String[] segments = rawId.split(":");
        if (segments.length < 3) {
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1] + ":";
        final String suffix = rawId.substring(prefix.length());

        if (TEAMS_ANONYMOUS_USER_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, true);
        } else if (TEAMS_PUBLIC_USER_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false);
        } else if (TEAMS_DOD_USER_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (TEAMS_GCCH_USER_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (ACS_USER_PREFIX.equals(prefix) || SPOOL_USER_PREFIX.equals(prefix)
            || ACS_DOD_USER_PREFIX.equals(prefix) || ACS_GCCH_USER_PREFIX.equals(prefix)) {
            return new CommunicationUserIdentifier(rawId);
        }

        return new UnknownIdentifier(rawId);
    }

    /**
     * Returns the rawId for a given CommunicationIdentifier.
     * You can use the rawId for encoding the identifier and then use it as a key in a database.
     *
     * @return raw id
     */
    public String getRawId() {
        return rawId;
    }

    /**
     * Set full id of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full id of the identifier
     * @return CommunicationIdentifier object itself
     */
    protected CommunicationIdentifier setRawId(String rawId) {
        this.rawId = rawId;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof CommunicationIdentifier)) {
            return false;
        }

        CommunicationIdentifier thatId = (CommunicationIdentifier) that;
        return this.getRawId().equals(thatId.getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }
}
