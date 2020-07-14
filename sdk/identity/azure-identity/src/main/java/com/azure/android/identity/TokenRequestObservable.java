package com.azure.android.identity;

import android.os.Looper;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * An Observable to send the token request from a background thread to an Observer in the main UI thread.
 * <p>
 * A use case of this type is - sending a token request from ViewModel code running in a background thread to the main
 * UI thread, where the actual acquisition of token is interactive, e.g. involves Activity UI. This Observable
 * decouples ViewModel from the UI so that ViewModel will not hold a reference to the UI component, holding such
 * reference can lead to a memory leak. However, the Observer of this Observable can hold a UI reference, because the
 * Observer is removed when the UI that the Observer is associated with is in the DESTROYED state.
 */
public final class TokenRequestObservable {
    /**
     * The LiveData to notify the token request to a LifeCycle aware Observer registered through
     * {@link TokenRequestObservable#observe(LifecycleOwner, TokenRequestObserver)}
     */
    private final MutableLiveData<TokenRequestHandle> innerObservable;

    /**
     * Creates a TokenRequestObservable to send the token request and observe for it.
     */
    public TokenRequestObservable() {
        this.innerObservable = new MutableLiveData<>();
    }

    /**
     * Emits a token request to a {@link TokenRequestObserver} registered through
     * {@link TokenRequestObservable#observe(LifecycleOwner, TokenRequestObserver)}.
     * <p>
     * The Observer's {@link TokenRequestObserver#onTokenRequest(String[], TokenResponseCallback)}
     * will be executed in the main UI thread.
     *
     * @param scopes The scopes required for the token.
     * @return The Handle that the caller can use to synchronously receive an access token produced by the main UI
     * thread.
     * @throws if this method is called from the main UI thread.
     */
    public TokenRequestHandle sendRequest(List<String> scopes) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // The TokenRequestObservable is designed for sending a request from a background thread to the main UI
            // thread. This block validates sendRequest() is not called from the main UI thread as such a call will
            // result in a dead lock once the caller waits on the returned Handle.
            throw new IllegalStateException("Cannot invoke sendRequest() on the main UI Thread");
        }

        TokenRequestHandle requestMessage = new TokenRequestHandle(scopes);
        this.innerObservable.postValue(requestMessage);

        return requestMessage;
    }

    /**
     * Register an Observer that listen for token request Event and associate the Observer with the lifecycle of the
     * given owner.
     * <p>
     * An event will be delivered to the Observer only if the owner (e.g. Activity, View) is in active state i.e.
     * when it is in either STARTED or RESUMED states.
     * <p>
     * When the owner reaches DESTROYED state right before calling owner::onDestroy(), the observer will be removed
     * and ready for GC collection. https://developer.android.com/reference/androidx/lifecycle/Lifecycle.State
     * .html#DESTROYED https://developer.android.com/topic/libraries/architecture/livedata#work_livedata This
     * guarantees that any reference to UI components (e.g. Activity) that the Observer is holding will be released.
     *
     * @param owner The owner whose lifecycle the Observer needs to be aware of.
     * @param observer The token request Observer.
     */
    public void observe(LifecycleOwner owner, TokenRequestObserver observer) {
        // It's "VERY IMPORTANT" that this TokenRequestObservable is NOT holding a reference to LifecycleOwner or
        // TokenRequestObserver parameters, doing so can result in memory leaks.
        this.innerObservable.observe(owner, observer);
    }
}
