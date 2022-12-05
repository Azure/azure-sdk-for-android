// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.implementation;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP response which will buffer the response's body.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final ClientLogger logger = new ClientLogger(BufferedHttpResponse.class);

    private static final Pattern CHARSET_PATTERN
        = Pattern.compile("charset=([\\S]+)\\b", Pattern.CASE_INSENSITIVE);

    private final HttpResponse innerHttpResponse;
    private byte[] bufferedContent;
    private volatile boolean closed;


    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer.
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
    }

    @Override
    public int getStatusCode() {
        return this.innerHttpResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return this.innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.innerHttpResponse.getHeaders();
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(this.getBodyAsByteArray());
    }

    @Override
    public synchronized byte[] getBodyAsByteArray() {
        if (this.bufferedContent == null) {
            InputStream innerStream = this.innerHttpResponse.getBody();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = innerStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                this.bufferedContent = outStream.toByteArray();
            } catch (IOException ioe) {
                throw logger.logExceptionAsError(new RuntimeException(ioe));
            }
        }
        return this.bufferedContent;
    }

    @Override
    public String getBodyAsString() {
        return bomAwareToString(this.getBodyAsByteArray(),
            this.innerHttpResponse.getHeaderValue("Content-Type"));
    }

    @Override
    public String getBodyAsString(Charset charset) {
        return new String(this.getBodyAsByteArray(), charset);
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.innerHttpResponse.close();
            this.closed = true;
            super.close();
        }
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
            return new String(bytes, 3, bytes.length - 3, Charset.forName("UTF-8"));
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
            return new String(bytes, 2, bytes.length - 2, Charset.forName("UTF-16BE"));
        } else if (bytes.length >= 2
            && bytes[0] == (byte) 0xFF
            && bytes[1] == (byte) 0xFE) {
            return new String(bytes, 2, bytes.length - 2, Charset.forName("UTF-16LE"));
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
                        return new String(bytes, Charset.forName("UTF-8"));
                    }
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                    return new String(bytes, Charset.forName("UTF-8"));
                }
            } else {
                return new String(bytes, Charset.forName("UTF-8"));
            }
        }
    }
}