// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import java.util.Objects;

public final class Maybe<T> {
    private final T value;
    private static final Maybe<?> NONE = new Maybe<>();

    private Maybe() {
        this.value = null;
    }

    public Maybe(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @SuppressWarnings("unchecked")
    public static<T> Maybe<T> none() {
        return (Maybe<T>) NONE;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public boolean hasValue() {
        return value != null;
    }

    public T value() {
        if (value == null) {
            throw new UnsupportedOperationException("No value present");
        }
        return value;
    }
}
