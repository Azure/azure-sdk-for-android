// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {
    static final String ACS_USER_PREFIX = "8:acs:";
    static final String ACS_USER_GCCH_CLOUD_PREFIX = "8:gcch-acs:";
    static final String ACS_USER_DOD_CLOUD_PREFIX = "8:dod-acs:";
    static final String SPOOL_USER_PREFIX = "8:spool:";
    static final String TEAMS_USER_PUBLIC_CLOUD_PREFIX = "8:orgid:";
    static final String TEAMS_USER_GCCH_CLOUD_PREFIX = "8:gcch:";
    static final String TEAMS_USER_DOD_CLOUD_PREFIX = "8:dod:";
    static final String TEAMS_ANONYMOUS_USER_PREFIX = "8:teamsvisitor:";
    static final String TEAMS_APP_PUBLIC_CLOUD_PREFIX = "28:orgid:";
    static final String TEAMS_APP_DOD_CLOUD_PREFIX = "28:dod:";
    static final String TEAMS_APP_GCCH_CLOUD_PREFIX = "28:gcch:";
    static final String PHONE_NUMBER_PREFIX = "4:";
    private String rawId;

    /**
     * When storing rawIds, use this function to restore the identifier that was encoded in the rawId.
     *
     * @param rawId raw id.
     * @return CommunicationIdentifier
     * @throws IllegalArgumentException raw id is null or empty.
     */
    public static CommunicationIdentifier fromRawId(String rawId) {
        ValidationUtils.validateNotNullOrEmpty(rawId, "rawId");

        if (rawId.startsWith(PHONE_NUMBER_PREFIX)) {
            return new PhoneNumberIdentifier(rawId.substring(PHONE_NUMBER_PREFIX.length()));
        }
        final String[] segments = rawId.split(":");
        int segmentCount = segments.length;
        if (segmentCount != 3) {
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1] + ":";
        final String suffix = segments[2];
        switch (prefix) {
            case TEAMS_ANONYMOUS_USER_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, true);
            case TEAMS_USER_PUBLIC_CLOUD_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, false);
            case TEAMS_USER_DOD_CLOUD_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, false)
                    .setCloudEnvironment(CommunicationCloudEnvironment.DOD);
            case TEAMS_USER_GCCH_CLOUD_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, false)
                    .setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
            case TEAMS_APP_PUBLIC_CLOUD_PREFIX:
                return new MicrosoftTeamsAppIdentifier(suffix);
            case TEAMS_APP_DOD_CLOUD_PREFIX:
                return new MicrosoftTeamsAppIdentifier(suffix, CommunicationCloudEnvironment.DOD);
            case TEAMS_APP_GCCH_CLOUD_PREFIX:
                return new MicrosoftTeamsAppIdentifier(suffix, CommunicationCloudEnvironment.GCCH);
            case SPOOL_USER_PREFIX:
                return new CommunicationUserIdentifier(rawId);
            case ACS_USER_PREFIX:
            case ACS_USER_DOD_CLOUD_PREFIX:
            case ACS_USER_GCCH_CLOUD_PREFIX:
                return tryCreateTeamsExtensionUserOrCommunicationUser(prefix, suffix, rawId);
            default:
                return new UnknownIdentifier(rawId);
        }
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

    private static CommunicationIdentifier tryCreateTeamsExtensionUserOrCommunicationUser(String prefix, String suffix,
                                                                                          String rawId) {
        String[] segments = suffix.split("_");
        if (segments.length != 3) {
            return new CommunicationUserIdentifier(rawId);
        }

        String resourceId = segments[0];
        String tenantId = segments[1];
        String userId = segments[2];
        CommunicationCloudEnvironment cloud = determineCloudEnvironment(prefix);

        return new TeamsExtensionUserIdentifier(userId, tenantId, resourceId).setCloudEnvironment(cloud);
    }

    /**
     * Determine the cloud based on identifier prefix.
     * @param cloudPrefix .
     * @return CommunicationCloudEnvironment.
     * @throws IllegalArgumentException thrown if CommunicationCloudEnvironment cannot be initialized.
     */
    static CommunicationCloudEnvironment determineCloudEnvironment(String cloudPrefix) {
        switch (cloudPrefix) {
            case ACS_USER_DOD_CLOUD_PREFIX:
                return CommunicationCloudEnvironment.DOD;
            case ACS_USER_GCCH_CLOUD_PREFIX:
                return CommunicationCloudEnvironment.GCCH;
            case ACS_USER_PREFIX:
                return CommunicationCloudEnvironment.PUBLIC;
            default:
                throw  new IllegalArgumentException("Cannot initialize CommunicationCloudEnvironment.");
        }
    }
}
