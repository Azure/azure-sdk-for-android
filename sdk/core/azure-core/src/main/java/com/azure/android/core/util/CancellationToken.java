// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import androidx.annotation.RestrictTo;

import com.azure.android.core.internal.util.CancellationTokenImpl;

/**
 * Type representing a token to cancel one or more operations.
 */
public abstract class CancellationToken {
    /**
     * An empty CancellationToken that cannot be cancelled.
     */
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
     * Communicates a request for cancellation.
     */
    public abstract void cancel();

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    protected CancellationToken() {
        if (!(this instanceof CancellationTokenImpl)) {
            throw new IllegalStateException("Use CancellationToken.create() factory method to obtain a CancellationToken.");
        }
    }
}
