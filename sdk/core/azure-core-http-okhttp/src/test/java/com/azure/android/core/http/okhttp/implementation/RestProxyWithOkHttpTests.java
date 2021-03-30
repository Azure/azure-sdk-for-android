// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.okhttp.implementation;

import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.android.core.test.RestProxyTestsWireMockServer;
import com.azure.android.core.test.implementation.RestProxyTests;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class RestProxyWithOkHttpTests extends RestProxyTests {
    private static WireMockServer server;

    @BeforeAll
    public static void getWireMockServer() {
        server = RestProxyTestsWireMockServer.getRestProxyTestsServer();
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
