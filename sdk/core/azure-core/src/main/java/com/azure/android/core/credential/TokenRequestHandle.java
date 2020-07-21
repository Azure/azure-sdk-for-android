package com.azure.android.core.credential;

import org.threeten.bp.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link TokenRequestHandle handle} that a background running thread (e.g. OkHttp thread) uses to synchronously
 * receive an {@link AccessToken} produced by the main UI thread. A background running thread can initiate the
 * {@link AccessToken} request by calling {@link TokenRequestObservable#sendRequest(List)} ()}, which returns a
 * {@link TokenRequestHandle} instance. The background thread then waits for the arrival of the {@link AccessToken}
 * by calling {@link TokenRequestHandle#waitForToken(Duration)}.
 * <p>
 * IMPLEMENTATION NOTE: The {@link TokenRequestObservable} INTERNALLY uses the same {@link TokenRequestHandle} object
 * (returned from {@link TokenRequestObservable#sendRequest(List)}) as an Event to notify a {@link TokenRequestObserver}
 * about the need for {@link AccessToken}. Upon receiving this event, the {@link TokenRequestObserver} invokes
 * {@link TokenRequestObserver#onTokenRequest(String[], TokenResponseCallback)} ({@link TokenResponseCallback})}
 * which is responsible for retrieving the {@link AccessToken}, the observer then sets the {@link AccessToken} or
 * {@link AccessToken} retrieval error in the Handle using it's package PRIVATE setters
 * {@link TokenRequestHandle#setToken(AccessToken)}, {@link TokenRequestHandle#setError(Throwable)}.
 * <p>
 * It is important that an Event instance is processed only once by the {@link TokenRequestObserver}. The
 * {@link TokenRequestObservable} uses a {@link androidx.lifecycle.LiveData} instance to post the Event to
 * {@link TokenRequestObserver}. {@link androidx.lifecycle.LiveData} is designed to cache the latest value (in
 * this case Event) and re-deliver it when there is a new Observer. An example for unexpected re-delivery is: Let's
 * say the {@link TokenRequestObservable} is owned by a {@link androidx.lifecycle.ViewModel} which lives
 * through multiple {@code Activity::onCreate} - {@code Activity::onDestroy} spans and in
 * {@code Activity::onCreate} we register a {@link TokenRequestObserver} to this {@link androidx.lifecycle.ViewModel}
 * 's scoped Observable. When a background thread requests for a token, the Observer will receive the request and
 * process it. If the user begins rotating the screen (i.e configuration changed) then {@code Activity::onDestroy} is
 * called and once the rotation is done {@code Activity::onCreate} method will be called again, which will result in
 * the registration of a new {@link TokenRequestObserver} and will internally cause
 * {@link androidx.lifecycle.LiveData} to re-deliver the old cached/handled Event, which we definitely don't want the
 * new Observer to handle. To identify such an Event, the Observer uses {@link TokenRequestHandle#isConsumed()}.
 */
class TokenRequestHandle {
    // The scope of the token requested by the handle owner.
    private final List<String> scopes;
    // Indicates whether the handle has already been consumed by a TokenRequestObserver.
    private boolean isConsumed;
    // The latch that signals the handle owner that the token acquisition operation is completed (successfully or
    // error-ed).
    private CountDownLatch latch = new CountDownLatch(1);
    // The retrieved access token.
    private AccessToken token;
    // The error on token retrieval.
    private Throwable error;

    /**
     * Creates a {@link TokenRequestHandle}.
     *
     * @param scopes The requested token scope.
     */
    TokenRequestHandle(List<String> scopes) {
        this.scopes = new ArrayList<>(scopes);
    }

    /**
     * Synchronously wait for the arrival of an {@link AccessToken} requested via
     * {@link TokenRequestObservable#sendRequest(List)}}.
     *
     * @param timeout The maximum time to wait in milliseconds.
     * @return The {@link AccessToken}.
     * @throws Throwable if there is an error.
     */
    public AccessToken waitForToken(Duration timeout) throws Throwable {
        this.latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);

        if (this.error != null) {
            throw this.error;
        } else {
            return this.token;
        }
    }

    /**
     * @return The scopes required for the token.
     */
    List<String> getScopes() {
        return this.scopes;
    }

    /**
     * @return true if event is consumed, false otherwise.
     */
    boolean isConsumed() {
        if (this.isConsumed) {
            return true;
        } else {
            this.isConsumed = true;

            return false;
        }
    }

    /**
     * Sets the {@link AccessToken} retrieved and unblocks the background thread waiting in
     * {@link TokenRequestHandle#waitForToken(Duration)}.
     *
     * @param token The {@link AccessToken}.
     */
    void setToken(AccessToken token) {
        if (this.token != null) {
            throw new IllegalStateException("AccessToken has already been set.");
        }

        this.token = token;
        this.latch.countDown();
    }

    /**
     * Sets the error during {@link AccessToken} retrieval and unblocks the background thread waiting in
     * {@link TokenRequestHandle#waitForToken(Duration)}.
     *
     * @param error The error.
     */
    void setError(Throwable error) {
        if (this.error != null) {
            throw new IllegalStateException("Error has already been set.");
        }

        this.error = error;
        this.latch.countDown();
    }
}
