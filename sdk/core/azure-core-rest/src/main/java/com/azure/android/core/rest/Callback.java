// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

/**
 * The callback type to notify the result of a REST call.
 */
public interface Callback<T> {
    /**
     * Called when the call to the REST endpoint is successfully completed.
     *
     * @param response The response for the REST call.
     */
    void onSuccess(T response);
    /**
     * Called when the REST call could not be executed due an error.
     *
     * @param error The reason for call failure.
     */
    void onFailure(Throwable error);
}
