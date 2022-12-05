// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.exception;

import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.exception.AzureException;

/**
 * The exception when an HTTP request fails.
 * <p>
 * Generally, these errors are safe to retry.
 */
public class HttpRequestException extends AzureException {

    /**
     * Information about the associated HTTP response.
     */
    private final transient HttpRequest request;

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     */
    public HttpRequestException(final HttpRequest request) {
        super();
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request the HTTP request sends to the Azure service
     */
    public HttpRequestException(final String message, final HttpRequest request) {
        super(message);
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     */
    public HttpRequestException(final HttpRequest request, final Throwable cause) {
        super(cause);
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     */
    public HttpRequestException(final String message, final HttpRequest request, final Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    /**
     * @return The {@link HttpRequest} being sent when the exception occurred.
     */
    public HttpRequest getRequest() {
        return request;
    }

}