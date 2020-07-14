package com.azure.android.identity;

import androidx.lifecycle.Observer;

/**
 * An Observer of {@link TokenRequestObservable}.
 */
public abstract class TokenRequestObserver implements Observer<TokenRequestHandle> {
    @Override
    public final void onChanged(TokenRequestHandle requestHandle) {
        if (!requestHandle.isConsumed()) {
            this.onTokenRequest(requestHandle.getScopes().toArray(new String[0]), new TokenResponseCallback() {
                @Override
                public void onToken(AccessToken token) {
                    requestHandle.setToken(token);
                }

                @Override
                public void onError(Throwable t) {
                    requestHandle.setError(t);
                }
            });
        }
    }

    /**
     * Invoked when {@link TokenRequestObservable} emits a token request event.
     *
     * @param scopes The scope of the requested token.
     * @param callback The callback that this observer use to notify the result of token retrieval.
     */
    public abstract void onTokenRequest(String[] scopes, TokenResponseCallback callback);
}
