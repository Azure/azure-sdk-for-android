// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpResponse;

import org.threeten.bp.Duration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeoutException;

/**
 * The interface for determining the retry strategy used in {@link RetryPolicy}.
 */
public interface RetryStrategy {
    /**
     * Get the maximum number of times to retry.
     *
     * @return The maximum number of times to retry.
     */
    int getMaxRetries();

    /**
     * Determines any retry should be performed.
     *
     * @param response The HTTP response.
     * @param error The pipeline exception, if any.
     * @param retryAttempts The number of retry attempts so far made.
     * @return True to retry, false to exit retry loop.
     */
    default boolean shouldRetry(HttpResponse response, Throwable error, int retryAttempts) {
        if (error != null) {
            return error instanceof IOException
                || error instanceof TimeoutException;
        } else {
            if (response != null) {
                final int code = response.getStatusCode();
                return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                    || code == 429 // to-many requests
                    || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                    && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                    && code != HttpURLConnection.HTTP_VERSION));
            } else {
                return true;
            }
        }
    }

    /**
     * Determines the delay duration that should be waited before retrying.
     *
     * @param response The HTTP response.
     * @param error The pipeline exception, if any.
     * @param retryAttempts The number of retry attempts so far made.
     * @return The delay duration.
     */
    Duration calculateRetryDelay(HttpResponse response, Throwable error, int retryAttempts);
}