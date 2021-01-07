// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * The callback type to notify the result of an HTTP call.
 */
public interface HttpCallback {
    /**
     * Called when the {@link HttpResponse} is successfully returned by the HTTP server.
     *
     * <p>
     * Receiving a {@link HttpResponse} successfully does not necessarily indicate application-layer
     * success; the response {@code statusCode} may still indicate an application-layer failure with
     * error-codes such as 404 or 500.
     * </p>
     *
     * @param response The response for the HTTP call.
     */
    void onSuccess(HttpResponse response);

    /**
     * Called when the {@link HttpRequest} call could not be executed due an error.
     *
     * <p>
     * It is possible that the HTTP server received the request before the failure; examples for
     * such failures are the client-side cancellation of the request written to the wire,
     * the response read timeout, etc.
     * </p>
     *
     * @param error The reason for call failure.
     */
    void onError(Throwable error);
}
