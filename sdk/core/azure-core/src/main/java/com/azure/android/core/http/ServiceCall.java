// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import retrofit2.Call;

/**
 * Type representing handle to the service call.
 */
public final class ServiceCall {
    private final Call<?> call;

    /**
     * Creates a service call.
     *
     * @param call the retrofit {@link Call} backing the service call.
     */
    public ServiceCall(Call<?> call) {
        this.call = call;
    }

    /**
     * Cancel this call. An attempt will be made to cancel in-flight calls, and if the call has not
     * yet been executed it never will be.
     */
    public void cancel() {
        this.call.cancel();
    }

    /**
     * Check whether {@link #cancel()} was called.
     *
     * @return true if {@link #cancel()} was called.
     */
    public boolean isCanceled() {
        return this.call.isCanceled();
    }
}
