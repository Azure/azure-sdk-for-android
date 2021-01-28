// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.util.CoreUtil;

/**
 * Communication identifier for Communication Services representing a PhoneNumber
 */
public class PhoneNumberIdentifier extends CommunicationIdentifier {

    private final String phoneNumber;
    private String id;

    /**
     * Creates a PhoneNumber object
     *
     * @param phoneNumber the phone number in E.164 format.
     * @throws IllegalArgumentException thrown if phoneNumber parameter fail the validation.
     */
    public PhoneNumberIdentifier(String phoneNumber) {
        if (CoreUtil.isNullOrEmpty(phoneNumber)) {
            throw new IllegalArgumentException("The initialization parameter [phoneNumber] cannot be null or empty.");
        }
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the phone number in E.164 format.
     *
     * @return the phone number in E.164 format.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Get the full id of the identifier
     */
     @Override
    public String getId() {
        return id;
    }

    /**
     * Set the full id of this identifier
     * @param id the full id of this identifier
     * @return the PhoneNumberIdentifier object itself
     */
    public PhoneNumberIdentifier setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof PhoneNumberIdentifier)) {
            return false;
        }

        PhoneNumberIdentifier phoneId = (PhoneNumberIdentifier) that;
        if (!phoneNumber.equals(phoneId.phoneNumber)) {
            return false;
        }

        return id == null
            || phoneId.id == null
            || id.equals(phoneId.id);
    }

    @Override
    public int hashCode() {
        return phoneNumber.hashCode();
    }
}
