// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * Callback to receive a service operation result.
 *
 * @param <T> The type of the result.
 */
public interface Callback<T> {
    /**
     * The method to call on a successful result.
     *
     * @param response The response.
     */
    void onResponse(T response);

    /**
     * The method to call on failure.
     *
     * @param t A throwable with the failure details.
     */
    void onFailure(Throwable t);
}
