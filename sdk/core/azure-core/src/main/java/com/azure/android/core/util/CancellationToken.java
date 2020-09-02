// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.azure.android.core.internal.util.CancellationTokenImpl;

import org.threeten.bp.Duration;

/**
 * Type representing a token to cancel one or more operations.
 */
public abstract class CancellationToken {
    protected Duration timeout = Duration.ZERO;

    /**
     * An empty CancellationToken that cannot be cancelled.
     */
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final CancellationToken NONE = new CancellationTokenImpl();

    /**
     * Creates a new {@link CancellationToken}.
     *
     * @return The {@link CancellationToken}.
     */
    public static CancellationToken create() {
        return new CancellationTokenImpl();
    }

    /**
     * Creates a new {@link CancellationToken} with a given timeout. 0 (zero) means there is no timeout.
     *
     * @return The {@link CancellationToken}.
     */
    public static CancellationToken createWithTimeout(@NonNull Duration timeout) {
        return new CancellationTokenImpl(timeout);
    }

    /**
     * Communicates a request for cancellation.
     */
    public abstract void cancel();

    /**
     * Start the cancellation token countdown. If the countdown is already running, this should return immediately.
     */
    public abstract void start();

    /**
     * Reset the {@link CancellationToken} and allow the timeout countdown to be restarted.
     */
    public abstract void reset();

    /**
     * Gets this {@link CancellationToken}'s timeout in seconds.
     */
    public Duration getTimeout() {
        return timeout;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    protected CancellationToken() {
        if (!(this instanceof CancellationTokenImpl)) {
            throw new IllegalStateException(
                "Use CancellationToken.create() factory method to obtain a CancellationToken.");
        }
    }
}
