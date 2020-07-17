package com.azure.android.core.credential;

/**
 * The callback that {@link TokenRequestObserver} uses to notify the result of token retrieval.
 */
public interface TokenResponseCallback {
    /**
     * Notify the successful retrieval of an {@link AccessToken}.
     *
     * @param token The retrieved token.
     */
    void onToken(AccessToken token);

    /**
     * Notify the error occurred during {@link AccessToken} retrieval.
     *
     * @param t The error.
     */
    void onError(Throwable t);
}
