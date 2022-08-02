// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

/**
 * Communication identifier for Communication Services Users
 */
public final class CommunicationUserIdentifier extends CommunicationIdentifier {
    private final String id;

    /**
     * Creates a CommunicationUserIdentifier object
     *
     * @param id identifier of the communication user.
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public CommunicationUserIdentifier(String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("The initialization parameter [id] cannot be null or empty.");
        }
        this.id = id;
        this.setRawId(id);
    }

    /**
     * Get id of the communication user.
     *
     * @return id of the communication user.
     */
    public String getId() {
        return id;
    }

    /**
     * Set full id of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full id of the identifier
     * @return CommunicationUserIdentifier object itself
     */
    @Override
    public CommunicationUserIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof CommunicationUserIdentifier)) {
            return false;
        }

        return ((CommunicationUserIdentifier) that).getRawId().equals(getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }
}
