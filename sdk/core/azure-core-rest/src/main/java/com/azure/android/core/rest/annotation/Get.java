// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * HTTP GET method annotation describing the parameterized relative path to a REST endpoint for resource retrieval.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Get {
    /**
     * Get the relative path of the annotated method's GET URL.
     * @return The relative path of the annotated method's GET URL.
     */
    String value();
}
