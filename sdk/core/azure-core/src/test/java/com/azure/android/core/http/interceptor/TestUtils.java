package com.azure.android.core.http.interceptor;

import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

final class TestUtils {
    static Request getSimpleRequest(MockWebServer mockWebServer) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .build();
    }

    static Request getSimpleRequestWithHeader(MockWebServer mockWebServer,
                                              String key,
                                              String value) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(key, value)
            .build();
    }

    static Request getSimpleRequestWithHeaders(MockWebServer mockWebServer,
                                               Map<String, String> headers) {
        Request.Builder builder = new Request.Builder()
            .url(mockWebServer.url("/"));

        for (Map.Entry header : headers.entrySet()) {
            builder.addHeader((String) header.getKey(), (String) header.getValue());
        }

        return builder.build();
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
