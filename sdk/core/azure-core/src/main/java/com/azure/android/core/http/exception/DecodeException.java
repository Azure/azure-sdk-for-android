// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.exception;

import okhttp3.Response;

/**
 * Exception thrown during response deserialization. Indicates the HTTP response could not be decoded.
 */
public class DecodeException extends HttpResponseException {
    /**
     * Initializes a new instance of the {@link DecodeException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response received from the Azure service.
     */
    public DecodeException(final String message, final Response response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link DecodeException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response received from the Azure service.
     * @param value    The deserialized response value.
     */
    public DecodeException(final String message, final Response response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the {@link DecodeException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response received from the Azure service.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public DecodeException(final String message, final Response response, final Throwable cause) {
        super(message, response, cause);
    }
}
