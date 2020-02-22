package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class RequestIdInterceptorTest {
    @Rule
    public final MockWebServer mockWebServer = new MockWebServer(); // Server is started automatically

    @Test
    public void requestIdHeaderIsPopulated() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse());

        OkHttpClient okHttpClient = new OkHttpClient()
            .newBuilder()
            .addInterceptor(new RequestIdInterceptor())
            .build();
        Request request = new Request
            .Builder()
            .url(mockWebServer.url("/"))
            .build();

        okHttpClient.newCall(request).execute();

        String header = mockWebServer.takeRequest().getHeader(HttpHeader.CLIENT_REQUEST_ID);

        Assert.assertNotNull(header);
    }
}
