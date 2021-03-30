// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.Context;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HostPolicyTests {
    @Test
    public void withNoPort() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost");
        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(createHttpRequest("ftp://www.example.com"),
            Context.NONE,
            CancellationToken.NONE,
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
        awaitOnLatch(latch, "withNoPort");
    }

    @Test
    public void withPort() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost:1234");
        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(createHttpRequest("ftp://www.example.com:1234"),
            Context.NONE,
            CancellationToken.NONE,
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
        awaitOnLatch(latch, "withPort");
    }

    private static HttpPipeline createPipeline(String host, String expectedUrl) {
        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new HostPolicy(host),
                (chain) -> {
                    assertEquals(expectedUrl, chain.getRequest().getUrl().toString());
                    chain.processNextPolicy(chain.getRequest());
                }
            )
            .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, url);
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}
