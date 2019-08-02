package com.azure.core.implementation.util;

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
}
