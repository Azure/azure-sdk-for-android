package com.azure.android.core.http.interceptor;

import org.junit.Before;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequest;

public abstract class InterceptorTest {
    final MockWebServer mockWebServer = new MockWebServer(); // Server is started automatically
    final OkHttpClient okHttpClient;
    Request request;

    InterceptorTest(Interceptor interceptor) {
        okHttpClient = buildOkHttpClientWithInterceptor(interceptor);
    }

    @Before
    public void createAndExecuteRequest() throws IOException {
        mockWebServer.enqueue(new MockResponse());
        request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();
    }
}
