// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.exception;

/**
 * The base Azure exception.
 *
 * @see ServiceResponseException
 */
public class AzureException extends RuntimeException {

    /**
     * Initializes a new instance of the AzureException class.
     */
    public AzureException() {
        super();
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message The exception message.
     */
    public AzureException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param cause The {@link Throwable} which caused the creation of this AzureException.
     */
    public AzureException(final Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new instance of the AzureException class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this AzureException.
     */
    public AzureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
