// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.internal.util.ResultTaskImpl;
import com.azure.android.core.util.Context;

/**
 * Type representing handle to the service call.
 */
public final class ServiceCall {
    private final ResultTaskImpl<?> resultTaskImpl;

    public ServiceCall(retrofit2.Call<?> call, Context context) {
        this.resultTaskImpl = new ResultTaskImpl<>(() -> { // create with lambda to execute on cancel() call.
            call.cancel();
            context.cancel();
        });
    }

    /**
     * Cancel this call. An attempt will be made to cancel in-flight calls, and if the call has not
     * yet been executed it never will be.
     */
    public void cancel() {
        this.resultTaskImpl.cancel();
    }

    /**
     * Check whether {@link #cancel()} was called.
     *
     * @return true If {@link #cancel()} was called.
     */
    public boolean isCanceled() {
        return this.resultTaskImpl.isCanceled();
    }
}
