// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequest;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeader;
public class AddDateInterceptorTest {
    private static final String TEST_DATE = "Tue, 25 Feb 2020 00:59:22 GMT";
    private final MockWebServer mockWebServer = new MockWebServer();
    private final OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(new AddDateInterceptor());

    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Test
    public void dateHeader_isPopulated_onRequest() throws InterruptedException, IOException {
        // Given a client with a AddDateInterceptor.

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'Date' header should be populated.
        Assert.assertNotNull(mockWebServer.takeRequest().getHeader(HttpHeader.DATE));
    }

    @Test
    public void dateHeader_isOverwritten_onRequest() throws InterruptedException, IOException {
        // Given a request where the 'Date' header is already populated and a client with a AddDateInterceptor.
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.DATE, TEST_DATE);

        // When executing said request.
        okHttpClient.newCall(request).execute();

        Headers headers = mockWebServer.takeRequest().getHeaders();

        // Then there should be only one 'Date' header in the request sent...
        Assert.assertEquals(1, headers.values(HttpHeader.DATE).size());
        // ...with a changed value.
        Assert.assertNotEquals(TEST_DATE, headers.get(HttpHeader.DATE));
    }
}
