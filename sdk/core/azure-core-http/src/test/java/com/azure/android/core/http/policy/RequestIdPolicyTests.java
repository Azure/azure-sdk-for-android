// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.micro.util.CancellationToken;
import com.azure.android.core.micro.util.Context;
import com.azure.android.core.test.http.NoOpHttpClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RequestIdPolicyTests {
    private final HttpResponse mockResponse = new HttpResponse(null) {
        @Override
        public int getStatusCode() {
            return 500;
        }

        @Override
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public byte[] getBodyAsByteArray() {
            return new byte[0];
        }

        @Override
        public String getBodyAsString() {
            return null;
        }

        @Override
        public String getBodyAsString(Charset charset) {
            return null;
        }
    };

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    @Test
    public void newRequestIdForEachCall() throws Exception {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                String firstRequestId = null;
                @Override
                public void send(HttpRequest request, HttpCallback httpCallback) {
                    if (firstRequestId != null) {
                        String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                        Assertions.assertNotNull(newRequestId, "newRequestId should not be null");
                        Assertions.assertNotEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assertions.fail("The firstRequestId should not be null.");
                    }
                    httpCallback.onSuccess(mockResponse);
                }
            })
            .policies(new RequestIdPolicy())
            .build();

        CountDownLatch latch1 = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET,"http://localhost/", Context.NONE, CancellationToken.NONE),
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch1.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch1.countDown();
                    }
                }
            });
        awaitOnLatch(latch1, "newRequestIdForEachCall");

        CountDownLatch latch2 = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/", Context.NONE, CancellationToken.NONE),
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch2.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch2.countDown();
                    }
                }
            });
        awaitOnLatch(latch2, "newRequestIdForEachCall");
    }

    @Test
    public void sameRequestIdForRetry() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                String firstRequestId = null;

                @Override
                public void send(HttpRequest request, HttpCallback httpCallback) {
                    if (firstRequestId != null) {
                        String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                        Assertions.assertNotNull(newRequestId, "newRequestId should not be null");
                        Assertions.assertEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assertions.fail("The firstRequestId should not be null.");
                    }
                    httpCallback.onSuccess(mockResponse);

// TODO: anuchan add a test with misbehaving httpclient that signal twice and assert we throw meaningful error
// TODO: anuchan add a test that watch running calls when we schedule (i.e. ensure not holding the thread)
//
//                    if (firstRequestId != null) {
//                        String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
//                        Assertions.assertNotNull(newRequestId);
//                        Assertions.assertEquals(newRequestId, firstRequestId);
//                    }
//                    firstRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
//                    if (firstRequestId == null) {
//                        Assertions.fail();
//                    }
//                    httpCallback.onSuccess(mockResponse);
                }
            })
            .policies(new RequestIdPolicy()
                ,
                 new RetryPolicy(new FixedDelay(1, Duration.of(5, ChronoUnit.SECONDS)))
            )
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/", Context.NONE, CancellationToken.NONE),
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        awaitOnLatch(latch, "sameRequestIdForRetry");
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}
