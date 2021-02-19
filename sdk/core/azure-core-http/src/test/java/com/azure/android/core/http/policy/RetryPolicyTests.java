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
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.Context;
import com.azure.android.core.test.http.MockHttpResponse;
import com.azure.android.core.test.http.NoOpHttpClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RetryPolicyTests {

    @Test
    public void retryEndOn501() {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                // Send 408, 500, 502, all retried, with a 501 ending
                private final int[] codes = new int[]{408, 500, 502, 501};
                private int count = 0;

                @Override
                public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, codes[count++]));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(3, Duration.of(0, ChronoUnit.MILLIS))))
            .build();


        final HttpResponse[] httpResponse = new HttpResponse[1];
        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE, CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    httpResponse[0] = response;
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
        awaitOnLatch(latch, "retryEndOn501");

        assertNotNull(httpResponse[0]);
        assertEquals(501, httpResponse[0].getStatusCode());
    }

    @Test
    public void retryMax() {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
                    Assertions.assertTrue(count++ < maxRetries);
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 500));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.of(0, ChronoUnit.MILLIS))))
            .build();

        final HttpResponse[] httpResponse = new HttpResponse[1];
        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE, CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    httpResponse[0] = response;
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
        awaitOnLatch(latch, "retryMax");

        assertNotNull(httpResponse[0]);
        assertEquals(500, httpResponse[0].getStatusCode());
    }

    @Test
    public void fixedDelayRetry() {
        final int maxRetries = 5;
        final long delayMillis = 500;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                @Override
                public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
                    if (count > 0) {
                        Assertions.assertTrue(System.currentTimeMillis() >= previousAttemptMadeAt + delayMillis);
                    }
                    Assertions.assertTrue(count++ < maxRetries);
                    previousAttemptMadeAt = System.currentTimeMillis();
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 500));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(delayMillis))))
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
                    latch.countDown();
                }
            });
        awaitOnLatch(latch, "fixedDelayRetry");
    }

    @Test
    public void exponentialDelayRetry() {
        final int maxRetries = 5;
        final long baseDelayMillis = 100;
        final long maxDelayMillis = 1000;
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(maxRetries, Duration.ofMillis(baseDelayMillis),
            Duration.ofMillis(maxDelayMillis));
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                @Override
                public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
                    if (count > 0) {
                        long requestMadeAt = System.currentTimeMillis();
                        long expectedToBeMadeAt =
                            previousAttemptMadeAt + ((1 << (count - 1)) * (long) (baseDelayMillis * 0.95));
                        Assertions.assertTrue(requestMadeAt >= expectedToBeMadeAt);
                    }
                    Assertions.assertTrue(count++ < maxRetries);
                    previousAttemptMadeAt = System.currentTimeMillis();
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 503));
                }
            })
            .policies(new RetryPolicy(exponentialBackoff))
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
                    latch.countDown();
                }
            });
        awaitOnLatch(latch, "exponentialDelayRetry");
    }

    @Test
    public void retryConsumesBody() {
        final AtomicInteger bodyConsumptionCount = new AtomicInteger();
        final InputStream errorBody = new ByteArrayInputStream("Should be consumed".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                bodyConsumptionCount.incrementAndGet();
                super.close();
            }
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))))
            .httpClient(new NoOpHttpClient() {
                @Override
                public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
                    httpCallback.onSuccess(new HttpResponse(httpRequest) {
                        @Override
                        public int getStatusCode() {
                            return 503;
                        }

                        @Override
                        public String getHeaderValue(String name) {
                            return getHeaders().getValue(name);
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            return new HttpHeaders();
                        }

                        @Override
                        public InputStream getBody() {
                            return errorBody;
                        }

                        @Override
                        public byte[] getBodyAsByteArray() {
                            return collectBytesInInputStream(getBody());
                        }

                        @Override
                        public String getBodyAsString() {
                            return getBodyAsString(StandardCharsets.UTF_8);
                        }

                        @Override
                        public String getBodyAsString(Charset charset) {
                            return new String(getBodyAsByteArray(), charset);
                        }

                        @Override
                        public void close() {
                            try {
                                errorBody.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            })
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        pipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com"), Context.NONE, CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    latch.countDown();
                }
            });
        awaitOnLatch(latch, "retryConsumesBody");
        assertEquals(2, bodyConsumptionCount.get());
    }


    private static byte[] collectBytesInInputStream(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return buffer.toByteArray();
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}
