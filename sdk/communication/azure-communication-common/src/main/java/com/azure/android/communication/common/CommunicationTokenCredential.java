// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.logging.ClientLogger;

import java9.util.concurrent.CompletableFuture;

/**
 * The Azure Communication Services User token credential.
 * <p>
 * This class is used to cache/refresh the access token required by Azure Communication Services.
 */
public final class CommunicationTokenCredential {
    private final ClientLogger logger = new ClientLogger(CommunicationTokenCredential.class);
    private final UserCredential userCredential;

    /**
     * Creates a {@link CommunicationTokenCredential} from the provided token string.
     * <p>
     * The same token will be returned whenever {@link #getToken()} is called.
     *
     * @param userToken token string for initialization
     */
    public CommunicationTokenCredential(String userToken) {
        this.userCredential = new StaticUserCredential(userToken);
    }

    /**
     * Creates a {@link CommunicationTokenCredential} that automatically refreshes the token
     * with a provided {@link java.util.concurrent.Callable} on a background thread.
     * <p>
     * The cached token is updated if {@link #getToken()} is called and if the difference between the current time
     * and token expiry time is less than 120s.
     * <p>
     * If {@code refreshProactively} is {@code true}:
     * <ul>
     *     <li>The cached token will be updated in the background when the difference between the current time and
     *     token expiry time is less than 600s.</li>
     *     <li>The cached token will be updated immediately when the constructor is invoked
     *     and <code>initialToken</code> is expired</li>
     * </ul>
     *
     * @param tokenRefreshOptions Options object that contains token refresher, initial token string,
     *                            and refreshProactively
     */
    public CommunicationTokenCredential(CommunicationTokenRefreshOptions tokenRefreshOptions) {
        this.userCredential = new AutoRefreshUserCredential(
            tokenRefreshOptions.getTokenRefresher(),
            tokenRefreshOptions.isRefreshProactively(),
            tokenRefreshOptions.getInitialToken());
    }


    /**
     * Get Azure core access token from credential
     * <p>
     * This method returns an asynchronous {@link CompletableFuture} with the AccessToken.
     * When the {@link CommunicationTokenCredential} is constructed with a <code>tokenRefresher</code>
     * {@link java.util.concurrent.Callable}, the AccessToken will automatically be updated as part of
     * the {@link CompletableFuture} if the cached token exceeds the expiry threshold.
     * <p>
     * If this method is called after {@link #dispose()} has been invoked, a failed
     * {@link CompletableFuture} that emits {@link IllegalStateException} will be returned.
     *
     * @return Asynchronous {@link CompletableFuture} with the AccessToken
     */
    public CompletableFuture<CommunicationAccessToken> getToken() {
        if (this.userCredential.isDisposed()) {
            return CompletableFuture.failedFuture(
                logger.logExceptionAsError(new IllegalStateException("UserCredential has been disposed.")));
        }
        return this.userCredential.getToken();
    }

    /**
     * Invalidates the {@link CommunicationTokenCredential} instance to free up resources for garbage collection.
     */
    public void dispose() {
        this.userCredential.dispose();
    }
}