// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

/**
 * Communication identifier for Microsoft Teams Phone user who is using a Communication Services resource
 * to extend their Teams Phone set up.
 */
public final class TeamsExtensionUserIdentifier extends CommunicationIdentifier {

    private final String userId;

    private final String tenantId;

    private final String resourceId;

    private CommunicationCloudEnvironment cloudEnvironment;

    /**
     * Creates a TeamsExtensionUserIdentifier object with PUBLIC cloud environment.
     *
     * @param userId ID of the Microsoft Teams Extension user i.e. the Entra ID object id of the user.
     * @param tenantId Tenant ID of the Microsoft Teams Extension user.
     * @param resourceId The Communication Services resource id.
     * @throws IllegalArgumentException if any parameter fail the validation.
     */
    public TeamsExtensionUserIdentifier(String userId, String tenantId, String resourceId) {
        this.userId = ValidationUtils.validateNotNullOrEmpty(userId, "userId");
        this.tenantId = ValidationUtils.validateNotNullOrEmpty(tenantId, "tenantId");
        this.resourceId = ValidationUtils.validateNotNullOrEmpty(resourceId, "resourceId");
        this.cloudEnvironment = CommunicationCloudEnvironment.PUBLIC;

        generateRawId();
    }

    /**
     * Set full ID of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full ID of the identifier.
     * @return TeamsExtensionUserIdentifier object itself.
     */
    @Override
    public TeamsExtensionUserIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        return this;
    }

    /**
     * Get Microsoft Teams Extension user
     * @return ID of the Microsoft Teams Extension user i.e. the Entra ID object id of the user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get Microsoft Teams Extension user Tenant ID
     * @return Tenant ID of the Microsoft Teams Extension user.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get Communication Services resource id.
     * @return the Communication Services resource id.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Get cloud environment of the Teams Extension User identifier
     * @return cloud environment in which this identifier is created
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    /**
     * Set cloud environment of the Teams Extension User identifier
     *
     * @param cloudEnvironment the cloud environment in which this identifier is created
     * @return this object
     * @throws IllegalArgumentException if cloudEnvironment .
     *
     */
    public TeamsExtensionUserIdentifier setCloudEnvironment(CommunicationCloudEnvironment cloudEnvironment) {
        this.cloudEnvironment = ValidationUtils.validateNotNull(cloudEnvironment, "cloudEnvironment");
        generateRawId();
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof TeamsExtensionUserIdentifier)) {
            return false;
        }

        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private void generateRawId() {
        String identifierBase = this.resourceId + "_" + this.tenantId + "_" + this.userId;
        if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
            super.setRawId(ACS_USER_DOD_CLOUD_PREFIX + identifierBase);
        } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
            super.setRawId(ACS_USER_GCCH_CLOUD_PREFIX + identifierBase);
        } else {
            super.setRawId(ACS_USER_PREFIX + identifierBase);
        }
    }
}
