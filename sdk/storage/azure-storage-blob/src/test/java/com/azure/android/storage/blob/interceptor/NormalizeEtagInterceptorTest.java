package com.azure.android.storage.blob.interceptor;

import com.azure.android.core.http.HttpHeader;

import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;


import static com.azure.android.storage.blob.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.storage.blob.TestUtils.getSimpleRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NormalizeEtagInterceptorTest {
    private final String etag = "testEtag";
    private final NormalizeEtagInterceptor normalizeEtagInterceptor = new NormalizeEtagInterceptor();

    private final MockWebServer mockWebServer = new MockWebServer();
    private final OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(normalizeEtagInterceptor);

    @Test
    public void intercept() throws IOException, InterruptedException {
        // Given a client with a NormalizeEtagInterceptor.

        // When receiving a response with an ETag in the headers.
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeader.ETAG, etag));
        Request request = getSimpleRequest(mockWebServer);
        okhttp3.Response response = okHttpClient.newCall(request).execute();

        // Then the header should remain unchanged in the request.
        assertEquals(etag, response.header(HttpHeader.ETAG));
    }

    @Test
    public void intercept_withEtagWithQuotationMarks() throws IOException, InterruptedException {
        // Given a client with a NormalizeEtagInterceptor.

        // When receiving a response with an ETag that includes double quotation marks in the headers.
        String etagWithDoubleQuotes = "\"" + etag + "\"";
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeader.ETAG, etagWithDoubleQuotes));
        okhttp3.Response response = okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        // Then the header should remain unchanged in the request.
        assertEquals(etag, response.header(HttpHeader.ETAG));
    }

    @Test
    public void intercept_withoutEtag() throws IOException, InterruptedException {
        // Given a client with a NormalizeEtagInterceptor.

        // When receiving a response without an ETag in the headers.
        mockWebServer.enqueue(new MockResponse());
        okhttp3.Response response = okHttpClient.newCall(getSimpleRequest(mockWebServer)).execute();

        // Then the header should remain unchanged in the request.
        assertNull(response.header(HttpHeader.ETAG));
    }
}
