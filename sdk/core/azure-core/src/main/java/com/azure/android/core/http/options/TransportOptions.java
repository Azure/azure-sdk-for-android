// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.RetryStrategy;

import org.threeten.bp.Duration;

/**
 * Options for configuring calls made by a {@link ServiceClient}.
 */
public class TransportOptions {
    // TODO: Implement timeout logic in CancellationToken.
    @NonNull
    private final Duration timeout;
    @Nullable
    private final RetryStrategy retryStrategy;

    /**
     * Creates an instance of {@link TransportOptions}.
     *
     * @param timeout       Default timeout on any network call. 0 (zero) means there is no timeout.
     * @param retryStrategy The {@link RetryStrategy} to be used for calls made by the {@link ServiceClient}.
     */
    public TransportOptions(@Nullable Duration timeout, @Nullable RetryStrategy retryStrategy) {
        this.timeout = timeout == null ? Duration.ZERO : timeout;
        this.retryStrategy = retryStrategy;
    }

    /**
     * Gets the default timeout for calls made by the {@link ServiceClient}.
     *
     * @return the default timeout in seconds. 0 (zero) means there is no timeout.
     */
    @NonNull
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Gets the {@link RetryStrategy} to be used for calls made by the {@link ServiceClient}.
     *
     * @return The {@link RetryStrategy}.
     */
    @Nullable
    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }
}
