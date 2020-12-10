package com.azure.android.storage.sample.kotlin.core.util.tokenrequest

import com.azure.android.core.credential.AccessToken
import org.threeten.bp.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A Handle that a background running thread (e.g. okhttp thread) uses to synchronously receives an
 * access-token produced by the main UI thread. A background running thread can initiate the
 * access-token request by calling [TokenRequestObservable.sendRequest] ()}, which returns
 * [TokenRequestHandle] instance. The background thread then wait for the arrival
 * of the access-token by calling [TokenRequestHandle.waitForToken].
 *
 * IMPLEMENTATION NOTE: The [TokenRequestObservable] INTERNALLY uses the same [TokenRequestHandle]
 * object (returned from sendRequest) as an Event to notify a [TokenRequestObserver] about the need
 * for access-token. Upon receiving this event, the [TokenRequestObserver] invokes
 * [TokenRequestObserver.onTokenRequest] (TokenResponseCallback)}
 * which is responsible for retrieving the access token, the observer then sets the access-token or
 * access-token retrieval error in the Handle using it's package PRIVATE setters
 * [TokenRequestHandle.setToken], [TokenRequestHandle.setError].
 *
 * It is important that an Event instance is processed only once by the [TokenRequestObserver].
 * The [TokenRequestObservable] uses [androidx.lifecycle.LiveData] to post the Event to
 * [TokenRequestObserver], the LiveData is designed to caches the latest value (in this case Event)
 * and re-deliver when there is a new observer. An example for such unexpected re-delivery is, Let's say
 * the [TokenRequestObservable] is owned by a [androidx.lifecycle.ViewModel], the ViewModel
 * live through multiple Activity::onCreate - Activity::onDestroy spans
 * https://developer.android.com/topic/libraries/architecture/viewmodel#lifecycle.
 * Let's say in Activity::onCreate(Bundle) we register a [TokenRequestObserver] to
 * this ViewModel scoped Observable. When a background thread request for token, the observer receives
 * the request and process it. If user begins rotating the screen (i.e configuration changed) then
 * the Activity::onDestroy() is called and once the rotation is done the Activity::onCreate(Bundle) method
 * will be called again, which result in registration of new [TokenRequestObserver] and internally
 * LiveData re-deliver the old cached/handled event, which we definitely don't want the new Observer to handle.
 * To identify such an Event, the Observer uses [TokenRequestHandle.isConsumed].
 */
class TokenRequestHandle(scopes: List<String>) {
    // The scope of the token requested by the handle owner.
    val scopes: ArrayList<String> = ArrayList(scopes)

    // Indicates whether the handle is already consumed by a TokenRequestObserver.
    private var isConsumed = false

    // The latch that signals the handle owner that token acquisition operation is completed (successfully or error-ed).
    private val latch = CountDownLatch(1)

    // The retrieved access token.
    private var token: AccessToken? = null

    // The error on token retrieval.
    private var error: Throwable? = null

    /**
     * synchronously wait for the arrival of access-token requested via [TokenRequestObservable.sendRequest] ()}.
     *
     * @param timeout the maximum time to wait in milliseconds
     * @return the access token
     * @throws Throwable
     */
    @Throws(Throwable::class)
    fun waitForToken(timeout: Duration): AccessToken? {
        latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)
        return if (error != null) {
            throw error!!
        } else {
            token
        }
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * @return true if event is consumed, false otherwise
     */
    internal fun isConsumed(): Boolean {
        return if (isConsumed) {
            true
        } else {
            isConsumed = true
            false
        }
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Sets the access-token retrieved and unblock the background thread waiting
     * in [TokenRequestHandle.waitForToken].
     *
     * @param token the access-token
     */
    internal fun setToken(token: AccessToken) {
        check(this.token == null) { "token is already set." }
        this.token = token
        latch.countDown()
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Sets the error during access-token retrieval and unblock the background thread
     * waiting in [TokenRequestHandle.waitForToken].
     *
     * @param error the error
     */
    internal fun setError(error: Throwable?) {
        check(this.error == null) { "error is already set." }
        this.error = error
        latch.countDown()
    }
}
