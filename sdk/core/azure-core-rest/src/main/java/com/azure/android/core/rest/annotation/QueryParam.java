// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for query parameters to be appended to a REST API Request URI.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface QueryParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     *     value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link QueryParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this query parameter is already encoded.
     */
    boolean encoded() default false;
}