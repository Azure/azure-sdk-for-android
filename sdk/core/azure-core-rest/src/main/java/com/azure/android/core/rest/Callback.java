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
     * Called when the REST call is failed.
     *
     * <p>
     * The client-side error, error response from the network layer or REST endpoint
     * is reported as REST call failure. Examples of client-side errors include
     * input validation error, cancellation of the request, the response read timeout, etc.
     * </p>
     *
     * @param error The reason for call failure.
     */
    void onFailure(Throwable error);
}