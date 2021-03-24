// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import com.azure.android.core.util.ExpandableStringEnum;

/**
 * The cloud that the identifier belongs to.
 */
public final class CommunicationCloudEnvironment extends ExpandableStringEnum<CommunicationCloudEnvironment> {
    /**
     * Returns the {@link CommunicationCloudEnvironment} associated with the name.
     *
     * @param name The name of the environment.
     * @return The {@link CommunicationCloudEnvironment} associated with the given name.
     */
    public static CommunicationCloudEnvironment fromString(String name) {
        return fromString(name, CommunicationCloudEnvironment.class);
    }

    /**
     * Represent Azure public cloud
     */
    public static final CommunicationCloudEnvironment PUBLIC = fromString("public");

    /**
     * Represent Azure Dod cloud
     */
    public static final CommunicationCloudEnvironment DOD = fromString("dod");

    /**
     * Represent Azure Gcch cloud
     */
    public static final CommunicationCloudEnvironment GCCH = fromString("gcch");
}
