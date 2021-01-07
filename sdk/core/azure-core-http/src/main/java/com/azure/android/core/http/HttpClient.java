// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.http.implementation.HttpClientProviders;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Gets the dispatcher to dispatch async HTTP calls.
     *
     * @return The HTTP call dispatcher.
     */
    HttpCallDispatcher getHttpCallDispatcher();

    /**
     * Send the provided request asynchronously.
     *
     * @param httpRequest The HTTP request to send.
     * @param httpCallback The HTTP callback to notify the result.
     */
    void send(HttpRequest httpRequest, HttpCallback httpCallback);

    /**
     * Create default {@link com.azure.android.core.http.HttpClient} instance.
     *
     * @return A new instance of the {@link com.azure.android.core.http.HttpClient}.
     */
    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }
}
