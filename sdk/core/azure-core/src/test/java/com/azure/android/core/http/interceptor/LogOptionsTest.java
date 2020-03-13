package com.azure.android.core.http.interceptor;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class LogOptionsTest {
    @Test
    public void test_constructor() {
        List<String> defaultHeadersWhitelist = Arrays.asList(
            "x-ms-client-request-id",
            "x-ms-return-client-request-id",
            "traceparent",
            "Accept",
            "Cache-Control",
            "Connection",
            "Content-Length",
            "Content-Type",
            "Date",
            "Etag",
            "Expires",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Unmodified-Since",
            "Last-Modified",
            "Pragma",
            "Request-Id",
            "Retry-After",
            "Server",
            "Transfer-Encoding",
            "User-Agent"
        );
        LogOptions logOptions = new LogOptions();
        Set<String> headersWhitelist = logOptions.getAllowedHeaderNames();

        assertFalse(logOptions.getAllowedHeaderNames().isEmpty());

        for (String header : defaultHeadersWhitelist) {
            assertTrue(headersWhitelist.contains(header));
        }

        assertTrue(logOptions.getAllowedQueryParamNames().isEmpty());
    }

    @Test
    public void test_setAllowedHeaderNames() {
        LogOptions logOptions = new LogOptions();

        assertFalse(logOptions.getAllowedHeaderNames().isEmpty());
        assertEquals(22, logOptions.getAllowedHeaderNames().size());

        Set<String> headers = new HashSet<>();
        headers.add("Test-Header");
        logOptions.setAllowedHeaderNames(headers);

        assertEquals(1, logOptions.getAllowedHeaderNames().size());
        assertTrue(logOptions.getAllowedHeaderNames().contains("Test-Header"));
    }

    @Test
    public void setAllowedHeaderNames_withNullSet() {
        LogOptions logOptions = new LogOptions();
        logOptions.setAllowedHeaderNames(null);

        assertNotNull(logOptions.getAllowedHeaderNames());
        assertTrue(logOptions.getAllowedHeaderNames().isEmpty());
    }

    @Test
    public void setAllowedHeaderNames_withEmptySet() {
        LogOptions logOptions = new LogOptions();
        logOptions.setAllowedHeaderNames(Collections.emptySet());

        assertTrue(logOptions.getAllowedHeaderNames().isEmpty());
    }

    @Test
    public void test_addAllowedHeaderName() {
        LogOptions logOptions = new LogOptions();
        int headersSetSize = logOptions.getAllowedHeaderNames().size();

        logOptions.addAllowedHeaderName("Test-Header");

        assertTrue(logOptions.getAllowedHeaderNames().contains("Test-Header"));
        assertEquals(headersSetSize + 1, logOptions.getAllowedHeaderNames().size());
    }

    @Test
    public void test_setAllowedQueryParamNames() {
        LogOptions logOptions = new LogOptions();

        assertTrue(logOptions.getAllowedQueryParamNames().isEmpty());

        Set<String> queryParams = new HashSet<>();
        queryParams.add("param1");
        queryParams.add("param2");
        logOptions.setAllowedHeaderNames(queryParams);

        assertEquals(2, logOptions.getAllowedHeaderNames().size());
        assertTrue(logOptions.getAllowedHeaderNames().contains("param1"));
        assertTrue(logOptions.getAllowedHeaderNames().contains("param2"));
    }

    @Test
    public void setAllowedQueryParamNames_withNullSet() {
        LogOptions logOptions = new LogOptions();

        assertTrue(logOptions.getAllowedQueryParamNames().isEmpty());

        logOptions.setAllowedQueryParamNames(null);

        assertNotNull(logOptions.getAllowedQueryParamNames());
        assertTrue(logOptions.getAllowedQueryParamNames().isEmpty());
    }

    @Test
    public void setAllowedQueryParamNames_withEmptySet() {
        LogOptions logOptions = new LogOptions();

        assertTrue(logOptions.getAllowedQueryParamNames().isEmpty());

        logOptions.setAllowedQueryParamNames(Collections.emptySet());

        assertTrue(logOptions.getAllowedQueryParamNames().isEmpty());
    }

    @Test
    public void test_addAllowedQueryParamName() {
        LogOptions logOptions = new LogOptions();
        logOptions.addAllowedQueryParamName("param1");

        assertEquals(1, logOptions.getAllowedQueryParamNames().size());
        assertTrue(logOptions.getAllowedQueryParamNames().contains("param1"));
    }
}
