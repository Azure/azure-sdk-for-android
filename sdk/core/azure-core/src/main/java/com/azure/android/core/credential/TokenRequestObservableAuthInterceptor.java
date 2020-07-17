package com.azure.android.core.credential;

import androidx.annotation.NonNull;

import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An OkHttp interceptor that uses {@link TokenRequestObservable} to retrieve an {@link AccessToken} and set as a the
 * value of the 'Authorization Bearer' header.
 */
public class TokenRequestObservableAuthInterceptor implements Interceptor {
    // The Observable to send and observe a token request.
    private final TokenRequestObservable requestObservable = new TokenRequestObservable();
    // The scope for the requested token.
    private final List<String> scopes;
    // The current cached access token.
    private volatile AccessToken accessToken;
    // A lock to ensure only one request to the UI thread is on fly.
    private final ReentrantLock sendRequestLock = new ReentrantLock();

    /**
     * Creates a {@link TokenRequestObservableAuthInterceptor}.
     *
     * @param scopes The scope for the requested token.
     */
    public TokenRequestObservableAuthInterceptor(List<String> scopes) {
        this.scopes = new ArrayList<>(scopes);
    }

    /**
     * @return The {@link TokenRequestObservable} that the UI can observe for an {@link AccessToken} request coming
     * from this interceptor.
     */
    public TokenRequestObservable getTokenRequestObservable() {
        return this.requestObservable;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (this.accessToken == null || this.accessToken.isExpired()) {
            this.sendRequestLock.lock();

            try {
                TokenRequestHandle handle = this.requestObservable.sendRequest(this.scopes);

                try {
                    this.accessToken = handle.waitForToken(Duration.ofSeconds(60));
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            } finally {
                this.sendRequestLock.unlock();
            }
        }

        return setAuthenticationHeader(chain, this.accessToken);
    }

    private static Response setAuthenticationHeader(Chain chain, AccessToken accessToken) throws IOException {
        Request authRequest = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer " + accessToken.getToken())
            .build();

        return chain.proceed(authRequest);
    }
}
