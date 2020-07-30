// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import org.threeten.bp.Duration;

import java.util.Objects;

import okhttp3.Response;

/**
 * The retry strategy with fixed backoff delay.
 */
public class FixedDelay implements RetryStrategy {
    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates {@link FixedDelay} retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param delay The fixed backoff delay applied before every retry.
     */
    public FixedDelay(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("'maxRetries' cannot be less than 0.");
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

    @Override
    public int getMaxRetries() {
        return this.maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(Response response, Exception exception, int retryAttempts) {
        return this.delay;
    }
}
