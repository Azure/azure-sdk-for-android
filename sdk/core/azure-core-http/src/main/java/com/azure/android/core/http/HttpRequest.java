// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.micro.util.CancellationToken;
import com.azure.android.core.micro.util.Context;
import com.azure.core.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * The outgoing Http request. It provides ways to construct {@link HttpRequest} with {@link HttpMethod},
 * {@code url}, {@link HttpHeader} request body, request {@link Context} and {@link CancellationToken}.
 */
public class HttpRequest {
    private final ClientLogger logger = new ClientLogger(HttpRequest.class);

    private HttpMethod httpMethod;
    private URL url;
    private HttpHeaders headers;
    private byte[] body;
    private final Context context;
    private final CancellationToken cancellationToken;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request to.
     * @param context The thread safe and immutable key-value store containing contextual information for the request.
     * @param cancellationToken The cancellation token for this request, on which the caller
     *     may request cancellation of this request execution.
     * @throws IllegalArgumentException if the url is malformed.
     */
    public HttpRequest(HttpMethod httpMethod,
                       String url,
                       Context context,
                       CancellationToken cancellationToken) {
        this.httpMethod = Objects.requireNonNull(httpMethod, "'httpMethod' is required.");
        Objects.requireNonNull(url, "'url' is required.");
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", ex));
        }
        this.headers = new HttpHeaders();
        this.context = Objects.requireNonNull(context, "'context' is required.");
        this.cancellationToken =  Objects.requireNonNull(cancellationToken, "'cancellationToken' is required.");
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request to.
     * @param headers The HTTP headers to use with this request.
     * @param body The request content.
     * @param context The thread safe and immutable key-value store containing contextual information for the request.
     * @param cancellationToken The cancellation token for this request, on which the caller
     *     may request cancellation of this request execution.
     * @throws IllegalArgumentException if the url is malformed.
     */
    public HttpRequest(HttpMethod httpMethod,
                       String url,
                       HttpHeaders headers,
                       byte[] body,
                       Context context,
                       CancellationToken cancellationToken) {
        this.httpMethod = Objects.requireNonNull(httpMethod, "'httpMethod' is required.");
        Objects.requireNonNull(url, "'url' is required.");
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", ex));
        }
        this.headers = Objects.requireNonNull(headers, "'headers' is required.");
        this.body = Objects.requireNonNull(body, "'body' is required.");
        this.context = Objects.requireNonNull(context, "'context' is required.");
        this.cancellationToken = Objects.requireNonNull(cancellationToken, "'cancellationToken' is required.");
    }

    /**
     * Get the request method.
     *
     * @return the request method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Set the request method.
     *
     * @param httpMethod the request method
     * @return this HttpRequest
     */
    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Get the target address.
     *
     * @return the target address
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url target address.
     * @return this HttpRequest
     * @throws IllegalArgumentException if the url is malformed.
     */
    public HttpRequest setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", ex));
        }
        return this;
    }

    /**
     * Get the request headers.
     *
     * @return headers to be sent
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Set the request headers.
     *
     * @param headers the set of headers
     * @return this HttpRequest
     */
    public HttpRequest setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Set a request header, replacing any existing value.
     * A null for {@code value} will remove the header if one with matching name exists.
     *
     * @param name the header name
     * @param value the header value
     * @return this HttpRequest
     */
    public HttpRequest setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Get the request content.
     *
     * @return the content to be send
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Set the request content.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(String content) {
        final byte[] bodyBytes = content.getBytes(StandardCharsets.UTF_8);
        return setBody(bodyBytes);
    }

    /**
     * Set the request content.
     * The Content-Length header will be set based on the given content's length
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(byte[] content) {
        headers.put("Content-Length", String.valueOf(content.length));
        this.body = content;
        return this;
    }

    /**
     * The thread safe and immutable key-value store to carry the contextual information for the request.
     *
     * @return The request context.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Get the cancellation token assigned to the request, which can be used to cancel this request.
     *
     * <p>
     *  Note that cancellation is the best effort; In HttpClient implementations, once the execution
     *  passed the point of no-cancellation, it will not honor the cancel request. Where precisely
     *  in the HTTP stack is this point of no-cancellation is depends on each HTTP Client implementation.
     * </p>
     *
     * @return The cancellation token.
     */
    public CancellationToken getCancellationToken() {
        return this.cancellationToken;
    }

    /**
     * Creates a copy of the request.
     *
     * The main purpose of this is so that this HttpRequest can be changed and the resulting
     * HttpRequest can be a backup. This means that the cloned HttpHeaders and body must
     * not be able to change from side effects of this HttpRequest.
     *
     * @return a new HTTP request instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        final HttpHeaders bufferedHeaders = new HttpHeaders(this.headers);
        return new HttpRequest(this.httpMethod,
            this.url.toString(),
            bufferedHeaders,
            this.body,
            this.context,
            this.cancellationToken);
    }
}
