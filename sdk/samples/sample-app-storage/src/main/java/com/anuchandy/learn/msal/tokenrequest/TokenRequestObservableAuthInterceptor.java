package com.anuchandy.learn.msal.tokenrequest;

import com.azure.android.core.credential.AccessToken;

import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An OkHttp interceptor that uses {@link TokenRequestObservable} to retrieve the access-token
 * and set it to Authorization Bearer header.
 */
public class TokenRequestObservableAuthInterceptor implements Interceptor {
    // The Observable to send token request.
    private final TokenRequestObservable requestObservable;
    // The scope for the requested token.
    private final List<String> scopes;
    // The current cached access token.
    private volatile AccessToken accessToken;

    /**
     * Creates TokenRequestObservableAuthInterceptor.
     *
     * @param requestObservable an observable to send token request
     * @param scopes the scope for the requested token
     */
    public TokenRequestObservableAuthInterceptor(TokenRequestObservable requestObservable,
                                                 List<String> scopes) {
        this.requestObservable = requestObservable;
        this.scopes = new ArrayList<>(scopes);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (this.accessToken != null && !this.accessToken.isExpired()) {
            return setAuthenticationHeader(chain, this.accessToken);
        } else {
            TokenRequestHandle handle = this.requestObservable.sendRequest(this.scopes);
            try {
                this.accessToken = handle.waitForToken(Duration.ofSeconds(60));
            } catch (Throwable t) {
                throw new RuntimeException(t);
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
