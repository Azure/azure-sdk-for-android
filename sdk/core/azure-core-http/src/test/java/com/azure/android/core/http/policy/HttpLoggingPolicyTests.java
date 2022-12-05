// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.RequestContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class contains tests for {@link HttpLoggingPolicy}.
 */
public class HttpLoggingPolicyTests {
    private static final String REDACTED = "REDACTED";
    private static final RequestContext CONTEXT = new RequestContext("caller-method", HttpLoggingPolicyTests.class.getName());

    private PrintStream originalSystemOut;
    private ByteArrayOutputStream logCaptureStream;

    @BeforeEach
    public void prepareForTest() {
        /*
         * DefaultLogger uses System.out to log. Inject a custom PrintStream to log into for the duration of the test to
         * capture the log messages.
         */
        originalSystemOut = System.out;
        logCaptureStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void cleanupAfterTest() throws IOException {
        // Reset System.err to the original PrintStream.
        System.setOut(originalSystemOut);
        logCaptureStream.close();
    }

    /**
     * Tests that a query string will be properly redacted before it is logged.
     */
    @ParameterizedTest
    @MethodSource("redactQueryParametersSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void redactQueryParameters(String requestUrl, String expectedQueryString,
        Set<String> allowedQueryParameters) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BASIC)
                .setAllowedQueryParamNames(allowedQueryParameters)))
            .httpClient(new NoOpHttpClient())
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        // pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl), CONTEXT, new HttpCallback() {..})
        pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl),
            RequestContext.NONE,
            CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        assertTrue(false, "unexpected call to pipeline::send onError" + error.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        awaitOnLatch(latch, "redactQueryParameters");

        assertTrue(convertOutputStreamToString(logCaptureStream).contains(expectedQueryString));
    }

    private static Stream<Arguments> redactQueryParametersSupplier() {
        String requestUrl = "https://localhost?sensitiveQueryParameter=sensitiveValue&queryParameter=value";

        String expectedFormat = "sensitiveQueryParameter=%s&queryParameter=%s";
        String fullyRedactedQueryString = String.format(expectedFormat, REDACTED, REDACTED);
        String sensitiveRedactionQueryString = String.format(expectedFormat, REDACTED, "value");
        String fullyAllowedQueryString = String.format(expectedFormat, "sensitiveValue", "value");

        Set<String> allQueryParameters = new HashSet<>();
        allQueryParameters.add("sensitiveQueryParameter");
        allQueryParameters.add("queryParameter");

        return Stream.of(
            // All query parameters should be redacted.
            Arguments.of(requestUrl, fullyRedactedQueryString, new HashSet<String>()),

            // Only the sensitive query parameter should be redacted.
            Arguments.of(requestUrl, sensitiveRedactionQueryString, Collections.singleton("queryParameter")),

            // No query parameters are redacted.
            Arguments.of(requestUrl, fullyAllowedQueryString, allQueryParameters)
        );
    }

    /**
     * Tests that logging the request body doesn't consume the stream before it is sent over the network.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void validateLoggingDoesNotChangeRequest(byte[] content, byte[] data, int contentLength) {
        final String requestUrl = "https://test.com";
        HttpHeaders requestHeaders = new HttpHeaders()
            .put("Content-Type", "application/json")
            .put("Content-Length", Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(new HttpClient() {
                @Override
                public HttpCallDispatcher getHttpCallDispatcher() {
                    return new HttpCallDispatcher();
                }

                @Override
                public void send(HttpRequest httpRequest,
                                 CancellationToken cancellationToken,
                                 HttpCallback httpCallback) {
                    assertArrayEquals(data, httpRequest.getBody());
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200));
                }
            })
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        // pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl, requestHeaders, content), CONTEXT)
        pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl, requestHeaders, content),
            RequestContext.NONE,
            CancellationToken.NONE,
            new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        assertTrue(false, "unexpected call to pipeline::send onError" + error.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        awaitOnLatch(latch, "validateLoggingDoesNotChangeRequest");

        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    /**
     * Tests that logging the response body doesn't consume the stream before it is returned from the service call.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void validateLoggingDoesNotChangeResponse(byte[] content, byte[] data, int contentLength) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com");
        HttpHeaders responseHeaders = new HttpHeaders()
            .put("Content-Type", "application/json")
            .put("Content-Length", Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(new HttpClient() {
                @Override
                public HttpCallDispatcher getHttpCallDispatcher() {
                    return new HttpCallDispatcher();
                }

                @Override
                public void send(HttpRequest httpRequest,
                                 CancellationToken cancellationToken,
                                 HttpCallback httpCallback) {
                    httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200, responseHeaders, content));
                }
            })
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        // pipeline.send(request, CONTEXT)
        pipeline.send(request, RequestContext.NONE, CancellationToken.NONE, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                try {
                    assertArrayEquals(data, response.getBodyAsByteArray());
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Throwable error) {
                try {
                    assertTrue(false, "unexpected call to pipeline::send onError" + error.getMessage());
                } finally {
                    latch.countDown();
                }
            }
        });

        awaitOnLatch(latch, "validateLoggingDoesNotChangeResponse");
        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    private static Stream<Arguments> validateLoggingDoesNotConsumeSupplier() {
        byte[] data = "this is a test".getBytes(StandardCharsets.UTF_8);
        byte[] repeatingData = new byte[data.length * 3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(data, 0, repeatingData, i * data.length, data.length);
        }

        return Stream.of(
            Arguments.of(data, data, data.length)
        );
    }

//    @ParameterizedTest(name = "[{index}] {displayName}")
//    @EnumSource(value = HttpLogDetailLevel.class, mode = EnumSource.Mode.INCLUDE,
//        names = { "BODY_AND_HEADERS" })
//    @ResourceLock("SYSTEM_OUT")
//    public void loggingIncludesRetryCount(HttpLogDetailLevel logLevel) {
//        AtomicInteger requestCount = new AtomicInteger();
//        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com");
//
//        HttpPipeline pipeline = new HttpPipelineBuilder()
//            .policies(RetryPolicy.withFixedDelay(2, Duration.ofSeconds(1)),
//                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(logLevel)))
//            .httpClient(new HttpClient() {
//                @Override
//                public HttpCallDispatcher getHttpCallDispatcher() {
//                    return new HttpCallDispatcher();
//                }
//
//                @Override
//                public void send(HttpRequest httpRequest, HttpCallback httpCallback) {
//                    if (requestCount.getAndIncrement() == 0) {
//                        httpCallback.onError(new RuntimeException("Try again!"));
//                    } else {
//                        httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200));
//                    }
//                }
//            })
//            .build();
//
//        CountDownLatch latch = new CountDownLatch(1);
//        // pipeline.send(request, CONTEXT)
//        pipeline.send(request, new HttpCallback() {
//            @Override
//            public void onSuccess(HttpResponse response) {
//                try {
//                    assertEquals(200, response.getStatusCode());
//                } finally {
//                    latch.countDown();
//                }
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                latch.countDown();
//            }
//        });
//
//        awaitOnLatch(latch, "loggingIncludesRetryCount");
//
//        String logString = convertOutputStreamToString(logCaptureStream);
//        assertTrue(logString.contains("Try count: 1"));
//        assertTrue(logString.contains("Try count: 2"));
//    }

    private static String convertOutputStreamToString(ByteArrayOutputStream stream) {
        try {
            return stream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}