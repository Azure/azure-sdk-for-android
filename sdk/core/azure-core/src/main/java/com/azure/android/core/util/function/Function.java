// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.function;

public interface Function<T, R> {
    R apply(T t);
}