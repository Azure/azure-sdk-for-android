package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;

public class RequestIdInterceptorTest extends InterceptorTest {
    public RequestIdInterceptorTest() {
        super(new RequestIdInterceptor());
    }

    @Test
    public void requestIdHeader_isPopulated() throws InterruptedException {
        // Given a client with a RequestIdInterceptor.
        // When executing a request.
        // Then the 'x-ms-client-request-id' header should be populated.
        Assert.assertNotNull(mockWebServer.takeRequest().getHeader(HttpHeader.CLIENT_REQUEST_ID));
    }

    @Test
    public void requestIdHeader_isNotOverwritten() throws IOException, InterruptedException {
        // Given a client with a RequestIdInterceptor that has executed a request...
        String requestId = mockWebServer.takeRequest().getHeader(HttpHeader.CLIENT_REQUEST_ID);
        // ...where the 'x-ms-client-request-id' header is populated.
        Assert.assertNotNull(requestId);

        mockWebServer.enqueue(new MockResponse());

        // When creating a new client based on the aforementioned one with a new RequestIdInterceptor...
        OkHttpClient newClient = okHttpClient.newBuilder()
            .addInterceptor(new RequestIdInterceptor())
            .build();
        // ...and executing a request with the same requestId.
        Request newRequest = request.newBuilder()
            .header(HttpHeader.CLIENT_REQUEST_ID, requestId)
            .build();
        newClient.newCall(newRequest).execute();

        Headers headers = mockWebServer.takeRequest().getHeaders();

        // Then there should be only one 'x-ms-client-request-id' header in the request...
        Assert.assertEquals(1, headers.values(HttpHeader.CLIENT_REQUEST_ID).size());

        // ...with the same value as the one in the first request.
        Assert.assertEquals(requestId, headers.get(HttpHeader.CLIENT_REQUEST_ID));
    }
}
