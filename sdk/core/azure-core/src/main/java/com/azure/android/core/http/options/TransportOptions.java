package com.azure.android.core.http.options;

import androidx.annotation.Nullable;

import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.RetryStrategy;

public class TransportOptions {
    double timeoutInSeconds;
    @Nullable
    RetryStrategy retryStrategy;

    /**
     * Options for configuring calls made by a {@link ServiceClient}.
     *
     * @param timeoutInSeconds Default timeout on any network call. 0 (zero) means no timeout.
     * @param retryStrategy    The {@link RetryStrategy} to be used for calls made by the {@link ServiceClient}.
     */
    public TransportOptions(double timeoutInSeconds, @Nullable RetryStrategy retryStrategy) {
        this.timeoutInSeconds = timeoutInSeconds;
        this.retryStrategy = retryStrategy;
    }

    /**
     * Gets the default timeout for calls made by the {@link ServiceClient}.
     *
     * @return the default timeout in seconds. 0 (zero) means there is no timeout.
     */
    public double getTimeoutInSeconds() {
        return timeoutInSeconds;
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
