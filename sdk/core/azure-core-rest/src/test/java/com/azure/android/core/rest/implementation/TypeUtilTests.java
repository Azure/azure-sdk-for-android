// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

public class TypeUtilTests {

    @Test
    public void testGetClasses() {
        Puppy puppy = new Puppy();
        List<Class<?>> classes = com.azure.core.rest.implementation.TypeUtil.getAllClasses(puppy.getClass());
        Assertions.assertEquals(4, classes.size());
        Assertions.assertTrue(classes.contains(Puppy.class));
        Assertions.assertTrue(classes.contains(Dog.class));
        Assertions.assertTrue(classes.contains(Pet.class));
        Assertions.assertTrue(classes.contains(Object.class));
    }

    @Test
    public void testGetTypeArguments() {
        Type[] puppyArgs = com.azure.core.rest.implementation.TypeUtil.getTypeArguments(Puppy.class);
        Type[] dogArgs = com.azure.core.rest.implementation.TypeUtil.getTypeArguments(Puppy.class.getGenericSuperclass());
        Type[] petArgs = com.azure.core.rest.implementation.TypeUtil.getTypeArguments(Dog.class.getGenericSuperclass());

        Assertions.assertEquals(0, puppyArgs.length);
        Assertions.assertEquals(1, dogArgs.length);
        Assertions.assertEquals(2, petArgs.length);
    }

    @Test
    public void testGetTypeArgument() {
        Type dogArgs = com.azure.core.rest.implementation.TypeUtil.getTypeArgument(Puppy.class.getGenericSuperclass());
        Assertions.assertEquals(Kid.class, dogArgs);
    }

    @Test
    public void testGetRawClass() {
        Type petType = Puppy.class.getSuperclass().getGenericSuperclass();
        Assertions.assertEquals(Pet.class, com.azure.core.rest.implementation.TypeUtil.getRawClass(petType));
    }

    @Test
    public void testGetSuperType() {
        Type dogType = com.azure.core.rest.implementation.TypeUtil.getSuperType(Puppy.class);
        Type petType = com.azure.core.rest.implementation.TypeUtil.getSuperType(dogType);

        Type[] arguments = com.azure.core.rest.implementation.TypeUtil.getTypeArguments(petType);
        Assertions.assertEquals(2, arguments.length);
        Assertions.assertEquals(Kid.class, arguments[0]);
        Assertions.assertEquals(String.class, arguments[1]);
    }

    @Test
    public void testGetTopSuperType() {
        Type petType = com.azure.core.rest.implementation.TypeUtil.getSuperType(Puppy.class, Pet.class);

        Type[] arguments = com.azure.core.rest.implementation.TypeUtil.getTypeArguments(petType);
        Assertions.assertEquals(2, arguments.length);
        Assertions.assertEquals(Kid.class, arguments[0]);
        Assertions.assertEquals(String.class, arguments[1]);
    }

    @Test
    public void testIsTypeOrSubTypeOf() {
        Type dogType = com.azure.core.rest.implementation.TypeUtil.getSuperType(Puppy.class);
        Type petType = com.azure.core.rest.implementation.TypeUtil.getSuperType(dogType);

        Assertions.assertTrue(com.azure.core.rest.implementation.TypeUtil.isTypeOrSubTypeOf(Puppy.class, dogType));
        Assertions.assertTrue(com.azure.core.rest.implementation.TypeUtil.isTypeOrSubTypeOf(Puppy.class, Puppy.class));
        Assertions.assertTrue(com.azure.core.rest.implementation.TypeUtil.isTypeOrSubTypeOf(Puppy.class, petType));
        Assertions.assertTrue(com.azure.core.rest.implementation.TypeUtil.isTypeOrSubTypeOf(dogType, petType));
        Assertions.assertTrue(com.azure.core.rest.implementation.TypeUtil.isTypeOrSubTypeOf(dogType, dogType));
        Assertions.assertTrue(com.azure.core.rest.implementation.TypeUtil.isTypeOrSubTypeOf(petType, petType));
    }

    @Test
    public void testCreateParameterizedType() {
        Type dogType = com.azure.core.rest.implementation.TypeUtil.getSuperType(Puppy.class);
        Type petType = com.azure.core.rest.implementation.TypeUtil.getSuperType(dogType);

        Type createdType = com.azure.core.rest.implementation.TypeUtil.createParameterizedType(Pet.class, Kid.class, String.class);
        Assertions.assertEquals(com.azure.core.rest.implementation.TypeUtil.getRawClass(petType), com.azure.core.rest.implementation.TypeUtil.getRawClass(createdType));
        Assertions.assertArrayEquals(com.azure.core.rest.implementation.TypeUtil.getTypeArguments(petType), TypeUtil.getTypeArguments(createdType));
    }

    private abstract static class Pet<T extends Human, V> {
        abstract T owner();
    }

    private static class Human {
    }

    private static class Kid extends Human {
    }

    private static class Dog<T extends Human> extends Pet<T, String> {
        private T owner;

        @Override
        public T owner() {
            return owner;
        }
    }

    private static class Puppy extends Dog<Kid> {
    }
}
