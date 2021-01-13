// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.credential.AccessToken;
import com.azure.android.core.credential.TokenCredential;
import com.azure.android.core.credential.TokenRequestContext;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.micro.util.Context;

import java.util.Objects;

/**
 * The pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme.
 */
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    private final TokenCredential credential;
    private final String[] scopes;
    // private final SimpleTokenCache cache; // TODO: anuchan: enable callback cache

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public BearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        Objects.requireNonNull(scopes);
        assert scopes.length > 0;
        this.credential = credential;
        this.scopes = scopes;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain, Context context) {
        if ("http".equals(chain.getRequest().getUrl().getProtocol())) {
            chain.finishedProcessing(
                new IllegalStateException("Token credentials require HTTPS to prevent leaking the key."));
        } else {
            this.credential.getToken(new TokenRequestContext().addScopes(scopes),
                new TokenCredentialCallback(chain));
        }
    }

    private static final class TokenCredentialCallback implements TokenCredential.TokenCredentialCallback {
        private final HttpPipelinePolicyChain chain;

        private TokenCredentialCallback(HttpPipelinePolicyChain chain) {
            this.chain = chain;
        }

        @Override
        public void onSuccess(AccessToken token) {
            chain.getRequest().getHeaders().put(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
            chain.processNextPolicy(chain.getRequest());
        }

        @Override
        public void onError(Throwable error) {
            chain.finishedProcessing(error);
        }
    }
}
