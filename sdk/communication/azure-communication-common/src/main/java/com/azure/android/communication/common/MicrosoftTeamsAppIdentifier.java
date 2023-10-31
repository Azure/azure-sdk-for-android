// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

/**
 * Communication identifier for a Microsoft Teams Application.
 */
public final class MicrosoftTeamsAppIdentifier extends CommunicationIdentifier {
    private final String appId;
    private boolean rawIdSet = false;
    private final CommunicationCloudEnvironment cloudEnvironment;

    /**
     * Creates a MicrosoftTeamsAppIdentifier object
     *
     * @param appId The unique Microsoft Teams Application ID.
     * @param cloudEnvironment the cloud environment in which this identifier is created.
     * @throws IllegalArgumentException thrown if appId parameter fail the validation.
     */
    public MicrosoftTeamsAppIdentifier(String appId,
                                  CommunicationCloudEnvironment cloudEnvironment) {
        if (appId == null || appId.trim().length() == 0) {
            throw new IllegalArgumentException("The initialization parameter [appId] cannot be null or empty.");
        }
        this.appId = appId;
        this.cloudEnvironment = cloudEnvironment;
        generateRawId();
    }

    /**
     * Creates a MicrosoftTeamsAppIdentifier object
     *
     * @param appId The unique Microsoft Teams Application ID.
     * @throws IllegalArgumentException thrown if appId parameter fail the validation.
     */
    public MicrosoftTeamsAppIdentifier(String appId) {
        this(appId, CommunicationCloudEnvironment.PUBLIC);
    }

    /**
     * Get the ID of Microsoft Teams Application.
     * @return ID of Microsoft Teams Application.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * Get cloud environment of the Microsoft Teams Application identifier.
     * @return cloud environment in which this identifier is created.
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    /**
     * Set full ID of the identifier.
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full ID of the identifier.
     * @return MicrosoftTeamsAppIdentifier object itself.
     */
    @Override
    public MicrosoftTeamsAppIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        rawIdSet = true;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof MicrosoftTeamsAppIdentifier)) {
            return false;
        }

        return ((MicrosoftTeamsAppIdentifier) that).getRawId().equals(this.getRawId());
    }


    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }

    private void generateRawId() {
        if (!rawIdSet) {
            if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                super.setRawId(TEAMS_APP_DOD_CLOUD_PREFIX + this.appId);
            } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                super.setRawId(TEAMS_APP_GCCH_CLOUD_PREFIX + this.appId);
            } else {
                super.setRawId(TEAMS_APP_PUBLIC_CLOUD_PREFIX + this.appId);
            }
        }
    }
}
