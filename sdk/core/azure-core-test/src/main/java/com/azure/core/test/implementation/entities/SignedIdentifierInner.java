// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation.entities;

import com.azure.core.serde.SerdeProperty;

/**
 * signed identifier.
 */
public class SignedIdentifierInner {
    /**
     * a unique id.
     */
    @SerdeProperty(value = "Id")
    private String id;

    /**
     * The access policy.
     */
    @SerdeProperty(value = "AccessPolicy")
    private AccessPolicy accessPolicy;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the SignedIdentifierInner object itself.
     */
    public SignedIdentifierInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the accessPolicy value.
     *
     * @return the accessPolicy value
     */
    public AccessPolicy accessPolicy() {
        return this.accessPolicy;
    }

    /**
     * Set the accessPolicy value.
     *
     * @param accessPolicy the accessPolicy value to set
     * @return the SignedIdentifierInner object itself.
     */
    public SignedIdentifierInner withAccessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        return this;
    }

}
