// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.internal.util.ExceptionUtils;
import com.azure.android.core.util.DateTimeRfc1123;

import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Pipeline interceptor that retries when a recoverable exception or HTTP error occurs.
 */
public class RetryInterceptor implements Interceptor {
    private final RetryStrategy retryStrategy;

    /**
     * Creates {@link RetryInterceptor} with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     */
    public RetryInterceptor(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    /**
     * Get an instance of {@link RetryInterceptor} that uses fixed backoff delay retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param delay The fixed backoff delay applied before every retry.
     * @return The retry interceptor.
     */
    public static RetryInterceptor withFixedDelay(int maxRetries, Duration delay) {
        return new RetryInterceptor(new FixedDelay(maxRetries, delay));
    }

    /**
     * Get an instance of {@link RetryInterceptor} that uses a default full jitter backoff
     * retry strategy.
     *
     * <p>
     * The retry strategy by default retries maximum 3 times, uses 800 milliseconds as
     * the default base delay and uses 8 seconds as default maximum backoff delay before a retry.
     *
     * @return The retry interceptor.
     */
    public static RetryInterceptor withExponentialBackoff() {
        return new RetryInterceptor(new ExponentialBackoff());
    }

    /**
     * Get an instance of {@link RetryInterceptor} that uses full jitter backoff retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param baseDelay The delay used as the coefficient for backoffs, also baseDelay will be the first backoff delay.
     * @param maxDelay The maximum backoff delay before a retry.
     * @return The retry interceptor.
     */
    public static RetryInterceptor withExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        return new RetryInterceptor(new ExponentialBackoff(maxRetries, baseDelay, maxDelay));
    }

    /**
     * Intercepts any exception in the pipeline or the HTTP response error and, if recoverable, retries sending the request.

     * @param chain Provides access to the response.
     *
     * @return Response from the next interceptor in the pipeline.
     * @throws IOException If the pipeline gets canceled or an there is an IO error that
     * indicates the request cannot be retried any more, for example, max retry limit reached.
     */
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();

        int retryAttempts = 0;
        final int maxRetries = this.retryStrategy.getMaxRetries();
        do {

            // Check for cancellation before Proceeding the chain.
            if (chain.call().isCanceled()) {
                throw ExceptionUtils.CALL_CANCELLED_IO_EXCEPTION;
            }

            Response response = null;
            Exception exception = null;
            // Proceed.
            try {
                response = chain.proceed(request);
            } catch (Exception e) {
                exception = e;
            }

            // Check for cancellation after Proceed.
            if (chain.call().isCanceled()) {
                try {
                    if (exception != null) {
                        // The later interceptors that execute as a result of the above 'chain.proceed' may throw
                        // IOException("Cancelled") [e.g. okhttp3.internal.http.RetryAndFollowUpInterceptor]
                        // If it is identified that the call is cancelled, we don't want to retry on such cases.
                        if (exception == ExceptionUtils.CALL_CANCELLED_IO_EXCEPTION) {
                            throw ExceptionUtils.CALL_CANCELLED_IO_EXCEPTION;
                        } else {
                            throw new IOException("Cancelled.", exception);
                        }
                    } else {
                        throw ExceptionUtils.CALL_CANCELLED_IO_EXCEPTION;
                    }
                } finally {
                    if (response != null) {
                        // Close the current response before propagating Cancelled Exception.
                        response.close();
                    }
                }
            }

            if (!this.shouldRetry(response, exception, retryAttempts)) {
                if (exception != null) {
                        throw new RuntimeException(exception);
                } else {
                    return response;
                }
            } else {
                final Duration duration;
                try {
                    duration = this.calculateRetryDelay(response, exception, retryAttempts);
                } finally {
                    if (response != null) {
                        // Close the current response before any retry.
                        response.close();
                    }
                }

                // Check for cancellation before going into sleep.
                if (chain.call().isCanceled()) {
                    throw ExceptionUtils.CALL_CANCELLED_IO_EXCEPTION;
                }

                try {
                    Thread.sleep(duration.toMillis());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                retryAttempts++;
            }
        } while (retryAttempts < maxRetries);

        throw new RuntimeException(String.format("The max retries (%d times) for the service call is exceeded.", maxRetries));
    }

    /**
     * Determines the delay duration that should be waited before retrying.
     *
     * @param response The HTTP response.
     * @param exception The pipeline exception, if any.
     * @param retryAttempts The number of retry attempts so far made.
     * @return The delay duration.
     */
    public Duration calculateRetryDelay(Response response, Exception exception, int retryAttempts) {
        if (exception != null) {
            return this.retryStrategy.calculateRetryDelay(null, exception, retryAttempts);
        } else {
            final int code = response.code();
            if (code == 429) {
                final String retryAfterHeader = response.header("x-ms-retry-after-ms");
                if (retryAfterHeader != null) {
                    return Duration.of(Integer.parseInt(retryAfterHeader), ChronoUnit.MILLIS);
                }
            }

            if (code == 429 || code == 503) {
                final String retryAfterHeader = response.header("Retry-After");
                if (retryAfterHeader != null) {
                    OffsetDateTime retryWhen = null;
                    try {
                        retryWhen = new DateTimeRfc1123(retryAfterHeader).getDateTime();
                    } catch (Exception ignored) {
                    }
                    if (retryWhen != null) {
                        return Duration.between(OffsetDateTime.now(), retryWhen);
                    } else {
                        return Duration.of(Integer.parseInt(retryAfterHeader), ChronoUnit.SECONDS);
                    }
                }
            }
            return this.retryStrategy.calculateRetryDelay(response, null, retryAttempts);
        }
    }

    /**
     * Determines any retry should be performed.
     *
     * @param response The HTTP response.
     * @param exception The pipeline exception, if any.
     * @param retryAttempts The number of retry attempts so far made.
     * @return True to retry, false to exit retry loop.
     */
    private boolean shouldRetry(Response response, Exception exception, int retryAttempts) {
        return retryAttempts < this.retryStrategy.getMaxRetries()
            && this.retryStrategy.shouldRetry(response, exception, retryAttempts);
    }
}
