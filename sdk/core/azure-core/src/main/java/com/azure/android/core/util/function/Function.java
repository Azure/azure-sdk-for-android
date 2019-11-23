// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.function;

/**
 * Simple interface make code compatible with Java 7
 */
public interface Function<Input, Output> {
    Output apply(Input t);
}
