package com.azure.android.storage.sample.kotlin.core.util.tokenrequest

import com.azure.android.storage.sample.kotlin.core.credential.AccessToken
import okhttp3.Interceptor
import okhttp3.Response
import org.threeten.bp.Duration
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock

/**
 * An OkHttp interceptor that uses [TokenRequestObservable] to retrieve the access-token
 * and set it to Authorization Bearer header.
 */
class TokenRequestObservableAuthInterceptor(scopes: ArrayList<String>) : Interceptor {
    // The Observable to send and Observe token request.
    private val requestObservable: TokenRequestObservable = TokenRequestObservable()

    // The scope for the requested token.
    private val scopes: List<String> = ArrayList(scopes)

    // The current cached access token.
    @Volatile
    private var accessToken: AccessToken? = null

    // A lock to ensure only one request to the UI thread is on fly.
    private val sendRequestLock = ReentrantLock()

    /**
     * @return the token request observable that UI can Observe for access-token
     * request coming from this interceptor.
     */
    val tokenRequestObservable: TokenRequestObservable
        get() = requestObservable

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (accessToken != null && !accessToken!!.isExpired) {
            setAuthenticationHeader(chain, accessToken!!)
        } else {
            sendRequestLock.lock()
            try {
                val handle: TokenRequestHandle = requestObservable.sendRequest(scopes)
                try {
                    accessToken = handle.waitForToken(Duration.ofSeconds(60))
                } catch (t: Throwable) {
                    throw RuntimeException(t)
                }
            } finally {
                sendRequestLock.unlock()
            }
            setAuthenticationHeader(chain, accessToken!!)
        }
    }

    companion object {
        @Throws(IOException::class)
        private fun setAuthenticationHeader(chain: Interceptor.Chain, accessToken: AccessToken): Response {
            val authRequest = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken.token)
                .build()
            return chain.proceed(authRequest)
        }
    }

}
