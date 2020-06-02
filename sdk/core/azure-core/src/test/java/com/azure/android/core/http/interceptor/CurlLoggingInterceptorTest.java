package com.azure.android.core.http.interceptor;

import com.azure.android.core.common.EnqueueMockResponse;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.common.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.common.TestUtils.getSimpleRequest;
import static com.azure.android.core.common.TestUtils.getSimpleRequestWithHeader;
import static com.azure.android.core.util.logging.ClientLogger.LOG_LEVEL_DEBUG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CurlLoggingInterceptorTest {
    private final MockWebServer mockWebServer = new MockWebServer();
    private final TestClientLogger testClientLogger = new TestClientLogger();
    private final OkHttpClient okHttpClient =
        buildOkHttpClientWithInterceptor(new CurlLoggingInterceptor(testClientLogger));

    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Test
    public void curlLoggingInterceptor_logsAtDebugLevel() throws IOException {
        // Given a request and a client with a CurlLoggingInterceptor.

        // When sending said request.
        okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();

        // Then all the messages should be logged at the DEBUG log level.
        for (AbstractMap.SimpleEntry<Integer, String> log : logs) {
            assertEquals(LOG_LEVEL_DEBUG, (long) log.getKey());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void headersAndQueryParameters_areNotRedacted() throws IOException {
        // Given a request where at least one header is populated and there is at least one query parameter, and a
        // client with a CurlLoggingInterceptor.
        String testHeaderName = "Test-Header";
        String testHeaderValue = "Test Value";
        HttpUrl urlWithQueryParam = mockWebServer.url("/?testQueryParam=testValue");
        Request request = new Request.Builder()
            .addHeader(testHeaderName, testHeaderValue)
            .url(urlWithQueryParam)
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the cURL command should not redact the header and query parameter values.
        assertTrue(curlCommand.contains("-H \"" + testHeaderName + ": " + testHeaderValue + "\""));
        assertTrue(curlCommand.contains(urlWithQueryParam.toString()));
    }

    @Test
    public void method_isIncluded() throws IOException {
        // Given a request and a client with a CurlLoggingInterceptor.

        // When sending said request.
        okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the method executed should be logged at the DEBUG log level.
        assertTrue(curlCommand.contains("-X GET"));
    }

    @Test
    public void quotationMarksInHeaderValues_areProperlyEscaped() throws IOException {
        // Given a request where a header value contains double quotation marks and a client with a
        // CurlLoggingInterceptor.
        String testHeaderName = "Test-Header";
        String testHeaderValue = "Test Value";
        Request request = getSimpleRequestWithHeader(mockWebServer, testHeaderName,"\"" + testHeaderValue + "\"");

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the quotation marks should be escaped in the cURL command.
        assertTrue(curlCommand.contains("-H \"" + testHeaderName + ": \\\"" + testHeaderValue + "\\\"\""));
    }

    // Test that the CurlLoggingInterceptor escapes backslashes in header values
    @Test
    public void backslashesInHeaderValues_areProperlyEscaped() throws IOException {
        // Given a request where a header value contains backslashes and a client with a CurlLoggingInterceptor.
        String testHeaderName = "Test-Header";
        String testHeaderValue = "Test\\Value";
        Request request = getSimpleRequestWithHeader(mockWebServer, testHeaderName, testHeaderValue);

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the backslashes should be escaped in the cURL command.
        assertTrue(curlCommand.contains("-H \"" + testHeaderName + ": Test\\\\Value\""));
    }

    @Test
    public void singleQuotationMarksInBody_areProperlyEscaped() throws IOException {
        // Given a request where a header value contains single quotation marks and a client with a
        // CurlLoggingInterceptor.
        Request request = new Request.Builder()
            .put(RequestBody.create(MediaType.get("text/html"), "'Test body'"))
            .url(mockWebServer.url("/"))
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the quotation marks should be escaped in the cURL command.
        assertTrue(curlCommand.contains("\\'Test body\\'"));
    }

    @Test
    public void newlineCharactersInBody_areProperlyEscaped() throws IOException {
        // Given a request where a header value contains newline characters and a client with a CurlLoggingInterceptor.
        Request request = new Request.Builder()
            .put(RequestBody.create(MediaType.get("text/html"), "\nTest body\n"))
            .url(mockWebServer.url("/"))
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the newline characters should be escaped in the cURL command.
        assertTrue(curlCommand.contains("\\nTest body\\n"));
    }

    @Test
    public void compressedFlagIsIncluded_whenContentEncodingIsNotIdentity() throws IOException {
        // Given a request where a the Content-Encoding header does not have the 'identity' value and a client with a
        // CurlLoggingInterceptor.
        Request request = new Request.Builder()
            .put(RequestBody.create(MediaType.get("text/html"), "Test body"))
            .url(mockWebServer.url("/"))
            .addHeader("Accept-Encoding", "gzip")
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the 'compressed' flag should be included in the cURL command.
        assertTrue(curlCommand.contains("--compressed"));
    }

    // Test that the CurlLoggingInterceptor omits the compressed flag requests when the accept encoding is
    // identity
    @Test
    public void compressedFlagIsNotIncluded_whenContentEncodingIsIdentity() throws IOException {
        // Given a request where a the Content-Encoding header with the value 'identity' and a client with a
        // CurlLoggingInterceptor.
        Request request = new Request.Builder()
            .put(RequestBody.create(MediaType.get("text/html"), "Test body"))
            .url(mockWebServer.url("/"))
            .addHeader("Content-Encoding", "identity")
            .build();

        // When sending said request.
        okHttpClient.newCall(request).execute();

        List<AbstractMap.SimpleEntry<Integer, String>> logs = testClientLogger.getLogs();
        String curlCommand = logs.get(1).getValue();

        // Then the 'compressed' flag should not be included in the cURL command.
        assertFalse(curlCommand.contains("--compressed"));
    }
}
