// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.implementation.Util;
import com.azure.android.core.logging.ClientLogger;

import org.threeten.bp.Duration;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The retry strategy with full jitter backoff.
 */
public final class ExponentialBackoff implements RetryStrategy {
    private final ClientLogger logger = new ClientLogger(ExponentialBackoff.class);

    private static final Random RANDOM;
    private static final double JITTER_FACTOR = 0.05;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);

    private final int maxRetries;
    private final Duration baseDelay;
    private final Duration maxDelay;

    static {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RANDOM = null;
        } else {
            RANDOM = new Random();
        }
    }

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
     * @throws IllegalArgumentException if the {@code maxRetries} is less than 0 or if the {@code baseDelay}
     *     is zero or greater than {@code maxDelay}.
     */
    public ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        if (maxRetries < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'maxRetries' cannot be less than 0."));
        }
        Util.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Util.requireNonNull(maxDelay, "'maxDelay' cannot be null.");

        if (baseDelay.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be 0."));
        }

        if (baseDelay.compareTo(maxDelay) > 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
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
    public Duration calculateRetryDelay(HttpResponse response, Throwable error, int retryAttempts) {
        final long delayWithJitterInNanos;
        final long origin = (long) (baseDelay.toNanos() * (1 - JITTER_FACTOR));
        final long bound = (long) (baseDelay.toNanos() * (1 + JITTER_FACTOR));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            delayWithJitterInNanos = ThreadLocalRandom.current().nextLong(origin, bound);
        } else {
            // https://developer.android.com/reference/java/util/Random#longs(long,%20long)
            long r = RANDOM.nextLong();
            long n = bound - origin, m = n - 1;
            if ((n & m) == 0L) {
                r = (r & m) + origin;
            } else if (n > 0L) {
                long u = r >>> 1;
                r = u % n;
                while (u + m - r < 0L) {
                    u = RANDOM.nextLong() >>> 1;
                    r = u % n;
                }
                r += origin;
            } else {
                while (r < origin || r >= bound) {
                    r = RANDOM.nextLong();
                }
            }
            delayWithJitterInNanos = r;
        }

        Duration delay = Duration.ofNanos(Math.min((1 << retryAttempts) * delayWithJitterInNanos,
            maxDelay.toNanos()));
        return delay;
    }
}
