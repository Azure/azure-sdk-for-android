// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate list of HTTP status codes that are expected in response from a REST endpoint.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ExpectedResponses {
    /**
     * The status code that will trigger that an error of type errorType should be returned.
     * @return The status code that will trigger than an error of type errorType should be returned.
     */
    int[] value();
}