package com.azure.android.core.util.tokenrequest;

import android.os.Looper;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * An Observable to send the token request from background thread to an Observer in
 * the main UI thread
 *
 * A use case of this type is - sending a token request from ViewModel code running in a
 * back-ground thread to main UI thread, where the actual acquisition of token is
 * interactive, e.g. involves Activity UI. This observable decouples ViewModel
 * from UI so that ViewModel will not hold reference to UI component, holding such
 * reference can lead to memory leak. However the observer of this observable can hold
 * UI reference, because the Observer is removed when UI that life time of the Observer
 * is associated with is in DESTROYED state.
 */
public final class TokenRequestObservable {
    /**
     * The LiveData to notify the token request to a LifeCycle aware Observer registered
     * through {@link TokenRequestObservable#observe(LifecycleOwner, TokenRequestObserver)}
     */
    private final MutableLiveData<TokenRequestHandle> innerObservable;

    /**
     * Creates TokenRequestObservable to send the token request and observe for it.
     */
    public TokenRequestObservable() {
        this.innerObservable = new MutableLiveData<>();
    }

    /**
     * Emits a token request to a {@link TokenRequestObserver} observer registered through
     * {@link TokenRequestObservable#observe(LifecycleOwner, TokenRequestObserver)}.
     *
     * The Observer's {@link TokenRequestObserver#onTokenRequest(String[], TokenResponseCallback)} (TokenResponseCallback)}
     * will be executed in the main UI thread.
     *
     * @param scopes the scopes required for the token
     * @return the Handle that the caller can used to synchronously receives an access-token produced
     * by the main UI thread.
     *
     * @throws if this method is called from main UI thread
     */
    public TokenRequestHandle sendRequest(List<String> scopes) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // The TokenRequestObservable is designed for sending request from background thread
            // to main UI thread. This block validates sendRequest is not called from main UI thread
            // as such a call will result in dead lock once caller wait on the returned handle.
            throw new IllegalStateException("Cannot invoke sendRequest on the main UI Thread");
        }
        TokenRequestHandle requestMessage = new TokenRequestHandle(scopes);
        this.innerObservable.postValue(requestMessage);
        return requestMessage;
    }

    /**
     * Register an Observer that listen for token request Event and associate the Observer
     * with the lifecycle of the given owner.
     *
     * An event will be delivered to the Observer only if the owner (e.g. Activity, View)
     * is in active state i.e. when it is in either STARTED or RESUMED states.
     *
     * When the owner reaches DESTROYED state right before calling owner::onDestroy(),
     * the observer will be removed and ready for GC collection.
     * https://developer.android.com/reference/androidx/lifecycle/Lifecycle.State.html#DESTROYED
     * https://developer.android.com/topic/libraries/architecture/livedata#work_livedata
     * This guarantees that any reference to UI components (e.g. Activity) that the Observer
     * is holding will be released.
     *
     * @param owner the owner whose lifecycle that the observer needs to be aware of
     * @param observer the token request observer
     */
    public void observe(LifecycleOwner owner, TokenRequestObserver observer) {
        // It's "VERY IMPORTANT" that this TokenRequestObservable is NOT holding reference
        // to LifecycleOwner or TokenRequestObserver parameters, doing so can result in
        // memory leaks.
        //
        this.innerObservable.observe(owner, observer);
    }
}
