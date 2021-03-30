// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.http.implementation.BufferedHttpResponse;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * The type representing response of {@link HttpRequest}.
 *
 * The body of the {@link HttpResponse} is backed by an HTTP connection, the HTTP connections are
 * limited resources. It is important to close the response body to avoid leaking of backing connection
 * and associated resources, such leaking may ultimately cause the application to slow down or crash.
 *
 * {@link HttpResponse} implements {@link Closeable}, invoking {@link Closeable#close()} will close
 * the response body. The body must be closed by calling one of the following methods:
 *
 * <ul>
 *   <li>HttpResponse::close()</li>
 *   <li>HttpResponse::getBody().close()</li>
 *   <li>HttpResponse::getBodyAsByteArray()</li>
 *   <li>HttpResponse::getBodyAsString()</li>
 *   <li>HttpResponse::getBodyAsString(Charset)</li>
 * </ul>
 *
 */
public abstract class HttpResponse implements Closeable {
    private final HttpRequest request;

    /**
     * Creates a HttpResponse.
     *
     * @param request The request which resulted in this response.
     */
    protected HttpResponse(HttpRequest request) {
        this.request = request;
    }

    /**
     * Get the response status code.
     *
     * @return the response status code
     */
    public abstract int getStatusCode();

    /**
     * Lookup a response header with the provided name.
     *
     * @param name the name of the header to lookup.
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    public abstract String getHeaderValue(String name);

    /**
     * Get all response headers.
     *
     * @return the response headers
     */
    public abstract HttpHeaders getHeaders();

    /**
     * Get the {@link InputStream} producing response content chunks.
     *
     * @return The response's content as a stream.
     */
    public abstract InputStream getBody();

    /**
     * Get the response content as a byte[].
     *
     * @return this response content as a byte[]
     */
    public abstract byte[] getBodyAsByteArray();

    /**
     * Get the response content as a string.
     *
     * @return This response content as a string
     */
    public abstract String getBodyAsString();

    /**
     * Get the response content as a string.
     *
     * @param charset the charset to use as encoding
     * @return This response content as a string
     */
    public abstract String getBodyAsString(Charset charset);

    /**
     * Get the request which resulted in this response.
     *
     * @return the request which resulted in this response.
     */
    public final HttpRequest getRequest() {
        return request;
    }

    /**
     * Get a new Response object wrapping this response with it's content
     * buffered into memory.
     *
     * @return the new Response object
     */
    public HttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() {
    }
}
