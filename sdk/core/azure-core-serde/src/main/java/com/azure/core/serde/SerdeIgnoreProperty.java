// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates that the property should be ignored during serialization and deserialization.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeIgnoreProperty {
    /**
     * Indicate whether property should be ignored or not.
     *
     * @return true if property should be ignored, false otherwise.
     */
    boolean value() default true;
}
