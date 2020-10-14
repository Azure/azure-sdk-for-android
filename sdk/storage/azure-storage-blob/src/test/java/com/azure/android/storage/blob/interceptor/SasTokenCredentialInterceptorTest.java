package com.azure.android.storage.blob.interceptor;

import com.azure.android.core.common.EnqueueMockResponse;
import com.azure.android.storage.blob.credential.SasTokenCredential;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.storage.blob.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.storage.blob.TestUtils.getSimpleRequest;
import static com.azure.android.storage.blob.TestUtils.getSimpleRequestWithQueryParam;
import static org.junit.Assert.assertEquals;

public class SasTokenCredentialInterceptorTest {
    private final String sasToken = "sig=signature&key=value";
    private final SasTokenCredentialInterceptor sasTokenCredentialInterceptor =
        new SasTokenCredentialInterceptor(new SasTokenCredential(sasToken));

    private final MockWebServer mockWebServer = new MockWebServer();
    private final OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(sasTokenCredentialInterceptor);

    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Test
    public void intercept() throws IOException, InterruptedException {
        // Given a client with a SasTokenCredentialInterceptor.

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the SAS token should have been appended to the request URL.
        assertEquals(sasToken, mockWebServer.takeRequest().getRequestUrl().encodedQuery());
    }

    @Test
    public void intercept_withQueryParams() throws IOException, InterruptedException {
        // Given a client with a SasTokenCredentialInterceptor.

        // When executing a request that includes query params.
        Request request = getSimpleRequestWithQueryParam(mockWebServer, "otherKey", "otherValue");
        okHttpClient.newCall(request).execute();

        // Then the SAS token should have been appended to the request URL after its original query parameters.
        assertEquals("otherKey=otherValue&" + sasToken,
            mockWebServer.takeRequest().getRequestUrl().encodedQuery());
    }
}
