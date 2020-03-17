package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.BufferedSink;

import static com.azure.android.core.http.interceptor.LoggingInterceptor.MAX_BODY_LOG_SIZE;
import static com.azure.android.core.http.interceptor.LoggingInterceptor.REDACTED_PLACEHOLDER;
import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequest;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeader;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeaders;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithQueryParam;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithQueryParams;
import static com.azure.android.core.http.interceptor.TestUtils.getStackTraceString;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_DEBUG;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_INFO;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_WARNING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoggingInterceptorTest {
    private final MockWebServer mockWebServer = new MockWebServer();
    private final TestClientLogger testClientLogger = new TestClientLogger();
    private final OkHttpClient okHttpClient =
        buildOkHttpClientWithInterceptor(new LoggingInterceptor(new LogOptions(), testClientLogger));

    // MockResponse with Content-type: text/html, Content-Length: 9 and body 'Test body'.
    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Test
    public void requestId_isLoggedFirstAtInfoLevel_onRequestWithRequestId() throws IOException {
        // Given a request where the 'x-ms-client-request-id' header is populated and a client with a
        // LoggingInterceptor.
        String requestId = UUID.randomUUID().toString();
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.CLIENT_REQUEST_ID, requestId);

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = logs.get(0).getKey();
        String firstLogMessageOnRequest = logs.get(0).getValue();

        // Then the first message logged  for the request should include the requestId...
        assertEquals("--> [" + requestId + "]", firstLogMessageOnRequest);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void requestId_isLoggedLastAtInfoLevel_onRequestWithRequestId() throws IOException {
        // Given a request where the 'x-ms-client-request-id' header is populated and a client with a
        // LoggingInterceptor.
        String requestId = UUID.randomUUID().toString();
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.CLIENT_REQUEST_ID, requestId);

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String lastLogMessageOnRequest = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("--> [END")) {
                logLevel = entry.getKey();
                lastLogMessageOnRequest = message;

                break;
            }
        }

        // Then the first message logged for the request should include the requestId...
        assertEquals("--> [END " + requestId + "]", lastLogMessageOnRequest);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void headers_areRedactedAtDebugLevel_onRequestWithNoAllowedHeaders() throws IOException {
        // Given a request where at least one header is populated...
        String testHeader = "Test-Header";
        Request request = getSimpleRequestWithHeader(mockWebServer, testHeader, "Test Value");
        // ...and a client with a LoggingInterceptor with an empty allowed headers list.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(null, testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When sending said request.
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String headerLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith(testHeader + ": ")) {
                logLevel = entry.getKey();
                headerLogMessage = message;
            }
        }

        // Then the header should be logged with its value redacted...
        assertEquals(testHeader + ": " + REDACTED_PLACEHOLDER, headerLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void nonAllowedHeaders_areRedactedAtDebugLevel_onRequest() throws IOException {
        // Given a request where at least one non-allowed header and at least one allowed header are populated...
        String testHeader = "Test-Header";
        Map<String, String> headers = new HashMap<>();
        headers.put(testHeader, "Test value");
        Request request = getSimpleRequestWithHeaders(mockWebServer, headers);
        // ...and a client with a LoggingInterceptor with a list of allowed headers.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(new LogOptions(), testClientLogger);
        OkHttpClient allowedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When sending said request.
        allowedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int testHeaderLogLevel = 0;
        String testHeaderLogMessage = null;
        int contentTypeHeaderLogLevel = 0;
        String contentTypeHeaderLogMessage = null; // Content-Type is obtained from the response body.

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("Content-Type: ")) {
                contentTypeHeaderLogLevel = entry.getKey();
                contentTypeHeaderLogMessage = message;
            }

            if (message.startsWith(testHeader + ": ")) {
                testHeaderLogLevel = entry.getKey();
                testHeaderLogMessage = message;
            }
        }

        // Then the non-allowed header should be logged with its value redacted...
        assertEquals(testHeader + ": " + REDACTED_PLACEHOLDER, testHeaderLogMessage);
        // ...and the allowed header should be logged with its value intact...
        assertEquals("Content-Type: text/html", contentTypeHeaderLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, testHeaderLogLevel);
        assertEquals(LOG_LEVEL_DEBUG, contentTypeHeaderLogLevel);
    }

    @Test
    public void queryParams_areRedactedAtInfoLevel_onRequestWithNoAllowedQueryParams() throws IOException {
        // Given a request with a URL where there is at least one query parameter...
        String paramName = "testParam";
        Request request = getSimpleRequestWithQueryParam(mockWebServer, paramName, "testValue");
        // ...and a client with a LoggingInterceptor with a list of allowed query parameters.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(null, testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When sending said request.
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String urlLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("GET /")) {
                logLevel = entry.getKey();
                urlLogMessage = message;

                break;
            }
        }

        // Then the query parameter should be logged as part of the URL with its value redacted...
        assertEquals("GET /?" + paramName + "=" + REDACTED_PLACEHOLDER, urlLogMessage);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void nonAllowedQueryParams_areRedactedAtInfoLevel_onRequest() throws IOException {
        // Given a request with a URL where there is at least one non-allowed query parameter and at least one allowed
        // query parameter...
        String allowedQueryParamName = "allowedParam";
        String allowedQueryParamValue = "normalValue";
        String nonAllowedQueryParamName = "nonAllowedParam";
        String nonAllowedQueryParamValue = "valueToRedact";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(allowedQueryParamName, allowedQueryParamValue);
        queryParams.put(nonAllowedQueryParamName, nonAllowedQueryParamValue);
        Request request = getSimpleRequestWithQueryParams(mockWebServer, queryParams);
        // ...and a client with a LoggingInterceptor with an empty allowed query parameters list.
        Set<String> allowedQueryParams = new HashSet<>();
        allowedQueryParams.add(allowedQueryParamName);
        LogOptions logOptions = new LogOptions().setAllowedQueryParamNames(allowedQueryParams);
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(logOptions, testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When sending said request.
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String urlLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("GET /")) {
                logLevel = entry.getKey();
                urlLogMessage = message;

                break;
            }
        }

        // Then the query parameter should be logged as part of the URL with its value redacted...
        assertEquals("GET /?" + allowedQueryParamName + "=" + allowedQueryParamValue + "&" +
            nonAllowedQueryParamName + "=" + REDACTED_PLACEHOLDER, urlLogMessage); // Query params are ordered alphabetically by the OkHttp when creating the request
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void internalFailure_isLoggedAtWarningLevel_onRequest() {
        // Given a request that will cause an error when reading its content and a client with a LoggingInterceptor.
        Request request = new Request.Builder().url(mockWebServer.url("/")).put(new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.get("Bad Charset");
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public void writeTo(BufferedSink sink) {
                // Do nothing
            }
        }).build();

        try {
            // When sending said request an error will occur.
            okHttpClient.newCall(request).execute();
        } catch (Exception e) {
            List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
            int logLevel = 0;
            String errorLogMessage = null;

            for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
                String message = entry.getValue();

                if (message.startsWith("OPERATION FAILED: ")) {
                    logLevel = entry.getKey();
                    errorLogMessage = message;

                    break;
                }
            }

            // Then the error message will be logged...
            assertEquals("OPERATION FAILED: " + getStackTraceString(e), errorLogMessage);
            // ...at the WARNING log level.
            assertEquals(LOG_LEVEL_WARNING, logLevel);
        }
    }

    @Test
    public void requestOperationAndHost_areLoggedAtInfoLevel_onRequest() throws IOException {
        // Given a request for a given URL and a client with a LoggingInterceptor.
        String testPath = "/testPath";
        HttpUrl testUrl = mockWebServer.url(testPath);
        Request request = new Request.Builder()
            .url(testUrl)
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String operationLogMessage = null;
        String hostLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("GET /")) {
                logLevel = entry.getKey();
                operationLogMessage = message;
            }

            if (message.startsWith("Host: ")) {
                logLevel = entry.getKey();
                hostLogMessage = message;

                break;
            }
        }

        // Then the operation executed...
        assertEquals("GET " + testPath, operationLogMessage);
        // ...and the host the request was sent to should be logged...
        assertEquals("Host: " + testUrl.scheme() + "://" + testUrl.host(), hostLogMessage);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void requestBody_isLoggedAtDebugLevel_onRequest() throws IOException {
        // Given a request with a body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then the request body should be logged...
        assertEquals(testBody, bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequestWithEncodedBody() throws IOException {
        // Given a request with an encoded body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .addHeader("Content-Encoding", "gzip")
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Encoding: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(encoded body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void requestBody_isLoggedAtDebugLevel_onRequestWithNonEncodedBody() throws IOException {
        // Given a request with a non-encoded body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .addHeader("Content-Encoding", "identity")
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Encoding: ")) {
                bodyComingNext = true;
            }
        }

        // Then the request body should be logged...
        assertEquals(testBody, bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequestWithNonInlineBody() throws IOException {
        // Given a request with an attached body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .addHeader("Content-Disposition", "attached")
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Disposition: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(non-inline body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void requestBody_isLoggedAtDebugLevel_onRequestWithInlineBody() throws IOException {
        // Given a request with an inline body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .addHeader("Content-Disposition", "inline")
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Disposition: ")) {
                bodyComingNext = true;
            }
        }

        // Then the request body should be logged...
        assertEquals(testBody, bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequestWithBinaryBody() throws IOException {
        // Given a request with a binary body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("application/octet-stream"), testBody))
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(binary body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequestWithLargeBody() throws IOException {
        // Given a request with an very large body and a client with a LoggingInterceptor.
        byte[] testBody = new byte[MAX_BODY_LOG_SIZE + 1];
        new Random().nextBytes(testBody);
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(" + (MAX_BODY_LOG_SIZE + 1) + "-byte body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequestWithEmptyBody() throws IOException {
        // Given a request with an empty body and a client with a LoggingInterceptor.
        String testBody = "";
        Request request = new Request.Builder()
            .url(mockWebServer.url("/"))
            .put(RequestBody.create(MediaType.get("text/html"), testBody))
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(empty body)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequestWithNoContentLengthSpecified() throws IOException {
        // Given a request with no body and no Content-Length header, and a client with a LoggingInterceptor.
        Request request = getSimpleRequest(mockWebServer);

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            // No headers on the request mean that the body will be logged after the host.
            if (message.startsWith("Host: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(empty body)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onRequest0WithContentLengthZero() throws IOException {
        // Given a request with a Content-Length header with value 0 and a client with a LoggingInterceptor.
        Request request = getSimpleRequestWithHeader(mockWebServer, "Content-Length", "0");

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(empty body)", bodyLogMessage);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void headers_areRedactedAtDebugLevel_onResponseWithNoAllowedHeaders() throws IOException {
        // Given a response where at least one header is populated and a client with a LoggingInterceptor with an
        // empty allowed headers list.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(null, testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When sending a request.
        redactedHeadersOkHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        // Content-Type and Content-Length are obtained from the response body.
        int contentTypeLogLevel = 0;
        String contentTypeLogMessage = null;
        int contentLengthLogLevel = 0;
        String contentLengthLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("Content-Type: ")) {
                contentTypeLogLevel = entry.getKey();
                contentTypeLogMessage = message;
            }

            if (message.startsWith("Content-Length: ")) {
                contentLengthLogLevel = entry.getKey();
                contentLengthLogMessage = message;
            }
        }

        // Then the headers should be logged with its value redacted...
        assertEquals("Content-Type: " + REDACTED_PLACEHOLDER, contentTypeLogMessage);
        assertEquals("Content-Length: " + REDACTED_PLACEHOLDER, contentLengthLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, contentTypeLogLevel);
        assertEquals(LOG_LEVEL_DEBUG, contentLengthLogLevel);
    }

    @Test
    public void nonAllowedHeaders_areRedactedAtDebugLevel_onResponse() throws IOException {
        // Given a response where at least one non-allowed header and at least one allowed header are populated and a
        // client with a LoggingInterceptor with a list of allowed headers.
        String allowedHeader = "Content-Length";
        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add(allowedHeader);
        LogOptions logOptions = new LogOptions();
        logOptions.setAllowedHeaderNames(allowedHeaders);
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(logOptions, testClientLogger);
        OkHttpClient allowedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When sending a request.
        allowedHeadersOkHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        // Content-Type and Content-Length are obtained from the response body.
        int contentTypeLogLevel = 0;
        String contentTypeLogMessage = null;
        int contentLengthLogLevel = 0;
        String contentLengthLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("Content-Type: ")) {
                contentTypeLogLevel = entry.getKey();
                contentTypeLogMessage = message;
            }

            if (message.startsWith("Content-Length: ")) {
                contentLengthLogLevel = entry.getKey();
                contentLengthLogMessage = message;
            }
        }

        // Then the non-allowed header should be logged with its value redacted...
        assertEquals("Content-Type: " + REDACTED_PLACEHOLDER, contentTypeLogMessage);
        // ...and the allowed header should be logged with its value intact...
        //noinspection ConstantConditions
        assertTrue(contentLengthLogMessage.matches(allowedHeader + ": \\d+"));
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, contentTypeLogLevel);
        assertEquals(LOG_LEVEL_DEBUG, contentLengthLogLevel);
    }

    @Test
    public void requestId_isLoggedFirstAtInfoLevel_onResponseWithRequestId() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response where the 'x-ms-client-request-id' header is populated and a client with a
        // LoggingInterceptor.
        String requestId = UUID.randomUUID().toString();
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeader.CLIENT_REQUEST_ID, requestId));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String firstLogMessageOnResponse = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("<--")) { // Marks the end of the response logs.
                logLevel = entry.getKey();
                firstLogMessageOnResponse = message;

                break;
            }
        }

        // Then the first message logged for the response should include the requestId...
        //noinspection ConstantConditions
        assertTrue(firstLogMessageOnResponse.matches("<-- \\[" + requestId + "] \\(\\d+\\)"));
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void requestId_isLoggedLastAtInfoLevel_onResponseWithRequestId() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response where the 'x-ms-client-request-id' header is populated and a client with a
        // LoggingInterceptor.
        String requestId = UUID.randomUUID().toString();
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeader.CLIENT_REQUEST_ID, requestId));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String lastLogMessageOnResponse = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("<-- [END")) { // Marks the end of the response logs.
                logLevel = entry.getKey();
                lastLogMessageOnResponse = message;
            }
        }

        // Then the last message logged for the response should include the requestId...
        assertEquals("<-- [END " + requestId + "]", lastLogMessageOnResponse);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void operationDuration_isLoggedFirstAtInfoLevel_onResponse() throws IOException {
        // Given a simple response.

        // When sending a request.
        okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String firstLogMessageOnResponse = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("<--")) { // Marks the end of the response logs.
                logLevel = entry.getKey();
                firstLogMessageOnResponse = message;

                break;
            }
        }

        // Then the first message logged for the response should include the requestId...
        //noinspection ConstantConditions
        assertTrue(firstLogMessageOnResponse.matches("<-- \\[null] \\(\\d+\\)"));
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void nonFailureResponse_isLoggedAtInfoLevel_onRequest() throws IOException {
        // Given a response with a successful status and a client with a LoggingInterceptor.

        // When sending a request.
        okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean errorComingNext = false;
        String statusLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (errorComingNext) {
                logLevel = entry.getKey();
                statusLogMessage = message;

                break;
            }

            // No headers on the request mean that the body will be logged after the host
            if (message.startsWith("<--")) {
                errorComingNext = true;
            }
        }

        // Then the response error code and message should be logged...
        assertEquals("200 OK", statusLogMessage);
        // ...at the WARNING log level.
        assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void failureResponse_isLoggedAtWarningLevel_onRequest() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with an error status and a client with a LoggingInterceptor.
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean errorComingNext = false;
        String errorStatusLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (errorComingNext) {
                logLevel = entry.getKey();
                errorStatusLogMessage = message;

                break;
            }

            // No headers on the request mean that the body will be logged after the host
            if (message.startsWith("<--")) {
                errorComingNext = true;
            }
        }

        // Then the response error code and message should be logged...
        assertEquals("404 Client Error", errorStatusLogMessage);
        // ...at the WARNING log level.
        assertEquals(LOG_LEVEL_WARNING, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithEncodedBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with an encoded body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        mockWebServer.enqueue(new MockResponse()
            .setBody(testBody)
            .addHeader("Content-Encoding", "compressed"));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Encoding: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(encoded body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void requestBody_isLoggedAtDebugLevel_onResponseWithNonEncodedBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with an encoded body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        mockWebServer.enqueue(new MockResponse()
            .setBody(testBody)
            .addHeader("Content-Encoding", "identity"));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Encoding: ")) {
                bodyComingNext = true;
            }
        }

        // Then the response body should be logged...
        assertEquals(testBody, bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithNonInlineBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with an encoded body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        mockWebServer.enqueue(new MockResponse()
            .setBody(testBody)
            .addHeader("Content-Disposition", "attached"));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Disposition: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(non-inline body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void requestBody_isLoggedAtDebugLevel_onResponseWithInlineBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with an encoded body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        mockWebServer.enqueue(new MockResponse()
            .setBody(testBody)
            .addHeader("Content-Disposition", "inline"));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Disposition: ")) {
                bodyComingNext = true;
            }
        }

        // Then the response body should be logged...
        assertEquals(testBody, bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithBinaryBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a request with a binary body and a client with a LoggingInterceptor.
        String testBody = "Test body";
        mockWebServer.enqueue(new MockResponse()
            .setBody(testBody)
            .addHeader("Content-Type", "application/octet-stream"));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(binary body omitted)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithLargeBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a request with an very large body and a client with a LoggingInterceptor.
        byte[] testBody = new byte[MAX_BODY_LOG_SIZE + 1];
        new Random().nextBytes(testBody);
        mockWebServer.enqueue(new MockResponse()
            .setBody(new String(testBody)));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        //noinspection ConstantConditions
        assertTrue(bodyLogMessage.matches("\\(\\d+-byte body omitted\\)"));
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithEmptyBody() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a request with an empty body and a client with a LoggingInterceptor.
        String testBody = "";
        mockWebServer.enqueue(new MockResponse().setBody(testBody));

        // When sending a request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(empty body)", bodyLogMessage);
        // ...at the DEBUG log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithNoContentLengthSpecified() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with no body and no Content-Length header, and a client with a LoggingInterceptor.
        mockWebServer.enqueue(new MockResponse().clearHeaders());

        // When sending a request.
        okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.equals("200 OK")) { // No headers were logged before the body.
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(empty body)", bodyLogMessage);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void placeholder_isLoggedAtDebugLevel_onResponseWithContentLengthZero() throws IOException {
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute(); // Execute a request to remove the response already enqueued in @Rule.
        testClientLogger.clearLogs();

        // Given a response with no body and a client with a LoggingInterceptor.
        mockWebServer.enqueue(new MockResponse()); // A default MockResponse has Content-Length: 0.

        // When sending a request.
        okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        boolean bodyComingNext = false;
        String bodyLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (bodyComingNext) {
                logLevel = entry.getKey();
                bodyLogMessage = message;

                break;
            }

            if (message.startsWith("Content-Length: ")) {
                bodyComingNext = true;
            }
        }

        // Then a placeholder message should be logged instead of the request body...
        assertEquals("(empty body)", bodyLogMessage);
        // ...at the INFO log level.
        assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }
}
