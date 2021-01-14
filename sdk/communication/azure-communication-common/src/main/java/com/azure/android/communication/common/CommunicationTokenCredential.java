// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.credential.AccessToken;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The Azure Communication Services User token credential.
 * <p>
 * This class is used to cache/refresh the access token required by Azure Communication Services.
 */
public class CommunicationTokenCredential {
    private UserCredential userCredential;

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
     * Creates a {@link CommunicationTokenCredential} that may automatically refresh the token
     * with a provided {@link java.util.concurrent.Callable} on a background thread.
     * <p>
     * If an initial token is supplied in the options, this token will be cached.     *
     *
     * <ul>
     * <li>If {@Code refreshProactively} is {@code false}, When {@link #getToken()} is called:
     *      <ul>
     *          <li>If an initial token was supplied and it is not expired, the initial token will be returned</li>
     *          <li>If an initial token was not supplied or it is expired, a new token will be fetched and returned.
     *          Newly fetched token will be cached until its expiry.</li>
     *      </ul>
     * </li>
     * <li>If {@Code refreshProactively} is {@code true}:
     *      <ul>
     *          <li>If an initial token was supplied, a background thread is scheduled to update the token
     *          when the difference between the current time and token expiry time is less than 600s.</li>
     *          <li>If an initial token was not supplied, a new token will be fetched and cached by a background thread.
     *          A background thread is scheduled to update the token when the difference between the current time
     *          and cached token expiry time is less than 600s.</li>
     *      </ul>
     * </li>
     * </ul>
     *
     * @param tokenRefreshOptions Token refresh options that contains a non-null {@link java.util.concurrent.Callable}
     *          with optional initial token.
     */
    public CommunicationTokenCredential(CommunicationTokenRefreshOptions tokenRefreshOptions) {
        this.userCredential = new AutoRefreshUserCredential(tokenRefreshOptions.getTokenRefresher(),
            tokenRefreshOptions.getRefreshProactively(),
            tokenRefreshOptions.getToken());
    }

    /**
     * Get Azure core access token from credential
     * <p>
     * This method returns an asynchronous {@link java.util.concurrent.Future} with the AccessToken.
     * When the {@link CommunicationTokenCredential} is constructed with a <code>tokenRefresher</code> {@link java.util.concurrent.Callable},
     * the AccessToken will automatically be updated as part of the {@link java.util.concurrent.Future} if the cached token exceeds the expiry threshold.
     * <p>
     * If this method is called after {@link #dispose()} has been invoked, a cancelled {@link java.util.concurrent.Future} will be returned.
     *
     * @return Asynchronous {@link java.util.concurrent.Future} with the AccessToken
     */
    public Future<AccessToken> getToken() {
        if (this.userCredential.isDisposed()) {
            return new CancelledTokenFuture();
        }
        return this.userCredential.getToken();
    }

    /**
     * Invalidates the {@link CommunicationTokenCredential} instance to free up resources for garbage collection.
     */
    public void dispose() {
        this.userCredential.dispose();
    }

    private final class CancelledTokenFuture implements Future<AccessToken> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public AccessToken get() {
            throw new CancellationException();
        }

        @Override
        public AccessToken get(long timeout, TimeUnit unit) {
            throw new CancellationException();
        }
    }
}
