// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.common;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class EnqueueMockResponse implements TestRule {
    private final MockWebServer mockWebServer;

    public EnqueueMockResponse(MockWebServer mockWebServer) {
        this.mockWebServer = mockWebServer;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        MockResponse mockResponse = new MockResponse().setBody("Test body").setHeader("Content-Type", "text/html");

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                mockWebServer.enqueue(mockResponse);
                base.evaluate();
            }
        };
    }
}
