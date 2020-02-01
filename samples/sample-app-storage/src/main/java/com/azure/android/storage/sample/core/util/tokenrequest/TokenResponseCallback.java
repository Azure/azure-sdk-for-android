package com.azure.android.storage.sample.core.util.tokenrequest;

import com.azure.android.storage.sample.core.credential.AccessToken;

/**
 * The callback that {@link TokenRequestObserver} uses to notify the result of token retrieval.
 */
public interface TokenResponseCallback {
    /**
     * Notify successful retrieval of access-token.
     *
     * @param token the retrieved token
     */
    void onToken(AccessToken token);

    /**
     * Notify the error occurred during access-token retrieval.
     *
     * @param t the error
     */
    void onError(Throwable t);
}
