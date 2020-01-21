// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.exception;

import okhttp3.Response;

/**
 * Exception thrown when the HTTP request tried to create an already existing resource with status code of 4XX,
 * typically 412 conflict.
 */
public class ResourceExistsException extends HttpResponseException {
    /**
     * Initializes a new instance of the {@link ResourceExistsException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     */
    public ResourceExistsException(final String message, final Response response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link ResourceExistsException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param value    The deserialized response value.
     */
    public ResourceExistsException(final String message, final Response response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the {@link ResourceExistsException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public ResourceExistsException(final String message, final Response response, final Throwable cause) {
        super(message, response, cause);
    }
}
