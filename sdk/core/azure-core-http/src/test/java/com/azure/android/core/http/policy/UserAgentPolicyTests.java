// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.test.http.MockHttpResponse;
import com.azure.android.core.test.http.NoOpHttpClient;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.Context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserAgentPolicyTests {
    @Test
    void testDefaultUserAgentString() {
        UAgentOrError uAgentOrError = new UAgentOrError();
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public void send(HttpRequest httpRequest,
                                 CancellationToken cancellationToken,
                                 HttpCallback httpCallback) {
                    uAgentOrError.userAgent = httpRequest.getHeaders().getValue("User-Agent");
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200));
                }
            })
            .policies(new UserAgentPolicy())
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE, CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        uAgentOrError.error = error;
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "testDefaultUserAgentString");

        if (uAgentOrError.error != null) {
            Assertions.fail(uAgentOrError.error);
        }
        Assertions.assertEquals("azsdk-android", uAgentOrError.userAgent);
    }

    @Test
    void testNoAppIdInUserAgentString() {
        UAgentOrError uAgentOrError = new UAgentOrError();
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public void send(HttpRequest httpRequest,
                                 CancellationToken cancellationToken,
                                 HttpCallback httpCallback) {
                    uAgentOrError.userAgent = httpRequest.getHeaders().getValue("User-Agent");
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200));
                }
            })
            .policies(new UserAgentPolicy(null, "azure-storage-blob", "12.0.0"))
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE, CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        uAgentOrError.error = error;
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "testNoAppIdInUserAgentString");

        if (uAgentOrError.error != null) {
            Assertions.fail(uAgentOrError.error);
        }
        Assertions.assertEquals("azsdk-android-azure-storage-blob/12.0.0 (null; null)",
            uAgentOrError.userAgent);
    }

    @Test
    void testAppIdNoSdkInfoInUserAgentString() {
        UAgentOrError uAgentOrError = new UAgentOrError();
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public void send(HttpRequest httpRequest,
                                 CancellationToken cancellationToken,
                                 HttpCallback httpCallback) {
                    uAgentOrError.userAgent = httpRequest.getHeaders().getValue("User-Agent");
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200));
                }
            })
            .policies(new UserAgentPolicy("myappId", null, null))
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE, CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        uAgentOrError.error = error;
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "testAppIdNoSdkInfoInUserAgentString");

        if (uAgentOrError.error != null) {
            Assertions.fail(uAgentOrError.error);
        }
        Assertions.assertEquals("myappId azsdk-android-null/null (null; null)",
            uAgentOrError.userAgent);
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }

    private static class UAgentOrError {
        public String userAgent;
        public Throwable error;
    }
}
