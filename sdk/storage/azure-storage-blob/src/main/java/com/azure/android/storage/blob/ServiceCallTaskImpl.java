// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceCallTask;
import com.azure.android.core.internal.util.ResultTaskImpl;
import com.azure.android.core.util.Context;

import java.util.concurrent.Executor;

final class ServiceCallTaskImpl<T> implements ServiceCallTask<T> {
    private final ResultTaskImpl<T> resultTaskImpl;
    private final Executor defaultCallbackExecutor;

    ServiceCallTaskImpl(Executor executor) {
        this.resultTaskImpl = new ResultTaskImpl<>();
        this.defaultCallbackExecutor = executor;
    }

    @Override
    public void cancel() {
        this.resultTaskImpl.cancel();
    }

    @Override
    public boolean isCanceled() {
        return this.resultTaskImpl.isCanceled();
    }

    @Override
    public void addCallback(@NonNull Callback<T> callback, @NonNull Executor executor) {
        this.resultTaskImpl.addCallback(callback, executor);
    }

    @Override
    public void addCallback(@NonNull Callback<T> callback) {
        this.addCallback(callback, this.defaultCallbackExecutor);
    }

    // package private
    void setSucceeded(@Nullable T result) {
        this.resultTaskImpl.setSucceeded(result);
    }

    // package private
    void setFailed(@NonNull Throwable throwable) {
        this.resultTaskImpl.setFailed(throwable);
    }

    // package private
    void setupCancel(retrofit2.Call<?> call, Context context) {
        this.resultTaskImpl.setCancelCallback(() -> {
            call.cancel();
            context.cancel();
        });
    }

    // package private
    <U> ServiceCallTask<U> map(Function<T, U> mapper) {
        final ServiceCallTaskImpl<T> self = this;
        return new ServiceCallTask<U>() {
            @Override
            public void cancel() {
                self.cancel();
            }

            @Override
            public boolean isCanceled() {
                return self.isCanceled();
            }

            @Override
            public void addCallback(@NonNull Callback<U> callback, @NonNull Executor executor) {
                self.addCallback(new Callback<T>() {
                    @Override
                    public void onResponse(T response) {
                        callback.onResponse(mapper.apply(response));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        callback.onFailure(t);
                    }
                }, executor);
            }

            @Override
            public void addCallback(@NonNull Callback<U> callback) {
                this.addCallback(callback, self.defaultCallbackExecutor);
            }
        };
    }

    @FunctionalInterface
    interface Function<T, R> {
        R apply(T t);
    }
}
