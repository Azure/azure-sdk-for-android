// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * Type representing handle to the service call.
 */
public interface ServiceCallTask<T> {
    void cancel();

    boolean isCanceled();

    void enqueue(@NonNull Callback<T> callback);

    T execute() throws IOException;
}
