package com.azure.android.core.credential;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.interceptor.EnqueueMockResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
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
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
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
    public void authorizationHeader_isPopulated_onRequest() {
        // Given a client with a TokenRequestObservableAuthInterceptor.
        AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).create().start().get();

        tokenRequestObservableAuthInterceptor.getTokenRequestObservable().observe(activity, new TokenRequestObserver() {
            private AccessToken accessToken = new AccessToken("testToken", OffsetDateTime.now());

            @Override
            public void onTokenRequest(String[] scopes, TokenResponseCallback callback) {
                callback.onToken(accessToken);
            }
        });

        // When executing a request.

        // Then the 'Authorization' header should be populated with an AccessToken.

        // NOTE: We need to make the request in a different thread than the one Robolectric created the activity on.
        // This way the Observable doesn't think we are running in the UI thread.
        new Thread() {
            @Override
            public void run() {
                super.run();
                Request request = getSimpleRequest(mockWebServer);

                try {
                    okHttpClient.newCall(request).execute();
                    assertEquals("Bearer testToken", mockWebServer.takeRequest().getHeader(HttpHeader.AUTHORIZATION));
                } catch (IOException | InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        }.start();
    }
}
