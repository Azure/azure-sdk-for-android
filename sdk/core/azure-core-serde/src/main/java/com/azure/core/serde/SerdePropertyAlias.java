// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define one or more alternative names for
 * a property,
 */
@Target({ElementType.ANNOTATION_TYPE,
    ElementType.FIELD, ElementType.METHOD,
    ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdePropertyAlias {
    /**
     * One or more secondary names to accept as aliases to the official name.
     *
     * @return Zero or more aliases to associate with property annotated
     */
    String[] value() default {
    };
}
