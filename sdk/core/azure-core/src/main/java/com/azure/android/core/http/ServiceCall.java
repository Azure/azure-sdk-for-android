// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.util.Context;

/**
 * Type representing handle to the service call.
 */
public final class ServiceCall {
    private final retrofit2.Call<?> call;
    private final Context context;

    public ServiceCall(retrofit2.Call<?> call, Context context) {
        this.call = call;
        this.context = context;
    }

    /**
     * Cancel this call. An attempt will be made to cancel in-flight calls, and if the call has not
     * yet been executed it never will be.
     */
    public void cancel() {
        this.call.cancel();
        this.context.cancel();
    }

    /**
     * Check whether {@link #cancel()} was called.
     *
     * @return true If {@link #cancel()} was called.
     */
    public boolean isCanceled() {
        return this.call.isCanceled();
    }
}
