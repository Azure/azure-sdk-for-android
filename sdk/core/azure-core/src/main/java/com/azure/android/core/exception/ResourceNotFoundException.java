// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.exception;

import okhttp3.Response;

/**
 * Exception thrown when a resource could not be found, typically triggered by a 412 response (for update) or 404 (for
 * GET/POST).
 */
public class ResourceNotFoundException extends HttpResponseException {
    /**
     * Initializes a new instance of the {@link ResourceNotFoundException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     */
    public ResourceNotFoundException(final String message, final Response response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link ResourceNotFoundException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param value    The deserialized response value.
     */
    public ResourceNotFoundException(final String message, final Response response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the {@link ResourceNotFoundException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public ResourceNotFoundException(final String message, final Response response, final Throwable cause) {
        super(message, response, cause);
    }
}
