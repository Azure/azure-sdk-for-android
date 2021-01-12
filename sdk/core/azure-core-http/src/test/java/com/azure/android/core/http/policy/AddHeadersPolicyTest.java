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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class AddHeadersPolicyTest {

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
        public byte[] getBodyAsByteArray() {
            return new byte[0];
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public String getBodyAsString() {
            return new String(new byte[0]);
        }

        @Override
        public String getBodyAsString(Charset charset) {
            return new String(new byte[0], charset);
        }
    };

    @Test
    public void clientProvidedMultipleHeader() throws Exception {
        String customRequestId = "request-id-value";
        final HttpHeaders headers = new HttpHeaders();
        headers.put("x-ms-client-request-id", customRequestId);
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public void send(HttpRequest httpRequest, HttpCallback httpCallback) {
                    Assertions.assertEquals(httpRequest.getHeaders().getValue("x-ms-client-request-id"), customRequestId);
                    Assertions.assertEquals(httpRequest.getHeaders().getValue("my-header1"), "my-header1-value");
                    Assertions.assertEquals(httpRequest.getHeaders().getValue("my-header2"), "my-header2-value");
                    httpCallback.onSuccess(mockResponse);
                }
            })
            .policies(new AddHeadersPolicy(headers))
            .policies(new RequestIdPolicy())
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET,"http://localhost/", Context.NONE, CancellationToken.NONE),
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
        awaitOnLatch(latch, "clientProvidedMultipleHeader");



    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}
