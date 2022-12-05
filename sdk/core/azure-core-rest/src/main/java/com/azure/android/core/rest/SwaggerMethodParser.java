// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.rest.implementation.TypeUtil;
import com.azure.android.core.logging.ClientLogger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class SwaggerMethodParser {
    private final ClientLogger logger;

    private static final String MISSING_OR_NON_PARAMETERIZED_CALLBACK
        = "The method %s must have a %s parameter, it must be the last parameter and parameterized as "
        + " Callback<Response<Foo>>, Callback<? ResponseBase<FooHdr, Foo>>,"
        + " Callback<PagedResponse<Foo>> or Callback<? PagedResponseBase<FooHdr, Foo>>.";

    private final Method swaggerMethod;
    private final JacksonSerder jacksonSerder;

    private final String methodFullName;
    private final Type callbackType;
    final int callbackArgIndex;
    final Integer cancellationTokenArgIndex;
    private final HttpRequestMapper httpRequestMapper;
    private volatile HttpResponseMapper httpResponseMapper;

    SwaggerMethodParser(String rawHost,
                        Method swaggerMethod,
                        JacksonSerder jacksonSerder,
                        ClientLogger logger) {
        this.swaggerMethod = swaggerMethod;
        this.jacksonSerder = jacksonSerder;
        this.logger = logger;
        this.methodFullName = swaggerMethod.getDeclaringClass().getName() + "." + swaggerMethod.getName();

        final Type[] methodParamTypes = swaggerMethod.getGenericParameterTypes();
        this.callbackType = extractCallbackType(methodParamTypes);
        this.callbackArgIndex = methodParamTypes.length - 1;
        this.cancellationTokenArgIndex = extractCancellationTokenIndex(methodParamTypes);

        this.httpRequestMapper = new HttpRequestMapper(rawHost, swaggerMethod, jacksonSerder);
    }

    String getMethodFullName() {
        return this.methodFullName;
    }

    HttpRequest mapToHttpRequest(Object[] methodArguments) throws IOException {
        return this.httpRequestMapper.map(methodArguments);
    }

    synchronized Response<?> mapToRestResponse(HttpResponse httpResponse) throws Throwable {
        if (this.httpResponseMapper == null) {
            this.httpResponseMapper = new HttpResponseMapper(this.swaggerMethod, this.callbackType, this.logger);
        }
        return this.httpResponseMapper.map(httpResponse, this.jacksonSerder);
    }

    private Type extractCallbackType(Type[] methodParamTypes) {
        final Type methodLastParamType = methodParamTypes.length > 0
            ? methodParamTypes[methodParamTypes.length - 1]
            : null;

        if (methodLastParamType == null || !TypeUtil.isTypeOrSubTypeOf(methodLastParamType, Callback.class)) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(
                MISSING_OR_NON_PARAMETERIZED_CALLBACK,
                this.methodFullName,
                Callback.class.getName())));
        }

        final Type callbackType = methodLastParamType;
        if (!(callbackType instanceof ParameterizedType)) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(
                MISSING_OR_NON_PARAMETERIZED_CALLBACK,
                this.methodFullName,
                Callback.class.getName())));
        }
        return callbackType;
    }

    private Integer extractCancellationTokenIndex(Type[] methodParamTypes) {
        if (methodParamTypes.length < 2) {
            return -1;
        } else {
            final Type methodSecondLastParamType = methodParamTypes[methodParamTypes.length - 2];
            if (TypeUtil.isTypeOrSubTypeOf(methodSecondLastParamType, CancellationToken.class)) {
                return methodParamTypes.length - 2;
            } else {
                return -1;
            }
        }
    }
}