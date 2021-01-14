// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.http.implementation.HttpClientProviders;
import com.azure.android.core.micro.util.CancellationToken;

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
     * @param cancellationToken The cancellation token for the HTTP call, on which
     *     the caller may request cancellation of the {@code request} execution.
     *     Note that honoring cancellation request is best effort; In HttpClient
     *     implementations, once the execution passed the point of no-cancellation,
     *     it will not honor the cancel request. This point of no-cancellation
     *     depends on each HTTP Client implementation, for some HttpClient
     *     implementations cancellation is not at all supported.
     * @param httpCallback The HTTP callback to notify the result.
     */
    void send(HttpRequest httpRequest,
              CancellationToken cancellationToken,
              HttpCallback httpCallback);

    /**
     * Create default {@link HttpClient} instance.
     *
     * @return A new instance of the {@link HttpClient}.
     */
    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }
}
