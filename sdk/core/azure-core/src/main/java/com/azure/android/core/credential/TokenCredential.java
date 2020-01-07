package com.azure.android.core.credential;

/**
 * The interface for credentials that can provide a token.
 */
@FunctionalInterface
public interface TokenCredential {
    /**
     * Get a token for a given resource/audience.
     *
     * @param request the details of the token request
     * @return the access token
     */
    AccessToken getToken(TokenRequestContext request);
}
