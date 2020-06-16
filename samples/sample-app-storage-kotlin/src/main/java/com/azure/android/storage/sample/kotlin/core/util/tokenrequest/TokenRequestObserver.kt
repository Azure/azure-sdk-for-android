package com.azure.android.storage.sample.kotlin.core.util.tokenrequest

import androidx.lifecycle.Observer
import com.azure.android.storage.sample.kotlin.core.credential.AccessToken

/**
 * An Observer of [TokenRequestObservable].
 */
abstract class TokenRequestObserver : Observer<TokenRequestHandle> {
    override fun onChanged(requestHandle: TokenRequestHandle) {
        if (!requestHandle.isConsumed()) {
            onTokenRequest(requestHandle.scopes.toTypedArray(), object : TokenResponseCallback {
                override fun onToken(token: AccessToken) {
                    requestHandle.setToken(token)
                }

                override fun onError(t: Throwable?) {
                    requestHandle.setError(t)
                }
            })
        }
    }

    /**
     * Invoked when [TokenRequestObservable] emits a token request event.
     *
     * @param scopes the scope of the requested token
     * @param callback the callback that this observer use to notify the result
     * of token retrieval
     */
    abstract fun onTokenRequest(scopes: Array<String>, callback: TokenResponseCallback)
}
