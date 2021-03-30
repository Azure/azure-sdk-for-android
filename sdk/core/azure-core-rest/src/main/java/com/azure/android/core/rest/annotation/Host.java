// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for parameterized host name targeting a REST service.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Host {
    /**
     * Get the protocol/scheme, host, and optional port number in a single string.
     * @return The protocol/scheme, host, and optional port number in a single string.
     */
    String value() default "";
}
