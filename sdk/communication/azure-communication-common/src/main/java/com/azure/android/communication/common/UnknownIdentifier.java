// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.util.CoreUtil;

/**
 * Catch-all for all other Communication identifiers for Communication Services
 */
public class UnknownIdentifier extends CommunicationIdentifier {

    private final String id;

    /**
     * Creates an UnknownIdentifier object
     *
     * @param id the string identifier representing the identity
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public UnknownIdentifier(String id) {
        if (CoreUtil.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("The initialization parameter [id] cannot be null or empty.");
        }
        this.id = id;
    }

    /**
     * Get the full id of the identifier
     */
     @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof UnknownIdentifier)) {
            return false;
        }

        UnknownIdentifier thatId = (UnknownIdentifier) that;
        return this.id.equals(thatId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
