// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

class ValidationUtils {

    /**
     * Validate string
     * @param value a value to be validated
     * @param paramName Parameter name for exceptionMessage
     * @return value
     *
     * @throws IllegalArgumentException when value is null or Empty.
     */
    public static String validateNotNullOrEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            String message = "The parameter [" + paramName + "] cannot be null or empty.";
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /**
     * Validate string
     * @param cloudEnvironment a value to be validated
     * @param paramName Parameter name for exceptionMessage
     * @return value
     *
     * @throws IllegalArgumentException when value is null or Empty.
     */
    public static CommunicationCloudEnvironment validateNotNull(CommunicationCloudEnvironment cloudEnvironment,
                                                                String paramName) {
        if (cloudEnvironment == null) {
            String message = "The parameter [" + paramName + "] cannot be null.";
            throw new IllegalArgumentException(message);
        }
        return cloudEnvironment;
    }

}
