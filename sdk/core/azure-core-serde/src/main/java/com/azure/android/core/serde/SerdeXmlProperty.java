// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to provide XML-specific configuration
 * for properties, above and beyond what {@link SerdeProperty} contains.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeXmlProperty {
    /**
     * Indicates whether this property is a xml attribute.
     *
     * @return true if property is a xml attribute.
     */
    boolean isAttribute() default false;

    /**
     * Gets the xml namespace name.
     *
     * @return the xml namespace name.
     */
    String namespace() default "";

    /**
     * Gets the xml local name.
     *
     * @return the xml local name.
     */
    String localName() default "";
}
