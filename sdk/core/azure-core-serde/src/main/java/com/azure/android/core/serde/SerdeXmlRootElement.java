// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define name of root element used
 * for the root-level object when serialized, which normally uses
 * name of the type (class).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeXmlRootElement {
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
