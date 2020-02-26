package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.util.logging.ClientLogger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeader;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_INFO;


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

    // Test that the LoggingInterceptor redacts all request headers when the allowHeaders parameter is empty
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts request headers not included in the defaultAllowHeaders property
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts request headers not included in the supplied allowHeaders parameter
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts all query string params when the allowQueryParams parameter is empty and
    // the params are provided as part of the URL
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts query string params not included in the supplied allowQueryParams parameter
    // when the params are provided as part of the URL
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts all query string params when the allowQueryParams parameter is empty and
    // the params are provided via HttpRequest's queryParams argument
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts query string params not included in the supplied allowQueryParams parameter
    // when the params are provided via HttpRequest's queryParams argument
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts all query string params when the allowQueryParams parameter is empty and
    // some params are provided as part of the URL and others are provided via HttpRequest's queryParams argument
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor redacts query string params not included in the supplied allowQueryParams parameter
    // when some params are provided as part of the URL and others are provided via HttpRequest's queryParams argument
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs internal failures at the correct level
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor logs the request line in the correct format at the correct level
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor doesn't log request headers if the log level is not low enough
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor doesn't log the request body if the log level is not low enough
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

    // Test that the LoggingInterceptor logs the request body when its content is text
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

    // Test that the LoggingInterceptor redacts response headers not included in the supplied allowHeaders parameter
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

    // Test that the LoggingInterceptor doesn't log response headers if the log level is not low enough
    /*
    @Test
    public void test() {
    }
    */

    // Test that the LoggingInterceptor doesn't log the response body if the log level is not low enough
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

    // Test that the LoggingInterceptor logs the error's localized description when an error object is present
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
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_DEBUG, appendStackTraceToMessage(message, throwable)));
        }

        @Override
        public void info(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_INFO, message));
        }

        @Override
        public void info(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_INFO, appendStackTraceToMessage(message, throwable)));
        }

        @Override
        public void warning(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_WARNING, message));
        }

        @Override
        public void warning(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_WARNING, appendStackTraceToMessage(message, throwable)));
        }

        @Override
        public void error(String message) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_ERROR, message));
        }

        @Override
        public void error(String message, Throwable throwable) {
            logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_ERROR, appendStackTraceToMessage(message, throwable)));
        }

        private String appendStackTraceToMessage(String message, Throwable throwable) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            printWriter.flush();

            return message + System.lineSeparator() +
                throwable.getMessage() + System.lineSeparator() +
                stringWriter.toString();
        }
    }
}
