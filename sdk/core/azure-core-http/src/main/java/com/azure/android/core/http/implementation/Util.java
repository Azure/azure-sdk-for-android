// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.implementation;

public final class Util {
    private Util() {
    }

    public static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        } else {
            return value;
        }
    }
}
