// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

/**
 * Catch-all for all other Communication identifiers for Communication Services
 */
public final class UnknownIdentifier extends CommunicationIdentifier {

    private final String id;

    /**
     * Creates an UnknownIdentifier object
     *
     * @param id the string identifier representing the identity
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public UnknownIdentifier(String id) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("The initialization parameter [id] cannot be null or empty.");
        }
        this.id = id;
        this.rawId = id;
    }

    /**
     * Get id of this identifier
     * @return id of this identifier
     */
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
        return this.getRawId().equals(thatId.getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }
}
