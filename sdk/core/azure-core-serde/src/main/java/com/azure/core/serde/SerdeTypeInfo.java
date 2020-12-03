// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for configuring serialization and deserialization polymorphic type.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE,
    ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeTypeInfo {
    /**
     * Definition of different type identifiers that can be included in JSON
     * during serialization, and used for deserialization.
     */
    enum Id {
        /**
         * This means that no explicit type metadata is included, and typing is
         * purely done using contextual information possibly augmented with other
         * annotations.
         */
        NONE(null),

        /**
         * Means that logical type name is used as type information; name will then need
         * to be separately resolved to actual concrete type (Class).
         */
        NAME("@type");

        private final String propertyName;

        Id(String defProp) {
            propertyName = defProp;
        }
    }

    /**
     * Definition of standard type inclusion mechanisms for type metadata.
     * Used for standard metadata types, except for {@link Id#NONE}.
     */
    enum As {
        /**
         * Inclusion mechanism that uses a single configurable property, included
         * along with actual data (POJO properties) as a separate meta-property.
         * <p>
         * Default choice for inclusion.
         */
        PROPERTY,
    }

    /**
     * Specifies kind of type metadata to use when serializing
     * type information for instances of annotated type
     * and its subtypes; as well as what is expected during
     * deserialization.
     *
     * @return metadata type kind.
     */
    Id use();

    /**
     * Specifies mechanism to use for including type metadata (if any; for
     * {@link Id#NONE} nothing is included); used when serializing,
     * and expected when deserializing.
     *
     * @return metadata type inclusion mechanism.
     */
    As include() default As.PROPERTY;

    /**
     * Property names used when type inclusion method ({@link As#PROPERTY}).
     *
     * @return the property names used when type inclusion method.
     */
    String property() default "";

    /**
     * Optional property that can be used to specify default implementation
     * class to use for deserialization.
     *
     * @return the default implementation class for deserialization.
     */
    Class<?> defaultImpl() default SerdeTypeInfo.class;

    /**
     * Property that defines whether type identifier value will be passed
     * as part of JSON stream to deserializer (true), or handled and
     * removed by <code>TypeDeserializer</code> (false).
     * Property has no effect on serialization.
     *<p>
     * Default value is false, meaning that Jackson handles and removes
     * the type identifier from JSON content that is passed to
     * <code>JsonDeserializer</code>.
     *
     * @since 2.0
     *
     * @return the visibility of the type identifier value.
     */
    boolean visible() default false;
}

