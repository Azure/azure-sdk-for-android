package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.util.logging.ClientLogger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockWebServer;
import okio.BufferedSink;

import static com.azure.android.core.http.interceptor.LoggingInterceptor.REDACTED_PLACEHOLDER;
import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeader;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeaders;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithQueryParam;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithQueryParams;
import static com.azure.android.core.http.interceptor.TestUtils.getStackTraceString;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_DEBUG;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_INFO;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_WARNING;


public class LoggingInterceptorTest {
    private final MockWebServer mockWebServer = new MockWebServer();
    private final TestClientLogger testClientLogger = new TestClientLogger();
    private final OkHttpClient okHttpClient =
        buildOkHttpClientWithInterceptor(new LoggingInterceptor(new LogOptions(), testClientLogger));

    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Test
    public void requestId_isLoggedFirstAtInfoLevel_onRequestWithRequestId() throws IOException {
        // Given a request where the 'x-ms-client-request-id' header is already populated and a client with a
        // LoggingInterceptor.
        String requestId = UUID.randomUUID().toString();
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.CLIENT_REQUEST_ID, requestId);

        // When executing said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = logs.get(0).getKey();
        String firstLogMessageOnRequest = logs.get(0).getValue();

        // Then the first message logged  for the request should include the requestId...
        Assert.assertEquals("--> [" + requestId + "]", firstLogMessageOnRequest);
        // ...at the INFO log level.
        Assert.assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void requestId_isLoggedLastAtInfoLevel_onRequestWithRequestId() throws IOException {
        // Given a request where the 'x-ms-client-request-id' header is already populated and a client with a
        // LoggingInterceptor.
        String requestId = UUID.randomUUID().toString();
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.CLIENT_REQUEST_ID, requestId);

        // When executing said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String lastLogMessageOnRequest = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("--> [END")) {
                logLevel = entry.getKey();
                lastLogMessageOnRequest = message;
            }
        }

        // Then the first message logged for the request should include the requestId...
        Assert.assertEquals("--> [END " + requestId + "]", lastLogMessageOnRequest);
        // ...at the INFO log level.
        Assert.assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void headers_areRedactedAtDebugLevel_onRequestWithNoAllowedHeaders() throws IOException {
        // Given a request where at least one header is populated...
        String testHeader = "Test-Header";
        Request request = getSimpleRequestWithHeader(mockWebServer, testHeader, "Test Value");
        // ...and a client with a LoggingInterceptor with an empty allowed headers list.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(null, testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String headerLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith(testHeader)) {
                logLevel = entry.getKey();
                headerLogMessage = message;
            }
        }

        // Then the header should be logged with its value redacted...
        Assert.assertEquals(testHeader + ": " + REDACTED_PLACEHOLDER, headerLogMessage);
        // ...at the DEBUG log level.
        Assert.assertEquals(LOG_LEVEL_DEBUG, logLevel);
    }

    @Test
    public void nonAllowedHeaders_areRedactedAtDebugLevel_onRequest() throws IOException {
        // Given a request where at least one non-allowed header and at least one allowed header are populated...
        String testHeader = "Test-Header";
        String contentTypeHeader = "Content-Type";
        String contentType = "text/html";
        Map<String, String> headers = new HashMap<>();
        headers.put(testHeader, "Test value");
        headers.put(contentTypeHeader, contentType);
        Request request = getSimpleRequestWithHeaders(mockWebServer, headers);
        // ...and a client with a LoggingInterceptor with a list of allowed headers.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(new LogOptions(), testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);

        // When executing said request on an OkHttpClient with a LoggingInterceptor with an empty allowed headers list.
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int testHeaderLogLevel = 0;
        String testHeaderLogMessage = null;
        int contentTypeHeaderLogLevel = 0;
        String contentTypeHeaderLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith(testHeader)) {
                testHeaderLogLevel = entry.getKey();
                testHeaderLogMessage = message;
            }

            if (message.startsWith(contentTypeHeader)) {
                contentTypeHeaderLogLevel = entry.getKey();
                contentTypeHeaderLogMessage = message;
            }
        }

        // Then the non-allowed header should be logged with its value redacted...
        Assert.assertEquals(testHeader + ": " + REDACTED_PLACEHOLDER, testHeaderLogMessage);
        // ...and the allowed header should be logged with its value intact...
        Assert.assertEquals(contentTypeHeader + ": " + contentType, contentTypeHeaderLogMessage);
        // ...at the DEBUG log level.
        Assert.assertEquals(LOG_LEVEL_DEBUG, testHeaderLogLevel);
        Assert.assertEquals(LOG_LEVEL_DEBUG, contentTypeHeaderLogLevel);
    }

    @Test
    public void queryParams_areRedactedAtDebugLevel_onRequestWithNoAllowedQueryParams() throws IOException {
        // Given a request with a URL where there is at least one query parameter...
        String paramName = "testParam";
        Request request = getSimpleRequestWithQueryParam(mockWebServer, paramName, "testValue");
        // ...and a client with a LoggingInterceptor with a list of allowed query parameters.
        LoggingInterceptor redactedHeadersInterceptor = new LoggingInterceptor(null, testClientLogger);
        OkHttpClient redactedHeadersOkHttpClient = buildOkHttpClientWithInterceptor(redactedHeadersInterceptor);
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String urlLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("GET ")) {
                logLevel = entry.getKey();
                urlLogMessage = message;
            }
        }

        // Then the query parameter should be logged as part of the URL with its value redacted...
        Assert.assertEquals("GET /?" + paramName + "=" + REDACTED_PLACEHOLDER, urlLogMessage);
        // ...at the INFO log level.
        Assert.assertEquals(LOG_LEVEL_INFO, logLevel);
    }

    @Test
    public void nonAllowedQueryParams_areRedactedAtDebugLevel_onRequest() throws IOException {
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
        redactedHeadersOkHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        int logLevel = 0;
        String urlLogMessage = null;

        for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
            String message = entry.getValue();

            if (message.startsWith("GET ")) {
                logLevel = entry.getKey();
                urlLogMessage = message;
            }
        }

        // Then the query parameter should be logged as part of the URL with its value redacted...
        Assert.assertEquals("GET /?" + allowedQueryParamName + "=" + allowedQueryParamValue + "&" +
            nonAllowedQueryParamName + "=" + REDACTED_PLACEHOLDER, urlLogMessage); // Query params are ordered alphabetically by the OkHttp when creating the request
        // ...at the INFO log level.
        Assert.assertEquals(LOG_LEVEL_INFO, logLevel);
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
            // When executing said request an error will occur.
            okHttpClient.newCall(request).execute();
        } catch (Exception e) {
            List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
            int logLevel = 0;
            String errorLogMessage = null;
            String errorPrefix = "OPERATION FAILED: ";

            for (AbstractMap.SimpleEntry<Integer, String> entry : logs) {
                String message = entry.getValue();

                if (message.startsWith(errorPrefix)) {
                    logLevel = entry.getKey();
                    errorLogMessage = message;
                }
            }

            // Then the error message will be logged...
            Assert.assertEquals(errorPrefix + getStackTraceString(e), errorLogMessage);
            // ...at the WARNING log level.
            Assert.assertEquals(LOG_LEVEL_WARNING, logLevel);
        }
    }

    // Test that the LoggingInterceptor logs the request line in the correct format at the correct level
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the request body at the correct log level
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request body is encoded
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the request body when its encoding is identity
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request body is attached
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the request body when it is inline
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request body content is binary
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request body is too large to log
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request body is empty
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request doesn't specify a content length
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the request content length is zero
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor adds the request start time to the context
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts all response headers when the allowHeaders parameter is empty
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts response headers not included in the defaultAllowHeaders property
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor starts the response log with the request ID
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor ends the response log with the request ID
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor starting line includes the duration
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the status line of a successful response in the correct format at the correct
    // level
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the status line of a failure response in the correct format at the correct
    // level
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response body is encoded
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the response body when its encoding is identity
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response body is attached
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the response body when it is inline
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response body content is binary
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the response body when its content is text
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response body is too large to log
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response body is empty
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response doesn't specify a content length
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response content length is zero
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs a placeholder message when the response content length is present but empty
    /*
    @Test
    public void test() {
    }
    */

    private static class TestClientLogger implements ClientLogger {
        private final List<AbstractMap.SimpleEntry<Integer, String>> logs = new ArrayList<>();
        private int logLevel;

        List<AbstractMap.SimpleEntry<Integer, String>> getLogs() {
            return logs;
        }

        @Override
        public int getLogLevel() {
            return logLevel;
        }

        @Override
        public void setLogLevel(int logLevel) {
            this.logLevel = logLevel;
        }

        @Override
        public void debug(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_DEBUG, message));
        }

        @Override
        public void debug(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_DEBUG, message + getStackTraceString(throwable)));
        }

        @Override
        public void info(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_INFO, message));
        }

        @Override
        public void info(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_INFO, message + getStackTraceString(throwable)));
        }

        @Override
        public void warning(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_WARNING, message));
        }

        @Override
        public void warning(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_WARNING, message + getStackTraceString(throwable)));
        }

        @Override
        public void error(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_ERROR, message));
        }

        @Override
        public void error(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_ERROR, message + getStackTraceString(throwable)));
        }

    }
}
