// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer.exception;

/**
 * Exception thrown while parsing an invalid input during serialization or deserialization.
 */
public class MalformedValueException extends RuntimeException {
    /**
     * Create a {@link MalformedValueException} instance.
     *
     * @param message The exception message.
     */
    public MalformedValueException(String message) {
        super(message);
    }

    /**
     * Create a {@link MalformedValueException} instance.
     *
     * @param message The exception message.
     * @param cause   The actual cause.
     */
    public MalformedValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
