// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate list of static headers sent to a REST endpoint.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Headers {
    /**
     * List of static headers.
     * @return List of static headers.
     */
    String[] value();
}