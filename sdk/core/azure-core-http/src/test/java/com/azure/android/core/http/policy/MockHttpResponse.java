// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;


import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An HTTP response that is created to simulate a HTTP request.
 */
public class MockHttpResponse extends HttpResponse {
    private static final Pattern CHARSET_PATTERN
        = Pattern.compile("charset=([\\S]+)\\b", Pattern.CASE_INSENSITIVE);

    private final int statusCode;
    private final HttpHeaders headers;
    private final byte[] bodyBytes;

    /**
     * Creates a HTTP response associated with a {@code request}, returns the {@code statusCode}, and has an empty
     * response body.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new byte[0]);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and response body of
     * {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param bodyBytes Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, byte[] bodyBytes) {
        this(request, statusCode, new HttpHeaders(), bodyBytes);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and http headers.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers Headers of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, contains the
     * {@code headers}, and response body of {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers HttpHeaders of the response.
     * @param bodyBytes Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = clone(bodyBytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBodyAsByteArray() {
        if (this.bodyBytes == null) {
            return new byte[0];
        } else {
            return this.bodyBytes;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(this.getBodyAsByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBodyAsString() {
        if (this.bodyBytes == null) {
            return new String(new byte[0]);
        } else {
            return bomAwareToString(this.bodyBytes, getHeaderValue("Content-Type"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBodyAsString(Charset charset) {
        Objects.requireNonNull(charset, "'charset' cannot be null.");
        if (this.bodyBytes == null) {
            return new String(new byte[0]);
        } else {
            return new String(bodyBytes, charset);
        }
    }

    /**
     * Adds the header {@code name} and {@code value} to the existing set of HTTP headers.
     * @param name The header to add
     * @param value The header value.
     * @return The updated response object.
     */
    public MockHttpResponse addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    private static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /**
     * Attempts to convert a byte stream into the properly encoded String.
     * <p>
     * The method will attempt to find the encoding for the String in this order.
     * <ol>
     *     <li>Find the byte order mark in the byte array.</li>
     *     <li>Find the {@code charset} in the {@code Content-Type} header.</li>
     *     <li>Default to {@code UTF-8}.</li>
     * </ol>
     *
     * @param bytes Byte array.
     * @param contentType {@code Content-Type} header value.
     * @return A string representation of the byte array encoded to the found encoding.
     */
    private String bomAwareToString(byte[] bytes, String contentType) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length >= 3
            && bytes[0] == (byte) 0xEF
            && bytes[1] == (byte) 0xBB
            && bytes[2] == (byte) 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        } else if (bytes.length >= 4
            && bytes[0] == (byte) 0x00
            && bytes[1] == (byte) 0x00
            && bytes[2] == (byte) 0xFE
            && bytes[3] == (byte) 0xFF) {
            return new String(bytes, 4, bytes.length - 4, Charset.forName("UTF-32BE"));
        } else if (bytes.length >= 4
            && bytes[0] == (byte) 0xFF
            && bytes[1] == (byte) 0xFE
            && bytes[2] == (byte) 0x00
            && bytes[3] == (byte) 0x00) {
            return new String(bytes, 4, bytes.length - 4, Charset.forName("UTF-32LE"));
        } else if (bytes.length >= 2
            && bytes[0] == (byte) 0xFE
            && bytes[1] == (byte) 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        } else if (bytes.length >= 2
            && bytes[0] == (byte) 0xFF
            && bytes[1] == (byte) 0xFE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        } else {
            /*
             * Attempt to retrieve the default charset from the 'Content-Encoding' header,
             * if the value isn't present or invalid fallback to 'UTF-8' for the default charset.
             */
            if (contentType != null && contentType.length() != 0) {
                try {
                    Matcher charsetMatcher = CHARSET_PATTERN.matcher(contentType);
                    if (charsetMatcher.find()) {
                        return new String(bytes, Charset.forName(charsetMatcher.group(1)));
                    } else {
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            } else {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }
}