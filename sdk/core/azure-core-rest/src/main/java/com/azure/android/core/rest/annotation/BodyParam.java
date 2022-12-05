// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate a parameter to send to a REST endpoint as HTTP Request content.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface BodyParam {
    /**
     * @return the Content-Type that the body should be treated as
     */
    String value();
}