// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import com.azure.android.core.util.ExpandableStringEnum;

/**
 * The cloud that the identifier belongs to.
 */
public final class CommunicationCloudEnvironment extends ExpandableStringEnum<CommunicationCloudEnvironment> {
    private static final String PUBLIC_VALUE = "public";
    private static final String DOD_VALUE = "dod";
    private static final String GCCH_VALUE = "gcch";
    private final String environmentValue;

    /**
     * Creates Azure public cloud
     */
    public CommunicationCloudEnvironment() {
        this.environmentValue = PUBLIC_VALUE;
    }

    private CommunicationCloudEnvironment(String environmentValue) {
        if (environmentValue == null) {
            throw new NullPointerException();
        }
        this.environmentValue = environmentValue;
    }

    /**
     * Sets an environment value from a String and
     * returns the {@link CommunicationCloudEnvironment} associated with the name.
     *
     * @param name The name of the environment.
     * @return The {@link CommunicationCloudEnvironment} associated with the given name.
     * @throws NullPointerException if {@code name} is null
     */
    public static CommunicationCloudEnvironment fromString(String name) {
        return new CommunicationCloudEnvironment(name);
    }

    /**
     * Represent Azure public cloud
     */
    public static final CommunicationCloudEnvironment PUBLIC = fromString(PUBLIC_VALUE);

    /**
     * Represent Azure Dod cloud
     */
    public static final CommunicationCloudEnvironment DOD = fromString(DOD_VALUE);

    /**
     * Represent Azure Gcch cloud
     */
    public static final CommunicationCloudEnvironment GCCH = fromString(GCCH_VALUE);

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        return that != null && this.environmentValue.equals(that.toString());
    }

    @Override
    public String toString() {
        return environmentValue;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
