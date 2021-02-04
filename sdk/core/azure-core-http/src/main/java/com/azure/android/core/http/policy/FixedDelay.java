// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * The retry strategy with fixed backoff delay.
 */
public class FixedDelay implements RetryStrategy {
    private final ClientLogger logger = new ClientLogger(FixedDelay.class);

    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates {@link FixedDelay} retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param delay The fixed backoff delay applied before every retry.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0.
     */
    public FixedDelay(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'maxRetries' cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

    @Override
    public int getMaxRetries() {
        return this.maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(HttpResponse response, Throwable error, int retryAttempts) {
        return this.delay;
    }
}
