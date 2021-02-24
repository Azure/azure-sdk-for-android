// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;


import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.Context;
import com.azure.android.core.logging.ClientLogger;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 *
 * RestProxy can create proxy implementations for interfaces with methods that produces deserialized Java objects.
 */
public final class RestProxy implements InvocationHandler {
    private final ClientLogger logger = new ClientLogger(RestProxy.class);

    private final HttpPipeline httpPipeline;
    private final SwaggerInterfaceParser interfaceParser;

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for.
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests.
     * @param jacksonSerder the serializer that will be used to convert POJOs to and from request and response bodies
     * @param <A> the type of the Swagger interface.
     * @return a proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface,
                               HttpPipeline httpPipeline,
                               JacksonSerder jacksonSerder) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, jacksonSerder);
        final RestProxy restProxy = new RestProxy(httpPipeline, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(),
            new Class<?>[]{swaggerInterface},
            restProxy);
    }

    @Override
    public Object invoke(final Object restProxy,
                         final Method swaggerMethod,
                         final Object[] swaggerMethodArgs) {

        final SwaggerMethodParser methodParser = this.interfaceParser.getMethodParser(swaggerMethod, this.logger);

        final Callback<Response<?>> restCallback;
        restCallback = (Callback<Response<?>>) swaggerMethodArgs[methodParser.callbackArgIndex];
        Objects.requireNonNull(restCallback);

        final CancellationToken cancellationToken;
        if (methodParser.cancellationTokenArgIndex == -1) {
            cancellationToken = CancellationToken.NONE;
        } else {
            cancellationToken = (CancellationToken) swaggerMethodArgs[methodParser.cancellationTokenArgIndex];
        }

        final HttpRequest httpRequest;
        try {
            httpRequest = methodParser.mapToHttpRequest(swaggerMethodArgs);
        } catch (IOException e) {
            restCallback.onFailure(e);
            return null;
        } catch (HttpResponseException e) {
            restCallback.onFailure(e);
            return null;
        }

        this.httpPipeline.send(httpRequest, Context.NONE,
            cancellationToken,
            new HttpPipelineCallback(methodParser, restCallback));
        return null;
    }

    private RestProxy(HttpPipeline httpPipeline,
                      SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.interfaceParser = interfaceParser;
    }

    private static class HttpPipelineCallback implements HttpCallback {
        private final SwaggerMethodParser methodParser;
        private final Callback<Response<?>> restCallback;

        HttpPipelineCallback(SwaggerMethodParser methodParser, Callback<Response<?>> restCallback) {
            this.methodParser = methodParser;
            this.restCallback = restCallback;
        }

        @Override
        public void onSuccess(HttpResponse httpResponse) {
            final Response<?> restResponse;
            try {
                restResponse = this.methodParser.mapToRestResponse(httpResponse);
            } catch (Throwable e) {
                this.restCallback.onFailure(e);
                return;
            }

            this.restCallback.onSuccess(restResponse);
        }

        @Override
        public void onError(Throwable error) {
            this.restCallback.onFailure(error);
        }
    }
}
