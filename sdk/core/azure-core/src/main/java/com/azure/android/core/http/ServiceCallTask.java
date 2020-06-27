// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Type representing handle to the service call.
 */
public interface ServiceCallTask<T> {
    void cancel();

    boolean isCanceled();

    void addCallback(@NonNull Callback<T> callback, @NonNull Executor executor);

    void addCallback(@NonNull Callback<T> callback);
}
