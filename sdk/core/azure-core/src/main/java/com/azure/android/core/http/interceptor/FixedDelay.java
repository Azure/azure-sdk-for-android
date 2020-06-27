package com.azure.android.core.http.interceptor;

import org.threeten.bp.Duration;

import java.util.Objects;

import okhttp3.Response;

public class FixedDelay implements RetryStrategy {
    private final int maxRetries;
    private final Duration delay;

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
