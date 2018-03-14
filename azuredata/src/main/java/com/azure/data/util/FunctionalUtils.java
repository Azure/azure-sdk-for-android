package com.azure.data.util;

import android.annotation.TargetApi;

import java.util.function.Consumer;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@TargetApi(24)
public class FunctionalUtils {
    public static <T> Function1<T, Unit> onCallback(Consumer<T> callable) {
        return t -> {
            callable.accept(t);
            return Unit.INSTANCE;
        };
    }
}