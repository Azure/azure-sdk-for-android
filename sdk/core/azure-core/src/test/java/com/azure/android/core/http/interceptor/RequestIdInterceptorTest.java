package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequest;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeader;
public class RequestIdInterceptorTest {
    private final MockWebServer mockWebServer = new MockWebServer(); // Server is started automatically
    private final OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(new RequestIdInterceptor());

    @Test
    public void requestIdHeader_isPopulated() throws InterruptedException, IOException {
        mockWebServer.enqueue(new MockResponse());
        Request request = getSimpleRequest(mockWebServer);

        // Given a client with a RequestIdInterceptor.

        // When executing a request.
        okHttpClient.newCall(request).execute();
        // Then the 'x-ms-client-request-id' header should be populated.
        Assert.assertNotNull(mockWebServer.takeRequest().getHeader(HttpHeader.CLIENT_REQUEST_ID));
    }

    @Test
    public void requestIdHeader_isNotOverwritten() throws InterruptedException, IOException {
        String requestId = UUID.randomUUID().toString();
        mockWebServer.enqueue(new MockResponse());

        // Given a request where the 'x-ms-client-request-id' header is already populated.
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.CLIENT_REQUEST_ID, requestId);

        // When executing said request.
        okHttpClient.newCall(request).execute();

        Headers headers = mockWebServer.takeRequest().getHeaders();

        // Then there should be only one 'x-ms-client-request-id' header in the request sent...
        Assert.assertEquals(1, headers.values(HttpHeader.CLIENT_REQUEST_ID).size());
        // ...with an unchanged value.
        Assert.assertEquals(requestId, headers.get(HttpHeader.CLIENT_REQUEST_ID));
    }
}
