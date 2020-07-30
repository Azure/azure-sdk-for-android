// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import org.threeten.bp.Duration;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import okhttp3.Response;

/**
 * The retry strategy with full jitter backoff.
 */
public final class ExponentialBackoff implements RetryStrategy {
    private static final double JITTER_FACTOR = 0.05;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);

    private final int maxRetries;
    private final Duration baseDelay;
    private final Duration maxDelay;

    /**
     * Creates {@link ExponentialBackoff} retry strategy with default settings.
     *
     * <p>
     * The default maximum number of times to retry is 3, the default base delay
     * is 800 milliseconds and default maximum backoff delay before a retry is 8 seconds.
     */
    public ExponentialBackoff() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * Creates {@link ExponentialBackoff} retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param baseDelay The delay used as the coefficient for backoffs, also the same delay will be
     *                 used for the first backoff.
     * @param maxDelay The maximum backoff delay before a retry
     */
    public ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("'maxRetries' cannot be less than 0.");
        }
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");

        if (baseDelay.isZero()) {
            throw new IllegalArgumentException("'baseDelay' cannot be 0.");
        }

        if (baseDelay.compareTo(maxDelay) > 0) {
            throw new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'.");
        }
        this.maxRetries = maxRetries;
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    @Override
    public int getMaxRetries() {
        return this.maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(Response response, Exception exception, int retryAttempts) {
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (baseDelay.toNanos() * (1 - JITTER_FACTOR)),
                (long) (baseDelay.toNanos() * (1 + JITTER_FACTOR)));
        Duration delay = Duration.ofNanos(Math.min((1 << retryAttempts) * delayWithJitterInNanos,
            maxDelay.toNanos()));
        return delay;
    }
}
