// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.implementation.Util;
import com.azure.android.core.micro.util.Context;

import java.io.IOException;
import java.time.Duration; // TODO: anuchan: use threetenbp or old native time.
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Pipeline interceptor that retries when a recoverable exception or HTTP error occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private final RetryStrategy retryStrategy;

    /**
     * Creates {@link RetryPolicy} with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    /**
     * Get an instance of {@link RetryPolicy} that uses fixed backoff delay retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param delay The fixed backoff delay applied before every retry.
     * @return The retry interceptor.
     */
    public static RetryPolicy withFixedDelay(int maxRetries, Duration delay) {
        return new RetryPolicy(new FixedDelay(maxRetries, delay));
    }

    /**
     * Get an instance of {@link RetryPolicy} that uses a default full jitter backoff
     * retry strategy.
     *
     * <p>
     * The retry strategy by default retries maximum 3 times, uses 800 milliseconds as
     * the default base delay and uses 8 seconds as default maximum backoff delay before a retry.
     *
     * @return The retry interceptor.
     */
    public static RetryPolicy withExponentialBackoff() {
        return new RetryPolicy(new ExponentialBackoff());
    }

    /**
     * Get an instance of {@link RetryPolicy} that uses full jitter backoff retry strategy.
     *
     * @param maxRetries The maximum number of times to retry.
     * @param baseDelay The delay used as the coefficient for backoffs, also baseDelay will be the first backoff delay.
     * @param maxDelay The maximum backoff delay before a retry.
     * @return The retry interceptor.
     */
    public static RetryPolicy withExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        return new RetryPolicy(new ExponentialBackoff(maxRetries, baseDelay, maxDelay));
    }

    /**
     * Intercepts any exception in the pipeline or the HTTP response error and, if recoverable, retries sending
     * the request.

     * @param chain Provides access to the request to send.
     */
    @Override
    public void process(HttpPipelinePolicyChain chain, Context context) {
        this.attempt(chain, null, 0);
    }

    private void attempt(HttpPipelinePolicyChain chain, Duration delay, final int retryAttempts) {
        final HttpRequest httpRequest = chain.getRequest();

        // Check for cancellation before Proceeding the chain.
        if (httpRequest.getCancellationToken().isCancellationRequested()) {
            chain.finishedProcessing(new IOException("Canceled."));
            return;
        }

        if (delay == null) {
            chain.processNextPolicy(httpRequest, new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    retryIfRequired(chain, response, null, retryAttempts);
                }

                @Override
                public void onError(Throwable error) {
                    retryIfRequired(chain, null, error, retryAttempts);
                }
            });
        } else {
            chain.processNextPolicy(httpRequest, new HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    retryIfRequired(chain, response, null, retryAttempts);
                }

                @Override
                public void onError(Throwable error) {
                    retryIfRequired(chain, null, error, retryAttempts);
                }
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void retryIfRequired(HttpPipelinePolicyChain chain,
                                 HttpResponse response,
                                 Throwable error,
                                 final int retryAttempts) {
        // Check for cancellation before retry.
        if (chain.getRequest().getCancellationToken().isCancellationRequested()) {
            if (response != null) {
                // Close the current response before propagating Cancelled Error.
                response.close();
            }
            chain.finishedProcessing(new IOException("Canceled."));
            return;
        }

        if (shouldRetry(response, error, retryAttempts)) {
            Duration delay = null;
            Throwable userError = null;
            try {
                delay = calculateRetryDelay(response, null, retryAttempts);
            } catch (Throwable e) {
                userError = e;
            } finally {
                if (response != null) {
                    response.close();
                }
            }

            if (userError != null) {
                chain.finishedProcessing(userError);
            } else {
                attempt(chain, delay, retryAttempts + 1);
            }
        } else {
            if (response != null) {
                chain.finishedProcessing(response);
            } else {
                if (retryAttempts >= this.retryStrategy.getMaxRetries()) {
                    final RuntimeException maxRetriedError = new RuntimeException(
                        String.format("The max retries (%d times) for the service call is exceeded.",
                            this.retryStrategy.getMaxRetries()));
                    if (error != null) {
                        maxRetriedError.addSuppressed(error);
                    }
                    chain.finishedProcessing(maxRetriedError);
                } else {
                    chain.finishedProcessing(error);
                }
            }
        }
    }

    /**
     * Determines the delay duration that should be waited before retrying.
     *
     * @param response The HTTP response.
     * @param exception The pipeline exception, if any.
     * @param retryAttempts The number of retry attempts so far made.
     * @return The delay duration.
     */
    public Duration calculateRetryDelay(HttpResponse response, Throwable exception, int retryAttempts) {
        if (exception != null) {
            return this.retryStrategy.calculateRetryDelay(null, exception, retryAttempts);
        } else {
            final int code = response.getStatusCode();
            if (code == 429) {
                // Too Many Requests.
                // https://docs.microsoft.com/en-us/rest/api/cosmos-db/common-cosmosdb-rest-response-headers
                final String retryAfterHeader = response.getHeaderValue("x-ms-retry-after-ms");
                if (retryAfterHeader != null) {
                    return Duration.of(Integer.parseInt(retryAfterHeader), ChronoUnit.MILLIS);
                }
            }

            if (code == 429 || code == 503) {
                // Too Many Requests OR Service Unavailable
                // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
                final String retryAfterHeader = response.getHeaderValue("Retry-After");
                if (retryAfterHeader != null) {
                    OffsetDateTime retryWhen = null;
                    try {
                        retryWhen = Util.parseRfc1123Time(retryAfterHeader);
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
     *ß
     * @param response The HTTP response.
     * @param error The pipeline exception, if any.
     * @param retryAttempts The number of retry attempts so far made.
     * @return True to retry, false to exit retry loop.
     */
    private boolean shouldRetry(HttpResponse response, Throwable error, int retryAttempts) {
        return retryAttempts < this.retryStrategy.getMaxRetries()
            && this.retryStrategy.shouldRetry(response, error, retryAttempts);
    }
}
