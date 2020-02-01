package com.azure.android.storage.sample.core.util.tokenrequest;

import com.azure.android.storage.sample.core.credential.AccessToken;

import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An OkHttp interceptor that uses {@link TokenRequestObservable} to retrieve the access-token
 * and set it to Authorization Bearer header.
 */
public class TokenRequestObservableAuthInterceptor implements Interceptor {
    // The Observable to send and Observe token request.
    private final TokenRequestObservable requestObservable = new TokenRequestObservable();
    // The scope for the requested token.
    private final List<String> scopes;
    // The current cached access token.
    private volatile AccessToken accessToken;
    // A lock to ensure only one request to the UI thread is on fly.
    private final ReentrantLock sendRequestLock = new ReentrantLock();

    /**
     * Creates TokenRequestObservableAuthInterceptor.
     *
     * @param scopes the scope for the requested token
     */
    public TokenRequestObservableAuthInterceptor(List<String> scopes) {
        this.scopes = new ArrayList<>(scopes);
    }

    /**
     * @return the token request observable that UI can Observe for access-token
     * request coming from this interceptor.
     */
    public TokenRequestObservable getTokenRequestObservable() {
        return this.requestObservable;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (this.accessToken != null && !this.accessToken.isExpired()) {
            return setAuthenticationHeader(chain, this.accessToken);
        } else {
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
            return setAuthenticationHeader(chain, this.accessToken);
        }
    }

    private static Response setAuthenticationHeader(Chain chain, AccessToken accessToken) throws IOException {
        Request authRequest = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken.getToken())
                .build();
        return chain.proceed(authRequest);
    }
}
