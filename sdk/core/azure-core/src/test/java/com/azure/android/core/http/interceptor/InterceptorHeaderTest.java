package com.azure.android.core.http.interceptor;

import org.junit.Before;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class InterceptorHeaderTest {
    static final MockWebServer mockWebServer = new MockWebServer(); // Server is started automatically
    static OkHttpClient okHttpClient;
    Request request;

    @Before
    public void createAndExecuteRequest() throws IOException {
        mockWebServer.enqueue(new MockResponse());

        request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .build();

        okHttpClient.newCall(request).execute();
    }

    static OkHttpClient buildOkHttpClientWithInterceptor(Interceptor interceptor) {
        return new OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .build();
    }

    OkHttpClient buildOkHttpClientWithInterceptors(List<Interceptor> interceptors) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

        for (Interceptor interceptor : interceptors) {
            clientBuilder.addInterceptor(interceptor);
        }

        return clientBuilder.build();
    }
}
