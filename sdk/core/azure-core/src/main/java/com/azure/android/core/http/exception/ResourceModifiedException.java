// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.exception;

import okhttp3.Response;

/**
 * Exception thrown for invalid resource modification with status code of 4XX, typically 409 Conflict.
 */
public class ResourceModifiedException extends HttpResponseException {
    /**
     * Initializes a new instance of the {@link ResourceModifiedException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     */
    public ResourceModifiedException(final String message, final Response response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link ResourceModifiedException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param value    The deserialized response value.
     */
    public ResourceModifiedException(final String message, final Response response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the {@link ResourceModifiedException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public ResourceModifiedException(final String message, final Response response, final Throwable cause) {
        super(message, response, cause);
    }
}
