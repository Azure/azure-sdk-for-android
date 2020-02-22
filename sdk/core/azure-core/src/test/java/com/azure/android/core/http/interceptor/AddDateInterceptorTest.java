package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.mockwebserver.MockResponse;

public class AddDateInterceptorTest extends InterceptorHeaderTest {
    @BeforeClass
    public static void setUp() {
        okHttpClient = buildOkHttpClientWithInterceptor(new AddDateInterceptor());
    }

    @Test
    public void dateHeader_isPopulated() throws InterruptedException {
        // Given a client with a AddDateInterceptor.
        // When executing a request.
        // Then the 'Date' header should be populated.
        Assert.assertNotNull(mockWebServer.takeRequest().getHeader(HttpHeader.DATE));
    }

    @Test
    public void requestIdHeader_isOverwritten() throws IOException, InterruptedException {
        // Given a client with a AddDateInterceptor that has executed a request.
        String date = mockWebServer.takeRequest().getHeader(HttpHeader.DATE);

        mockWebServer.enqueue(new MockResponse());

        // When creating a new client based on the aforementioned one with a new AddDateInterceptor...
        okHttpClient = okHttpClient.newBuilder()
            .addInterceptor(new AddDateInterceptor())
            .build();
        // ...and executing a request with the same requestId.
        request = request.newBuilder()
            .header(HttpHeader.DATE, date)
            .build();

        Thread.sleep(1000);

        okHttpClient.newCall(request).execute();

        Headers headers = mockWebServer.takeRequest().getHeaders();

        // Then there should be only one 'Date' header in the request...
        Assert.assertEquals(1, headers.values(HttpHeader.DATE).size());

        // ...with a different value as the one in the first request.
        Assert.assertNotEquals(date, headers.get(HttpHeader.DATE));
    }
}
