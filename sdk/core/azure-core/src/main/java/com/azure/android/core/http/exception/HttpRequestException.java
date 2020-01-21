// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.exception;

import com.azure.android.core.exception.AzureException;

import okhttp3.Request;

/**
 * Exception thrown when an error occurs while attempting to connect a socket to a Azure service address and port.
 * Typically, the connection was refused remotely, e.g., no process is listening on the Azure service address/port.
 */
public class HttpRequestException extends AzureException {
    /**
     * Information about the associated HTTP response.
     */
    private final transient Request request;

    /**
     * Initializes a new instance of the {@link HttpRequestException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param request The HTTP request sends to the Azure service.
     */
    public HttpRequestException(final String message, final Request request) {
        super(message);

        this.request = request;
    }

    /**
     * Initializes a new instance of the {@link HttpRequestException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param request The HTTP request sent to the Azure service.
     * @param cause   The {@link Throwable} which caused the creation of this exception.
     */
    public HttpRequestException(final String message, final Request request, final Throwable cause) {
        super(message, cause);

        this.request = request;
    }

    /**
     * @return Information about the associated HTTP response.
     */
    public Request getRequest() {
        return request;
    }
}
