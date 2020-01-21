// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.util;

import com.azure.android.core.exception.HttpResponseException;
import com.azure.android.core.implementation.util.serializer.SerializerAdapter;
import com.azure.android.core.implementation.util.serializer.SerializerEncoding;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Internal utility type to deal with exceptions.
 */
public class ExceptionUtils {
    private ExceptionUtils() {
    }

    /**
     * Create an exception from the HTTP response.
     *
     * @param exceptionMapping  The HTTP status code to perform exception mapping on.
     * @param response          The failed response from the service.
     * @param serializerAdapter The adapter to decode the error body.
     * @return The exception.
     */
    public static RuntimeException createException(Map<Integer, Class<? extends HttpResponseException>> exceptionMapping,
                                                   Response response,
                                                   SerializerAdapter serializerAdapter) {
        Class<? extends HttpResponseException> exceptionType = null;
        Class<? extends HttpResponseException> defaultExceptionType = null;

        for (Map.Entry<Integer, Class<? extends HttpResponseException>> mapping : exceptionMapping.entrySet()) {
            if (mapping.getKey() == response.code()) {
                exceptionType = mapping.getValue();
            } else if (mapping.getKey() == -1) {
                defaultExceptionType = mapping.getValue();
            }
        }

        exceptionType = exceptionType == null
            ? (defaultExceptionType == null ? HttpResponseException.class : defaultExceptionType)
            : exceptionType;

        Class<?> exceptionValueType = Object.class;
        String errorContent = "";
        Object errorContentDecoded = null;
        final ResponseBody errorBody = response.body();

        if (errorBody != null && errorBody.source() != null) {
            errorContent = errorBody.source().getBuffer().readUtf8();

            if (errorContent.length() >= 0) {
                try {
                    final Method exceptionValueMethod = exceptionType.getDeclaredMethod("value");
                    exceptionValueType = exceptionValueMethod.getReturnType();
                } catch (NoSuchMethodException e) {
                    exceptionValueType = Object.class;
                }

                try {
                    errorContentDecoded = serializerAdapter.deserialize(errorContent, exceptionValueType,
                        SerializerEncoding.fromHeaders(response.headers()));
                } catch (IOException ignored) {
                    // Ignored
                }
            }
        }

        String errorBodyRepresentation = errorContent.isEmpty() ? "(empty body)" : "\"" + errorContent + "\"";
        RuntimeException exception;

        try {
            final Constructor<? extends HttpResponseException> exceptionConstructor =
                exceptionType.getConstructor(String.class, Response.class, exceptionValueType);
            exception =
                exceptionConstructor.newInstance("Status code " + response.code() + ", " + errorBodyRepresentation,
                    response, errorContentDecoded);
        } catch (ReflectiveOperationException e) {
            String message =
                "Status code " + response.code() + ", but an instance of " + exceptionType.getCanonicalName() + " " +
                    "cannot be created." + " Response body: " + errorBodyRepresentation;
            exception = new RuntimeException(new IOException(message, e));
        }

        return exception;
    }
}
