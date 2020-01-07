package com.anuchandy.learn.msal;

import com.azure.android.core.credential.TokenCredential;
import com.azure.android.core.credential.TokenRequestContext;
import com.azure.android.core.credential.AccessToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BearerTokenAuthenticationInterceptor implements Interceptor {
    private final TokenCredential credential;
    private final List<String> scopes;
    private volatile AccessToken accessToken;

    BearerTokenAuthenticationInterceptor(TokenCredential credential, List<String> scopes) {
        this.credential = credential;
        this.scopes = new ArrayList<>(scopes);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (this.accessToken != null && !this.accessToken.isExpired()) {
            return setAuthenticationHeader(chain, this.accessToken);
        } else {
            this.accessToken = credential.getToken(new TokenRequestContext().setScopes(this.scopes));
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
