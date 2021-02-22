// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.customserde.json.jackson;

import com.azure.core.customserde.JsonSerializer;
import com.azure.core.customserde.MemberNameConverter;
import com.azure.core.customserde.TypeReference;
import com.azure.android.core.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.BeanUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Jackson based implementation of the {@link JsonSerializer} and {@link MemberNameConverter} interfaces.
 */
public final class JacksonJsonSerializer implements JsonSerializer, MemberNameConverter {
    private final ClientLogger logger = new ClientLogger(JacksonJsonSerializer.class);

    private final ObjectMapper mapper;
    private final TypeFactory typeFactory;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        this.typeFactory = mapper.getTypeFactory();
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        if (stream == null) {
            return null;
        }

        try {
            return mapper.readValue(stream, typeFactory.constructType(typeReference.getJavaType()));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            mapper.writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @Override
    public String convertMemberName(Member member) {
        if (Modifier.isTransient(member.getModifiers())) {
            return null;
        }

        VisibilityChecker<?> visibilityChecker = mapper.getVisibilityChecker();
        if (member instanceof Field) {
            Field f = (Field) member;

            if (f.isAnnotationPresent(JsonIgnore.class) || !visibilityChecker.isFieldVisible(f)) {
                if (f.isAnnotationPresent(JsonProperty.class)) {
                    logger.info("Field {} is annotated with JsonProperty but isn't accessible to "
                        + "JacksonJsonSerializer.", f.getName());
                }
                return null;
            }

            if (f.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = f.getAnnotation(JsonProperty.class).value();
                return (propertyName == null || propertyName.length() == 0) ? f.getName() : propertyName;
            }

            return f.getName();
        }

        if (member instanceof Method) {
            Method m = (Method) member;

            /*
             * If the method isn't a getter, is annotated with JsonIgnore, or isn't visible to the ObjectMapper ignore
             * it.
             */
            if (!verifyGetter(m)
                || m.isAnnotationPresent(JsonIgnore.class)
                || !visibilityChecker.isGetterVisible(m)) {
                if (m.isAnnotationPresent(JsonGetter.class) || m.isAnnotationPresent(JsonProperty.class)) {
                    logger.info("Method {} is annotated with either JsonGetter or JsonProperty but isn't accessible "
                        + "to JacksonJsonSerializer.", m.getName());
                }
                return null;
            }

            String methodNameWithoutJavaBeans = removePrefix(m);

            /*
             * Prefer JsonGetter over JsonProperty as it is the more targeted annotation.
             */
            if (m.isAnnotationPresent(JsonGetter.class)) {
                String propertyName = m.getAnnotation(JsonGetter.class).value();
                return (propertyName == null || propertyName.length() == 0) ? methodNameWithoutJavaBeans : propertyName;
            }

            if (m.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = m.getAnnotation(JsonProperty.class).value();
                return (propertyName == null || propertyName.length() == 0) ? methodNameWithoutJavaBeans : propertyName;
            }

            // If no annotation is present default to the inferred name.
            return methodNameWithoutJavaBeans;
        }

        return null;
    }

    /*
     * Only consider methods that don't have parameters and aren't void as valid getter methods.
     */
    private static boolean verifyGetter(Method method) {
        Class<?> returnType = method.getReturnType();

        return method.getParameterTypes().length == 0
            && returnType != void.class
            && returnType != Void.class;
    }

    private static String removePrefix(Method method) {
        return BeanUtil.okNameForGetter(new AnnotatedMethod(null, method, null, null), false);
    }
}
