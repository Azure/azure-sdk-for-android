// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.okhttp;

import com.azure.android.core.http.HttpClient;
import com.azure.android.core.test.http.HttpClientTests;
import com.azure.android.core.test.HttpClientTestsWireMockServer;
import com.github.tomakehurst.wiremock.WireMockServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class OkHttpHttpClientTests extends HttpClientTests {
    private static WireMockServer server;

    @BeforeAll
    public static void getWireMockServer() {
        server = HttpClientTestsWireMockServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void shutdownWireMockServer() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    protected int getWireMockPort() {
        return server.port();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new OkHttpAsyncHttpClientBuilder().build();
    }
}
