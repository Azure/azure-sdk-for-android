package com.azure.android.identity;

import com.azure.android.core.credential.AccessToken;

/**
 * The callback that {@link TokenRequestObserver} uses to notify the result of token retrieval.
 */
public interface TokenResponseCallback {
    /**
     * Notify the successful retrieval of an access token.
     *
     * @param token The retrieved token.
     */
    void onToken(AccessToken token);

    /**
     * Notify the error occurred during access token retrieval.
     *
     * @param t The error.
     */
    void onError(Throwable t);
}
