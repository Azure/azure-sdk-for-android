// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import com.azure.android.core.util.Context;
import com.azure.android.core.util.DateTimeRfc1123;

import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.concurrent.CancellationException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private final RetryStrategy retryStrategy;

    public RetryInterceptor(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    public static RetryInterceptor fixedDelay(int maxRetries, Duration delay) {
        return new RetryInterceptor(new FixedDelay(maxRetries, delay));
    }

    public static RetryInterceptor exponentialBackoff() {
        return new RetryInterceptor(new ExponentialBackoff());
    }

    public static RetryInterceptor exponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        return new RetryInterceptor(new ExponentialBackoff(maxRetries, baseDelay, maxDelay));
    }

    @Override
    public Response intercept(Chain chain) {
        final Request request = chain.request();
        final Context context = this.getContext(request);

        int retryAttempts = 0;
        final int maxRetries = this.retryStrategy.getMaxRetries();
        do {

            if (context.isCancelled()) {
                throw new CancellationException("The service call is cancelled.");
            }

            Response response = null;
            Exception exception = null;

            try {
                response = chain.proceed(request);
            } catch (Exception e) {
                exception = e;
            }

            if (!this.shouldRetry(response, exception, retryAttempts)) {
                if (exception != null) {
                    throw new RuntimeException(exception);
                }
                return response;
            } else {
                final Duration duration;
                try {
                    duration = this.calculateRetryDelay(response, exception, retryAttempts);
                } finally {
                    if (response != null) {
                        response.close();
                    }
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

    private Context getContext(Request request) {
        final Object tag = request.tag();
        if (tag != null && tag instanceof Context) {
            return  (Context) tag;
        } else {
            return Context.NONE;
        }
    }

    private boolean shouldRetry(Response response, Exception exception, int retryAttempts) {
        return retryAttempts < this.retryStrategy.getMaxRetries()
            && this.retryStrategy.shouldRetry(response, exception, retryAttempts);
    }
}
