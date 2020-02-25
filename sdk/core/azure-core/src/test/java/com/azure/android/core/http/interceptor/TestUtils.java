package com.azure.android.core.http.interceptor;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

class TestUtils {
    static Request getSimpleRequest(MockWebServer mockWebServer) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .build();
    }

    static OkHttpClient buildOkHttpClientWithInterceptor(Interceptor interceptor) {
        return new OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .build();
    }

    static OkHttpClient buildOkHttpClientWithInterceptors(List<Interceptor> interceptors) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

        for (Interceptor interceptor : interceptors) {
            clientBuilder.addInterceptor(interceptor);
        }

        return clientBuilder.build();
    }
}
