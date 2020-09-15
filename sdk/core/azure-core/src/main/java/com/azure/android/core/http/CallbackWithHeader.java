// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * Callback to receive a service operation result.
 *
 * @param <T> The type of the result.
 * @param <T> The type of the header.
 */
public interface CallbackWithHeader<T, H> {
    /**
     * The method to call on a successful result.
     *
     * @param result   The result.
     * @param header   The custom header value.
     * @param response The response.
     */
    void onSuccess(T result, H header, okhttp3.Response response);

    /**
     * The method to call on failure.
     *
     * @param throwable A throwable with the failure details.
     * @param response  The response, if available for the failure.
     */
    void onFailure(Throwable throwable, okhttp3.Response response);
}
