// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for form parameters to be sent to a REST API Request URI.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FormParam {
    /**
     * @return The name of the key in a key value pair as part of the form data
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link FormParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this query parameter is already encoded.
     */
    boolean encoded() default false;
}
