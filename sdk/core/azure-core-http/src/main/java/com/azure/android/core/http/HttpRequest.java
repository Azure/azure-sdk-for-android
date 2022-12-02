// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.http.implementation.Util;
import com.azure.android.core.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * The outgoing Http request. It provides ways to construct {@link HttpRequest} with {@link HttpMethod},
 * {@code url}, {@link HttpHeader} and request body.
 */
public class HttpRequest {
    private final ClientLogger logger = new ClientLogger(HttpRequest.class);

    private HttpMethod httpMethod;
    private URL url;
    private HttpHeaders headers;
    private byte[] body;
    private Map<Object, Object> tags;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request to.
     * @throws IllegalArgumentException if the url is malformed.
     */
    public HttpRequest(HttpMethod httpMethod,
                       String url) {
        this.httpMethod = Util.requireNonNull(httpMethod, "'httpMethod' is required.");
        Util.requireNonNull(url, "'url' is required.");
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", ex));
        }
        this.headers = new HttpHeaders();
        this.tags = new HashMap<>(0);
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request to.
     * @param headers The HTTP headers to use with this request.
     * @param body The request content.
     * @throws IllegalArgumentException if the url is malformed.
     */
    public HttpRequest(HttpMethod httpMethod,
                       String url,
                       HttpHeaders headers,
                       byte[] body) {
        this.httpMethod = Util.requireNonNull(httpMethod, "'httpMethod' is required.");
        Util.requireNonNull(url, "'url' is required.");
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", ex));
        }
        this.headers = Util.requireNonNull(headers, "'headers' is required.");
        this.body = Util.requireNonNull(body, "'body' is required.");
        this.tags = new HashMap<>(0);
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
        final byte[] bodyBytes = content.getBytes(Charset.forName("UTF-8"));
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
     * Gets the tags-store associated with the request.
     *
     * <p>
     * Tags are key-value data stored in a map and carried with the request.
     * Use it to store any arbitrary data (such as debugging info) that you want to access
     * from the request later in the call stack.
     * </p>
     *
     * @return The tags.
     */
    public Map<Object, Object> getTags() {
        return this.tags;
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
        HttpRequest requestCopy = new HttpRequest(this.httpMethod,
            this.url.toString(),
            new HttpHeaders(this.headers),
            this.body);
        // shallow-copy the tags.
        requestCopy.tags = new HashMap<>(this.tags);
        return requestCopy;
    }
}