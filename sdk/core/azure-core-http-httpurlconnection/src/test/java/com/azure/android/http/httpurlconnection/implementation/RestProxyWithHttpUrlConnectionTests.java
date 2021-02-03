// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.http.httpurlconnection.implementation;

import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.httpurlconnection.HttpUrlConnectionAsyncHttpClientBuilder;
import com.azure.android.core.test.RestProxyTestsWireMockServer;
import com.azure.android.core.test.implementation.RestProxyTests;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RestProxyWithHttpUrlConnectionTests extends RestProxyTests {
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
        return new HttpUrlConnectionAsyncHttpClientBuilder().build();
    }

    @Test
    @Override
    @Disabled("Disabled: HttpUrlConnection seems not supporting PATCH out of the box")
    public void patchRequest() {
        // ref: https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch
    }
}
