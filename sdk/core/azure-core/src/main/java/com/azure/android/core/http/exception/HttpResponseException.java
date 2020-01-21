// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.exception;

import com.azure.android.core.exception.AzureException;

import okhttp3.Response;

/**
 * Exception thrown when an unsuccessful response is received with http status code (e.g. 3XX, 4XX, 5XX) from the
 * service request.
 */
public class HttpResponseException extends AzureException {
    /**
     * The HTTP response value.
     */
    private final Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final Response response;

    /**
     * Initializes a new instance of the {@link HttpResponseException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     */
    public HttpResponseException(final String message, final Response response) {
        super(message);

        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the {@link HttpResponseException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param value    The deserialized response value.
     */
    public HttpResponseException(final String message, final Response response, final Object value) {
        super(message);

        this.value = value;
        this.response = response;
    }

    /**
     * Initializes a new instance of the {@link HttpResponseException} class.
     *
     * @param message  The exception message or the response content if a message is not available.
     * @param response The HTTP response associated with this exception.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public HttpResponseException(final String message, final Response response, final Throwable cause) {
        super(message, cause);

        this.value = null;
        this.response = response;
    }

    /**
     * Gets the associated HTTP response that caused the exception.
     *
     * @return Gets the associated HTTP response.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Gets the deserialized HTTP response value.
     *
     * @return The deserialized HTTP response value.
     */
    public Object getValue() {
        return value;
    }
}
