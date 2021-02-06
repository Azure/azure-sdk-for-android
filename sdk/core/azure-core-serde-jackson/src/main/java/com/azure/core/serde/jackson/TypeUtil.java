// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility type exposing methods to deal with {@link Type}.
 */
public final class TypeUtil {
    /**
     * Find all super classes including provided class.
     *
     * @param clazz the raw class to find super types for
     * @return the list of super classes
     */
    public static List<Class<?>> getAllClasses(Class<?> clazz) {
        List<Class<?>> types = new ArrayList<>();
        while (clazz != null) {
            types.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return types;
    }

    /**
     * Get the generic arguments for a type.
     *
     * @param type the type to get arguments
     * @return the generic arguments, empty if type is not parameterized
     */
    public static Type[] getTypeArguments(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return new Type[0];
        }
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    /**
     * Get the raw class for a given type.
     *
     * @param type the input type
     * @return the raw class
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getRawClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    /**
     * Determines if a type is the same or a subtype for another type.
     *
     * @param subType the supposed sub type
     * @param superType the supposed super type
     * @return true if the first type is the same or a subtype for the second type
     */
    public static boolean isTypeOrSubTypeOf(Type subType, Type superType) {
        return getRawClass(superType).isAssignableFrom(getRawClass(subType));
    }

    // Private Ctr
    private TypeUtil() {
    }
}
