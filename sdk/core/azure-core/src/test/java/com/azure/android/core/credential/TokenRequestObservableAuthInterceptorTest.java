package com.azure.android.core.credential;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.interceptor.EnqueueMockResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TokenRequestObservableAuthInterceptorTest {
    private final MockWebServer mockWebServer = new MockWebServer();
    private final List<String> scopes = new ArrayList<>(Arrays.asList("testScope1", "testScope2"));
    private final TokenRequestObservableAuthInterceptor tokenRequestObservableAuthInterceptor =
        new TokenRequestObservableAuthInterceptor(scopes);
    private final OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(tokenRequestObservableAuthInterceptor);

    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    @Test
    public void getTokenRequestObservable() {
        assertNotNull(tokenRequestObservableAuthInterceptor.getTokenRequestObservable());
    }

    @Test
    public void authorizationHeader_isPopulated_onRequest() throws IOException, InterruptedException {
        // Given a client with a TokenRequestObservableAuthInterceptor.
        tokenRequestObservableAuthInterceptor.getTokenRequestObservable().observeForever(new TokenRequestObserver() {
            private AccessToken accessToken = new AccessToken("testToken", OffsetDateTime.now());

            @Override
            public void onTokenRequest(String[] scopes, TokenResponseCallback callback) {
                callback.onToken(accessToken);
            }
        });

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'Authorization' header should be populated with an AccessToken.
        assertEquals("Bearer testToken", mockWebServer.takeRequest().getHeader(HttpHeader.AUTHORIZATION));
    }
}
