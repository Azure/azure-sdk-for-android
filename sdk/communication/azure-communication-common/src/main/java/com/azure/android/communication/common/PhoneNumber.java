// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.util.CoreUtil;

/**
 * Communication identifier for Communication Services representing a PhoneNumber
 */
public class PhoneNumber extends CommunicationIdentifier {

    private final String value;

    /**
     * Creates a PhoneNumber object
     *
     * @param phoneNumber the string identifier representing the PhoneNumber
     * @throws IllegalArgumentException thrown if phoneNumber parameter fail the validation.
     */
    public PhoneNumber(String phoneNumber) {
        if (CoreUtil.isNullOrEmpty(phoneNumber)) {
            throw new IllegalArgumentException("The initialization parameter [phoneNumber] cannot be null or empty.");
        }
        this.value = phoneNumber;
    }

    /**
     * Gets the string identifier representing the object identity
     *
     * @return the string identifier representing the object identity
     */
    public String getValue() {
        return value;
    }
}
