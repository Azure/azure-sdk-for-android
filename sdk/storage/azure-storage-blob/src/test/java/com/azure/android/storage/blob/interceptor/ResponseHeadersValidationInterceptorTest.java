// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.interceptor;

import com.azure.android.core.http.exception.HttpResponseException;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.storage.blob.common.TestUtils.buildOkHttpClientWithInterceptor;

public class ResponseHeadersValidationInterceptorTest {
    private static final String CLIENT_ID_HEADER = "x-ms-client-id";
    private static final String ENCRYPTION_KEY_SHA256_HEADER = "x-ms-encryption-key-sha256";

    private final ResponseHeadersValidationInterceptor normalizeEtagInterceptor = new ResponseHeadersValidationInterceptor();

    private final MockWebServer mockWebServer = new MockWebServer();
    private final OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(normalizeEtagInterceptor);

    @Test
    public void intercept_withEqualValuesInRequestAndResponseHeaders() throws IOException {
        // Given a client with a ResponseHeadersValidationInterceptor that only considers the mandatory headers used
        // by Storage.

        // When sending a request with said headers and its values and receiving a response that includes them as well.
        MockResponse response = new MockResponse()
            .addHeader(CLIENT_ID_HEADER, "client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header");
        mockWebServer.enqueue(response);

        okhttp3.Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(CLIENT_ID_HEADER, "client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header")
            .build();
        okHttpClient.newCall(request).execute();

        // Then the interceptor will not throw an exception because the headers on both the request and the response
        // have the same exact values.
    }

    @Test(expected = HttpResponseException.class)
    public void intercept_withDifferentValuesInRequestAndResponseHeaders() throws IOException {
        // Given a client with a ResponseHeadersValidationInterceptor that only considers the mandatory headers used
        // by Storage.

        // When sending a request with said headers and its values and receiving a response that includes them as well.
        MockResponse response = new MockResponse()
            .addHeader(CLIENT_ID_HEADER, "client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header");
        mockWebServer.enqueue(response);

        okhttp3.Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(CLIENT_ID_HEADER, "different-client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header")
            .build();
        okHttpClient.newCall(request).execute();

        // Then the interceptor will throw an exception because at least one of the headers on the request has a
        // different value that its counterpart in the response.
    }

    @Test(expected = HttpResponseException.class)
    public void intercept_withDifferentHeadersInRequestAndResponse() throws IOException {
        // Given a client with a ResponseHeadersValidationInterceptor that only considers the mandatory headers used
        // by Storage.

        // When sending a request that includes two mandatory headers used by Storage and receiving a response that
        // includes the same headers.
        MockResponse response = new MockResponse().addHeader(CLIENT_ID_HEADER, "client-id");
        mockWebServer.enqueue(response);

        okhttp3.Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(CLIENT_ID_HEADER, "different-client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header")
            .build();
        okHttpClient.newCall(request).execute();

        // Then the interceptor response will throw an exception because headers on both the request and the response
        // do not have the same exact values.
    }

    @Test
    public void intercept_withCustomHeaders_withEqualValuesInRequestAndResponseHeaders() throws IOException {
        // Given a client with a ResponseHeadersValidationInterceptor that considers a custom list of headers
        // additional to the mandatory headers used by Storage.
        List<String> headerNames = new ArrayList<>();
        headerNames.add("name1");
        headerNames.add("name2");
        headerNames.add("name3");

        ResponseHeadersValidationInterceptor normalizeEtagInterceptor = new ResponseHeadersValidationInterceptor(headerNames);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(normalizeEtagInterceptor);

        // When sending a request with said headers and its values and receiving a response that includes them as well.
        MockResponse response = new MockResponse()
            .addHeader(CLIENT_ID_HEADER, "client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header")
            .addHeader("name1", "value1")
            .addHeader("name2", "value2")
            .addHeader("name3", "value3");
        mockWebServer.enqueue(response);

        okhttp3.Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(CLIENT_ID_HEADER, "client-id")
            .addHeader(ENCRYPTION_KEY_SHA256_HEADER, "encryption-header")
            .addHeader("name1", "value1")
            .addHeader("name2", "value2")
            .addHeader("name3", "value3")
            .build();
        okHttpClient.newCall(request).execute();

        // Then the interceptor will not throw an exception because the headers on both the request and the response
        // have the same exact values.
    }
}
