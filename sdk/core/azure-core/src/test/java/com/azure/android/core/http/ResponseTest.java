package com.azure.android.core.http;

import org.junit.Test;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;

public class ResponseTest {
    private final Request request = new Request.Builder().url(new MockWebServer().url("/")).build();
    private final Headers headers = new Headers.Builder().build();
    private final int statusCode = 200; // OK 200
    private final String value = "Test value";
    private final Response<String> response = new Response<>(request, statusCode, headers, value);

    @Test
    public void test_getRequest() {
        assertEquals(request, response.getRequest());
    }

    @Test
    public void test_getStatusCode() {
        assertEquals(statusCode, response.getStatusCode());
    }

    @Test
    public void test_getHeaders() {
        assertEquals(headers, response.getHeaders());
    }

    @Test
    public void test_getValue() {
        assertEquals(value, response.getValue());
    }
}
