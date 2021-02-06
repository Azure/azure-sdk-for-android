// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate sub-types of a polymorphic type.
 */
@Target({ElementType.ANNOTATION_TYPE,
    ElementType.TYPE,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeSubTypes {
    /**
     * Subtypes of the annotated type.
     *
     * @return The subtypes.
     */
    SerdeSubTypes.Type[] value();

    /**
     * Definition of a subtype, along with optional name.
     */
    @interface Type {
        /**
         * Gets the Class of the subtype.
         *
         * @return The subtype class name.
         */
        Class<?> value();

        /**
         * Gets the logical type name used as the type identifier for the class.
         *
         * @return The name of the type identifier for the class.
         */
        String name() default "";
    }
}
