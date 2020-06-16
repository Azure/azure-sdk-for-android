package com.azure.android.storage.sample.kotlin.core.util.tokenrequest

import com.azure.android.storage.sample.kotlin.core.credential.AccessToken

/**
 * The callback that [TokenRequestObserver] uses to notify the result of token retrieval.
 */
interface TokenResponseCallback {
    /**
     * Notify successful retrieval of access-token.
     *
     * @param token the retrieved token
     */
    fun onToken(token: AccessToken)

    /**
     * Notify the error occurred during access-token retrieval.
     *
     * @param t the error
     */
    fun onError(t: Throwable?)
}
