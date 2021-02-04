// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.implementation;

import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.logging.ClientLogger;
import com.azure.core.serde.SerdeAdapter;
import com.azure.core.serde.SerdeEncoding;
import com.azure.core.serde.SerdeParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * Contains the information needed to generate a exception type to be thrown or returned when a REST API returns
 * an error status code.
 */
public class HttpResponseExceptionInfo {
    private static final String EXCEPTION_BODY_METHOD = "getValue";
    public final Class<? extends HttpResponseException> exceptionType;
    public final Class<?> exceptionBodyType;

    /**
     * Creates an UnexpectedExceptionInformation object with the given exception type and expected response body.
     *
     * @param exceptionType Exception type to be thrown.
     */
    public HttpResponseExceptionInfo(Class<? extends HttpResponseException> exceptionType) {
        this.exceptionType = exceptionType;

        // Should always have a value() method. Register Object as a fallback plan.
        Class<?> exceptionBodyType = Object.class;
        try {
            final Method exceptionBodyMethod = exceptionType.getDeclaredMethod(EXCEPTION_BODY_METHOD);
            exceptionBodyType = exceptionBodyMethod.getReturnType();
        } catch (NoSuchMethodException e) {
            // no-op
        }
        this.exceptionBodyType = exceptionBodyType;
    }

    public Throwable instantiateException(final SerdeAdapter serdeAdapter,
                                          final HttpResponse httpResponse,
                                          final ClientLogger logger) {
        final byte[] responseContent = httpResponse.getBodyAsByteArray();
        final InputStream contentStream = (responseContent == null || responseContent.length == 0)
            ? null
            : new ByteArrayInputStream(responseContent);

        Object responseDecodedContent = null;
        try {
            responseDecodedContent = serdeAdapter.deserialize(contentStream,
                this.exceptionBodyType,
                SerdeEncoding.fromHeaders(httpResponse.getHeaders().toMap()));
        }  catch (IOException | SerdeParseException ex) {
            // Though we're unable to represent the wire-error as a POJO, we will communicate
            // the wire-error as exception error-message, hence logged as warning without throw.
            //
            logger.warning("Failed to deserialize the error entity.", ex);
        }

        final int responseStatusCode = httpResponse.getStatusCode();
        final String contentType = httpResponse.getHeaderValue("Content-Type");
        final String bodyRepresentation;
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            bodyRepresentation = "(" + httpResponse.getHeaderValue("Content-Length") + "-byte body)";
        } else {
            bodyRepresentation = responseContent == null || responseContent.length == 0
                ? "(empty body)"
                : "\"" + new String(responseContent, StandardCharsets.UTF_8) + "\"";
        }

        Throwable result;
        try {
            final Constructor<? extends HttpResponseException> exceptionConstructor =
                this.exceptionType.getConstructor(String.class, HttpResponse.class,
                    this.exceptionBodyType);
            result = exceptionConstructor.newInstance("Status code "
                    + responseStatusCode + ", " + bodyRepresentation,
                httpResponse,
                responseDecodedContent);
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of "
                + this.exceptionType.getCanonicalName() + " cannot be created."
                + " Response body: " + bodyRepresentation;

            result = new IOException(message, e);
        }
        return result;
    }
}
