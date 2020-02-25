package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;

public class AddDateInterceptorTest extends InterceptorTest {
    public AddDateInterceptorTest() {
        super(new AddDateInterceptor());
    }

    @Test
    public void dateHeader_isPopulated() throws InterruptedException {
        // Given a client with a AddDateInterceptor.
        // When executing a request.
        // Then the 'Date' header should be populated.
        Assert.assertNotNull(mockWebServer.takeRequest().getHeader(HttpHeader.DATE));
    }

    @Test
    public void dateHeader_isOverwritten() throws IOException, InterruptedException {
        // Given a client with a AddDateInterceptor that has executed a request...
        String date = mockWebServer.takeRequest().getHeader(HttpHeader.DATE);
        // ...where the 'Date' header is populated.
        Assert.assertNotNull(date);

        mockWebServer.enqueue(new MockResponse());

        // When creating a new client based on the aforementioned one with a new AddDateInterceptor...
        OkHttpClient newClient = okHttpClient.newBuilder()
            .addInterceptor(new AddDateInterceptor())
            .build();
        // ...and executing a request with the same requestId.
        Request newRequest = request.newBuilder()
            .header(HttpHeader.DATE, date)
            .build();

        Thread.sleep(1000);

        newClient.newCall(newRequest).execute();

        Headers headers = mockWebServer.takeRequest().getHeaders();

        // Then there should be only one 'Date' header in the request...
        Assert.assertEquals(1, headers.values(HttpHeader.DATE).size());

        // ...with a different value as the one in the first request.
        Assert.assertNotEquals(date, headers.get(HttpHeader.DATE));
    }
}
