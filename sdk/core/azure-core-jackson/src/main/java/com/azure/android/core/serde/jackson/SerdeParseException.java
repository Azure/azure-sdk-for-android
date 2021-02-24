// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

/**
 * An exception thrown while parsing an invalid input during serialization or deserialization.
 */
public class SerdeParseException extends RuntimeException {
    /**
     * Create a MalformedValueException instance.
     *
     * @param message the exception message
     */
    public SerdeParseException(String message) {
        super(message);
    }

    /**
     * Create a MalformedValueException instance.
     *
     * @param message the exception message
     * @param cause the actual cause
     */
    public SerdeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
