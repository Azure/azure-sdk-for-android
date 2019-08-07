// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import android.text.TextUtils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.serializer.MalformedValueException;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.util.function.Function;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

/**
 *  The util class is a helper class for clone operation.
 */
public final class ImplUtils {
    private static final String COMMA = ",";

    private ImplUtils() {
        // Exists only to defeat instantiation.
    }

    /*
     * Creates a copy of the source byte array.
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /*
     * Creates a copy of the source int array.
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static int[] clone(int[] source) {
        if (source == null) {
            return null;
        }
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /*
     * Creates a copy of the source array.
     * @param source Array being copied.
     * @param <T> Generic representing the type of the source array.
     * @return A copy of the array or null if source was null.
     */
    public static <T> T[] clone(T[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
    }

    /*
     * Checks if the array is null or empty.
     * @param array Array being checked for nullness or emptiness.
     * @return True if the array is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /*
     * Checks if the collection is null or empty.
     * @param collection Collection being checked for nullness or emptiness.
     * @return True if the collection is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /*
     * Checks if the map is null or empty.
     * @param map Map being checked for nullness or emptiness.
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /*
     * Checks if the character sequence is null or empty.
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    //    TODO: anuchan: Function available starting from API Level 24., below Function is from com.azure.core.implementation.util.function package
//    /*
//     * Turns an array into a string mapping each element to a string and delimits them using a coma.
//     * @param array Array being formatted to a string.
//     * @param mapper Function that maps each element to a string.
//     * @param <T> Generic representing the type of the array.
//     * @return Array with each element mapped and delimited, otherwise null if the array is empty or null.
//     */
    public static <T> String arrayToString(T[] array, Function<T, String> mapper) {
        if (isNullOrEmpty(array)) {
            return null;
        }
        String [] mappedArray = new String[array.length];
        int i = 0;
        for (T item : array) {
            mappedArray[i] = mapper.apply(item);
            i++;
        }
        return TextUtils.join(COMMA, mappedArray);
    }

    /*
     * Returns the first instance of the given class from an array of Objects.
     * @param args Array of objects to search through to find the first instance of the given `clazz` type.
     * @param clazz The type trying to be found.
     * @param <T> Generic type
     * @return The first object of the desired type, otherwise null.
     */
    public static <T> T findFirstOfType(Object[] args, Class<T> clazz) {
        if (isNullOrEmpty(args)) {
            return null;
        }

        for (Object arg : args) {
            if (clazz.isInstance(arg)) {
                return clazz.cast(arg);
            }
        }

        return null;
    }

    public static RuntimeException createException(Map<Integer, Class<? extends HttpResponseException>> exceptionMapping, ResponseBody errorBody, HttpResponse httpResponse, SerializerAdapter serializerAdapter) {
        Class<? extends HttpResponseException> exceptionType = HttpResponseException.class;
        for (Map.Entry<Integer, Class<? extends HttpResponseException>> mapping : exceptionMapping.entrySet()) {
            if (mapping.getKey() == httpResponse.statusCode()) {
                exceptionType = mapping.getValue();
            }
        }
        //
        Class<?> exceptionValueType = Object.class;
        String errorContent = "";
        Object responseErrorContentDecoded = null;
        if (errorBody != null && errorBody.source() != null) {
            errorContent = errorBody.source().getBuffer().readUtf8(); // TODO: anuchan if errorBody has to be reused then clone it.
            if (errorContent.length() >= 0) {
                try {
                    final Method exceptionValueMethod = exceptionType.getDeclaredMethod("value");
                    exceptionValueType = exceptionValueMethod.getReturnType();
                } catch (NoSuchMethodException e) {
                    exceptionValueType = Object.class;
                }
                try {
                    responseErrorContentDecoded = serializerAdapter.deserialize(new StringReader(errorContent), exceptionValueType, SerializerEncoding.fromHeaders(httpResponse.headers()));
                } catch (IOException | MalformedValueException ignored) {
                    // ignored
                }
            }
        }
        String errorBodyRepresentation = errorContent.isEmpty() ? "(empty body)" : "\"" + errorContent + "\"";
        //
        RuntimeException exception;
        try {
            final Constructor<? extends HttpResponseException> exceptionConstructor = exceptionType.getConstructor(String.class, HttpResponse.class, exceptionValueType);
            exception = exceptionConstructor.newInstance("Status code " + httpResponse.statusCode() + ", " + errorBodyRepresentation,
                httpResponse,
                responseErrorContentDecoded);
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + httpResponse.statusCode() + ", but an instance of "
                + exceptionType.getCanonicalName() + " cannot be created."
                + " Response body: " + errorBodyRepresentation;
            exception = new RuntimeException(new IOException(message, e));
        }
        return exception;
    }
}
