// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.exception;

import okhttp3.Request;

/**
 * The exception occurred while attempting to connect a socket to a Azure service address and port.
 * Typically, the connection was refused remotely (e.g., no process is listening on the Azure
 * service address/port).
 */
public class HttpRequestException extends AzureException {

    /**
     * Information about the associated HTTP response.
     */
    private final transient Request request;

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param request the HTTP request sends to the Azure service
     */
    public HttpRequestException(final String message, final Request request) {
        super(message);
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param request the HTTP request sends to the Azure service
     * @param cause the Throwable which caused the creation of this ServiceRequestException
     */
    public HttpRequestException(final String message, final Request request, final Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    /**
     * @return information about the associated HTTP response
     */
    public Request getRequest() {
        return request;
    }
}
