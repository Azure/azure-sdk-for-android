// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertFalse;

public final class TestUtils {
    private TestUtils() {
        // Empty constructor to prevent instantiation of this class.
    }

    public static OkHttpClient buildOkHttpClientWithInterceptor(Interceptor interceptor) {
        return new OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .build();
    }

    public static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(method + " didn't produce any result.", true);
        }
    }

    public static OkHttpClient buildOkHttpClientWithInterceptors(List<Interceptor> interceptors) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

        for (Interceptor interceptor : interceptors) {
            clientBuilder.addInterceptor(interceptor);
        }

        return clientBuilder.build();
    }

    public static Request getSimpleRequest(MockWebServer mockWebServer) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .build();
    }

    public static Request getSimpleRequestWithHeader(MockWebServer mockWebServer,
                                              String name,
                                              String value) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(name, value)
            .build();
    }

    public static Request getSimpleRequestWithHeaders(MockWebServer mockWebServer,
                                               Map<String, String> headers) {
        Request.Builder builder = new Request.Builder()
            .url(mockWebServer.url("/"));

        for (Map.Entry header : headers.entrySet()) {
            builder.addHeader((String) header.getKey(), (String) header.getValue());
        }

        return builder.build();
    }

    public static Request getSimpleRequestWithQueryParam(MockWebServer mockWebServer,
                                                  String name,
                                                  String value) {
        String path = "/?" + name + "=" + value;

        return new Request.Builder()
            .url(mockWebServer.url(path))
            .build();
    }

    public static Request getSimpleRequestWithQueryParams(MockWebServer mockWebServer,
                                                   Map<String, String> queryParams) {
        StringBuilder pathStringBuilder = new StringBuilder("/?");

        for (Map.Entry queryParam : queryParams.entrySet()) {
            pathStringBuilder.append(queryParam.getKey())
                .append("=")
                .append(queryParam.getValue())
                .append("&");
        }

        pathStringBuilder.setLength(pathStringBuilder.length() - 1);

        return new Request.Builder()
            .url(mockWebServer.url(pathStringBuilder.toString()))
            .build();
    }

    public static String getStackTraceString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();

        return stringWriter.toString();
    }
}
