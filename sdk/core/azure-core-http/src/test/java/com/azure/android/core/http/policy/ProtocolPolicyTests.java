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
import com.azure.android.core.test.http.NoOpHttpClient;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ProtocolPolicyTests {

    @Test
    public void withOverwrite() {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(createHttpRequest("http://www.bing.com"),
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
        awaitOnLatch(latch, "withOverwrite");
    }

    @Test
    public void withNoOverwrite() {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(createHttpRequest("https://www.bing.com"),
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
        awaitOnLatch(latch, "withNoOverwrite");
    }

    private static HttpPipeline createPipeline(String protocol, String expectedUrl) {
        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ProtocolPolicy(protocol, true),
                (chain) -> {
                    assertEquals(expectedUrl, chain.getRequest().getUrl().toString());
                    chain.processNextPolicy(chain.getRequest());
                })
            .build();
    }

    private static HttpPipeline createPipeline(String protocol, boolean overwrite, String expectedUrl) {
        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ProtocolPolicy(protocol, overwrite),
                (chain) -> {
                    assertEquals(expectedUrl, chain.getRequest().getUrl().toString());
                    chain.processNextPolicy(chain.getRequest());
                })
            .build();
    }

    private static HttpRequest createHttpRequest(String url) {
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
