// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.httpurlconnection;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpClient;

import java.util.Objects;

/**
 * Builder class responsible for creating instances of {@link com.azure.android.core.http.HttpClient}
 * backed by HttpUrlConnection.
 */
public class HttpUrlConnectionAsyncHttpClientBuilder {
    private HttpCallDispatcher httpCallDispatcher;

    /**
     * Creates HttpUrlConnectionAsyncHttpClientBuilder.
     */
    public HttpUrlConnectionAsyncHttpClientBuilder() {
    }

    /**
     * Sets the {@link HttpCallDispatcher} to execute the enqueued HTTP calls.
     *
     * @param httpCallDispatcher The HTTP call dispatcher
     * @return The updated HttpUrlConnectionAsyncHttpClientBuilder object.
     */
    public HttpUrlConnectionAsyncHttpClientBuilder setHttpCallDispatcher(HttpCallDispatcher httpCallDispatcher) {
        this.httpCallDispatcher
            = Objects.requireNonNull(httpCallDispatcher, "'httpCallDispatcher' is required.");
        return this;
    }

    /**
     * Creates a new HttpUrlConnection-backed {@link com.azure.android.core.http.HttpClient} instance on every call,
     * using the configuration set in the builder at the time of the build method call.
     *
     * @return A new HttpUrlConnection-backed {@link com.azure.android.core.http.HttpClient} instance.
     */
    public HttpClient build() {
        final HttpCallDispatcher httpCallDispatcher = this.httpCallDispatcher == null
            ? new HttpCallDispatcher()
            : this.httpCallDispatcher;
        return new HttpUrlConnectionAsyncHttpClient(httpCallDispatcher);
    }
}
