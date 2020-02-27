package com.azure.android.core.http.interceptor;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class EnqueueMockResponse implements TestRule {
    private final MockWebServer mockWebServer;

    EnqueueMockResponse(MockWebServer mockWebServer) {
        this.mockWebServer = mockWebServer;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                mockWebServer.enqueue(new MockResponse());
                base.evaluate();
            }
        };
    }
}
