// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.okhttp;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpClient;

import java.util.Objects;

import okhttp3.OkHttpClient;

/**
 * Builder class responsible for creating instances of {@link com.azure.android.core.http.HttpClient} backed by OkHttp.
 */
public class OkHttpAsyncHttpClientBuilder {
    private final okhttp3.OkHttpClient okHttpClient;

    /**
     * Creates OkHttpAsyncHttpClientBuilder.
     */
    public OkHttpAsyncHttpClientBuilder() {
        this.okHttpClient = null;
    }

    /**
     * Creates OkHttpAsyncHttpClientBuilder from the builder of an existing OkHttpClient.
     *
     * @param okHttpClient the httpclient
     */
    public OkHttpAsyncHttpClientBuilder(OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "'okHttpClient' cannot be null.");
    }

    /**
     * Creates a new OkHttp-backed {@link com.azure.android.core.http.HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return A new OkHttp-backed {@link com.azure.android.core.http.HttpClient} instance.
     */
    public HttpClient build() {
        OkHttpClient.Builder httpClientBuilder = this.okHttpClient == null
            ? new OkHttpClient.Builder()
            : this.okHttpClient.newBuilder();

        final OkHttpClient okHttpClient = httpClientBuilder.build();
        final HttpCallDispatcher httpCallDispatcher
            = new HttpCallDispatcher(okHttpClient.dispatcher().executorService());
        httpCallDispatcher.setMaxRunningCalls(okHttpClient.dispatcher().getMaxRequests());

        return new OkHttpAsyncHttpClient(okHttpClient, httpCallDispatcher);
    }
}