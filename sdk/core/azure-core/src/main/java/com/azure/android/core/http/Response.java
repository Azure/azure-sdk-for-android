// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * Rest API response with a strongly-typed content.
 *
 * @param <T> The type of the response content, available by using {@link #getValue()}.
 */
public class Response<T> {
    private final okhttp3.Headers headers;
    private final okhttp3.Request request;
    private final int statusCode;
    private final T value;

    /**
     * Creates Response.
     *
     * @param request    The request that resulted in this response.
     * @param statusCode The HTTP response status code.
     * @param headers    The HTTP response header.
     * @param value      The HTTP response content as a strongly typed instance.
     */
    public Response(okhttp3.Request request, int statusCode, okhttp3.Headers headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Gets the HTTP request which resulted in this response.
     *
     * @return The HTTP request.
     */
    okhttp3.Request getRequest() {
        return this.request;
    }

    /**
     * Gets the HTTP response status code.
     *
     * @return The status code of the HTTP response.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the headers from the HTTP response.
     *
     * @return The HTTP response headers.
     */
    public okhttp3.Headers getHeaders() {
        return this.headers;
    }

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    public T getValue() {
        return this.value;
    }
}
