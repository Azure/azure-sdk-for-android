// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.test.http;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.util.CancellationToken;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Generic test suite for {@link HttpClient HttpClients}.
 */
public abstract class HttpClientTests {
    private static final String REQUEST_HOST = "http://localhost";
    private static final String PLAIN_RESPONSE = "plainBytesNoHeader";
    private static final String HEADER_RESPONSE = "plainBytesWithHeader";
    private static final String INVALID_HEADER_RESPONSE = "plainBytesInvalidHeader";
    private static final String UTF_8_BOM_RESPONSE = "utf8BomBytes";
    private static final String UTF_16BE_BOM_RESPONSE = "utf16BeBomBytes";
    private static final String UTF_16LE_BOM_RESPONSE = "utf16LeBomBytes";
    private static final String UTF_32BE_BOM_RESPONSE = "utf32BeBomBytes";
    private static final String UTF_32LE_BOM_RESPONSE = "utf32LeBomBytes";
    private static final String BOM_WITH_SAME_HEADER = "bomBytesWithSameHeader";
    private static final String BOM_WITH_DIFFERENT_HEADER = "bomBytesWithDifferentHeader";

    private static final byte[] EXPECTED_RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

    /**
     * Get the dynamic port the WireMock server is using to properly route the request.
     *
     * @return The HTTP port WireMock is using.
     */
    protected abstract int getWireMockPort();

    /**
     * Tests that a response without a byte order mark or a 'Content-Type' header encodes using UTF-8.
     */
    @Test
    public void plainResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        String actual = sendRequest(PLAIN_RESPONSE, "plainResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @Test
    public void headerResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);
        String actual = sendRequest(HEADER_RESPONSE, "headerResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @Test
    public void invalidHeaderResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        String actual = sendRequest(INVALID_HEADER_RESPONSE, "invalidHeaderResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf8BomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        String actual = sendRequest(UTF_8_BOM_RESPONSE, "utf8BomResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);
        String actual = sendRequest(UTF_16BE_BOM_RESPONSE, "utf16BeBomResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);
        String actual = sendRequest(UTF_16LE_BOM_RESPONSE, "utf16LeBomResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32BeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));
        String actual = sendRequest(UTF_32BE_BOM_RESPONSE, "utf32BeBomResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32LeBomResponse() {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));
        String actual = sendRequest(UTF_32LE_BOM_RESPONSE, "utf32LeBomResponse");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithSameHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        String actual = sendRequest(BOM_WITH_SAME_HEADER, "bomWithSameHeader");
        assertEquals(expected, actual);
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithDifferentHeader() {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        String actual = sendRequest(BOM_WITH_DIFFERENT_HEADER, "bomWithDifferentHeader");
        assertEquals(expected, actual);
    }

    private String sendRequest(String requestPath, String method) {
        CountDownLatch latch = new CountDownLatch(1);

        final String[] content = new String[1];
        final Throwable[] throwable = new Throwable[1];

        content[0] = null;
        throwable[0] = null;
        createHttpClient()
            .send(new HttpRequest(HttpMethod.GET, REQUEST_HOST + ":" + getWireMockPort() + "/" + requestPath),
                CancellationToken.NONE, new HttpCallback() {
                    @Override
                    public void onSuccess(HttpResponse response) {
                        try {
                            content[0] = response.getBodyAsString();
                        } finally {
                            latch.countDown();
                        }

                    }

                    @Override
                    public void onError(Throwable error) {
                        try {
                            throwable[0] = error;
                        } finally {
                            latch.countDown();
                        }
                    }
                });

        awaitOnLatch(latch, "method");

        if (throwable[0] != null) {
            throw new RuntimeException(throwable[0]);
        } else {
            return content[0];
        }
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}