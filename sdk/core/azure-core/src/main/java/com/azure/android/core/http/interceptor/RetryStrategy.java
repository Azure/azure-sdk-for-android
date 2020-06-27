// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import org.threeten.bp.Duration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeoutException;

import okhttp3.Response;

public interface RetryStrategy {
    int getMaxRetries();

    default boolean shouldRetry(Response response, Exception exception, int retryAttempts) {
        if (exception != null) {
            return exception instanceof IOException
                || exception instanceof TimeoutException;
        } else {
            final int code = response.code();
            return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                || code == 429 // to-many requests
                || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                && code != HttpURLConnection.HTTP_VERSION));
        }
    }

    Duration calculateRetryDelay(Response response, Exception exception, int retryAttempts);
}
