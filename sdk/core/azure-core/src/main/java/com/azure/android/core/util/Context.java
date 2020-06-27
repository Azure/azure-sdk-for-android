// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import java.util.Objects;

public final class Context {
    public static final Context NONE = new Context(null, null, null);

    private final Context parent;
    private final Object key;
    private final Object value;
    private boolean isCancelled = false;

    public Context(Object key, Object value) {
        this(null,
            Objects.requireNonNull(key, "'key' is required and cannot be null."),
            value);
    }

    private Context(Context parent, Object key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    public Context addData(Object key, Object value) {
        return new Context(this,
            Objects.requireNonNull(key, "'key' is required and cannot be null."),
            value);
    }

    public Maybe<Object> getData(Object key) {
        Objects.requireNonNull(key, "'key' is required and cannot be null.");
        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return new Maybe<>(c.value);
            }
        }
        return Maybe.none();
    }
}
