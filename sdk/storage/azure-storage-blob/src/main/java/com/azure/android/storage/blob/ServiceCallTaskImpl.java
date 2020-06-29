// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import androidx.annotation.NonNull;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceCallTask;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

final class ServiceCallTaskImpl<T, V> implements ServiceCallTask<T> {
    private final Call<V> innerCall;
    private final Function<Response<V>, T> onResponseMapper;
    private final RuntimeException synchronousError;

    ServiceCallTaskImpl(Call<V> innerCall,
                        Function<Response<V>, T> onResponseMapper) {
        this.innerCall
            = Objects.requireNonNull(innerCall, "'innerCall' is required and cannot be null.");
        this.onResponseMapper
            = Objects.requireNonNull(onResponseMapper, "'onResponseMapper' is required and cannot be null.");
        this.synchronousError = null;
    }

    ServiceCallTaskImpl(RuntimeException synchronousError) {
        this.innerCall = null;
        this.onResponseMapper = responseBodyResponse -> null;
        this.synchronousError = synchronousError;
    }

    @Override
    public void cancel() {
        if (this.synchronousError != null) {
            return;
        }
        this.innerCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        if (this.synchronousError != null) {
            return true;
        }
        return this.innerCall.isCanceled();
    }

    @Override
    public void enqueue(@NonNull Callback<T> callback) {
        if (this.synchronousError != null) {
            callback.onFailure(this.synchronousError);
        } else {
            this.innerCall.enqueue(new retrofit2.Callback<V>() {
                @Override
                public void onResponse(Call<V> call, Response<V> response) {
                    try {
                        T result = onResponseMapper.apply(response);
                        callback.onResponse(result);
                    } catch (Throwable t) {
                        callback.onFailure(t);
                    }
                }

                @Override
                public void onFailure(Call<V> call, Throwable t) {
                    callback.onFailure(t);
                }
            });
        }
    }

    @Override
    public T execute() throws IOException {
        if (this.synchronousError != null) {
            throw this.synchronousError;
        } else {
            Response<V> response = this.innerCall.execute();
            return onResponseMapper.apply(response);
        }
    }

    // package private
    <U> ServiceCallTask<U> map(Function<T, U> mapper) {
        final ServiceCallTaskImpl<T, V> self = this;
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
            public void enqueue(@NonNull Callback<U> callback) {
                self.enqueue(new Callback<T>() {
                    @Override
                    public void onResponse(T response) {
                        callback.onResponse(mapper.apply(response));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        callback.onFailure(t);
                    }
                });
            }

            @Override
            public U execute() throws IOException {
                T innerResponse = self.execute();
                return mapper.apply(innerResponse);
            }
        };
    }

    @FunctionalInterface
    interface Function<T, R> {
        R apply(T t);
    }
}
