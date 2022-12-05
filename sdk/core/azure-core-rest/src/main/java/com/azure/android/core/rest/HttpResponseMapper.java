// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import android.util.Pair;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionTypes;
import com.azure.android.core.rest.implementation.HttpResponseExceptionInfo;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.util.paging.PagedResponse;
import com.azure.android.core.rest.util.paging.PagedResponseBase;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.serde.jackson.SerdeEncoding;
import com.azure.android.core.serde.jackson.SerdeParseException;
import com.azure.android.core.util.Base64Url;
import com.azure.android.core.util.DateTimeRfc1123;
import com.azure.android.core.util.UnixTime;
import com.azure.android.core.rest.annotation.ExpectedResponses;
import com.azure.android.core.rest.annotation.ReturnValueWireType;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionType;
import com.azure.android.core.rest.implementation.ItemPage;
import com.azure.android.core.rest.implementation.TypeUtil;
import com.azure.android.core.util.paging.Page;

import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class HttpResponseMapper {
    private final ClientLogger logger;

    private static final String NON_PARAMETERIZED_RESPONSE
        = "The %s type argument of the %s parameter in the method %s must be parameterized as %s.";

    final Type headerDecodeType;
    final Type contentDecodeType;
    final Type contentEncodedType;
    final Type expandedContentEncodedType;
    private final BitSet expectedStatusCodes;
    private final HttpResponseExceptionInfo defaultExceptionInfo;
    private final Map<Integer, HttpResponseExceptionInfo> statusCodeToKnownExceptionInfo;
    private final Constructor<? extends Response<?>> responseCtr;
    private final int responseCtrParamCount;

    HttpResponseMapper(Method swaggerMethod, Type callbackType, ClientLogger logger) {
        this.logger = logger;
        final String methodFullName = swaggerMethod.getDeclaringClass().getName() + "." + swaggerMethod.getName();

        final Pair<Type, Type> decodeTypes = extractContentAndHeaderDecodeType(callbackType, methodFullName);
        this.headerDecodeType = decodeTypes.first;
        this.contentDecodeType = decodeTypes.second;

        this.contentEncodedType = extractContentEncodedType(swaggerMethod);
        if (this.contentEncodedType != null && !TypeUtil.isTypeOrSubTypeOf(this.contentEncodedType, Page.class)) {
            this.expandedContentEncodedType = expandContentEncodedType(this.contentEncodedType, this.contentDecodeType);
        } else {
            this.expandedContentEncodedType = null;
        }

        this.expectedStatusCodes = extractExpectedStatusCodes(swaggerMethod);

        Pair<HttpResponseExceptionInfo, Map<Integer, HttpResponseExceptionInfo>> defaultAndKnownExceptions
            = extractDefaultAndKnownExceptions(swaggerMethod);
        this.defaultExceptionInfo = defaultAndKnownExceptions.first;
        this.statusCodeToKnownExceptionInfo = defaultAndKnownExceptions.second;

        this.responseCtr = identifyResponseCtr(callbackType);
        this.responseCtrParamCount = this.responseCtr.getParameterTypes().length;
    }

    Response<?> map(HttpResponse httpResponse, JacksonSerder jacksonSerder) throws Throwable {
        if (!isExpectedStatusCode(httpResponse.getStatusCode())) {
            final HttpResponseExceptionInfo exceptionInfo = getExceptionInfo(httpResponse.getStatusCode());
            throw logger.logThrowableAsError((exceptionInfo.instantiateException(jacksonSerder,
                httpResponse,
                logger)));
        } else {
            Object headerObject = null;
            if (this.headerDecodeType != null) {
                try {
                    headerObject = jacksonSerder.deserialize(httpResponse.getHeaders().toMap(), headerDecodeType);
                } catch (IOException ioe) {
                    throw logger.logExceptionAsError(
                        new HttpResponseException("HTTP response has malformed headers", httpResponse, ioe));
                }
            }

            if (isBooleanResponseForHead(httpResponse)) {
                final boolean isSuccess = (httpResponse.getStatusCode() / 100) == 2;
                httpResponse.close();
                return instantiateResponse(this.responseCtr,
                    this.responseCtrParamCount,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    isSuccess);
            } else if (TypeUtil.isTypeOrSubTypeOf(this.contentDecodeType, Void.class)) {
                httpResponse.close();
                return instantiateResponse(this.responseCtr,
                    this.responseCtrParamCount,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    null);
            } else if (TypeUtil.isTypeOrSubTypeOf(this.contentDecodeType, InputStream.class)) {
                return instantiateResponse(this.responseCtr,
                    this.responseCtrParamCount,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    httpResponse.getBody());
            } else if (TypeUtil.isTypeOrSubTypeOf(this.contentDecodeType, byte[].class)) {
                if (this.contentEncodedType == Base64Url.class) {
                    final byte[] encodedContent = httpResponse.getBodyAsByteArray();
                    final byte[] decodedContent = new Base64Url(encodedContent).decodedBytes();
                    return instantiateResponse(this.responseCtr,
                        this.responseCtrParamCount,
                        httpResponse.getRequest(),
                        httpResponse,
                        headerObject,
                        decodedContent);
                } else {
                    return instantiateResponse(this.responseCtr,
                        this.responseCtrParamCount,
                        httpResponse.getRequest(),
                        httpResponse,
                        headerObject,
                        httpResponse.getBodyAsByteArray());
                }
            } else if (this.contentEncodedType == null) {
                final Object decodedContent = deserializeHttpBody(jacksonSerder,
                    httpResponse,
                    this.contentDecodeType);
                return instantiateResponse(this.responseCtr,
                    this.responseCtrParamCount,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    decodedContent);
            } else {
                Objects.requireNonNull(this.contentEncodedType);
                if (TypeUtil.isTypeOrSubTypeOf(this.contentEncodedType, Page.class)) {
                    final Type pageType = (this.contentEncodedType == Page.class)
                        ? TypeUtil.createParameterizedType(ItemPage.class, this.contentDecodeType)
                        : this.contentEncodedType;

                    final Object decodedContent = deserializeHttpBody(jacksonSerder, httpResponse, pageType);
                    return instantiateResponse(this.responseCtr,
                        this.responseCtrParamCount,
                        httpResponse.getRequest(),
                        httpResponse,
                        headerObject,
                        decodedContent);
                } else {
                    Objects.requireNonNull(this.expandedContentEncodedType);
                    final Object encodedContent = deserializeHttpBody(jacksonSerder,
                        httpResponse,
                        this.expandedContentEncodedType);
                    final Object decodedContent = decodeContent(encodedContent,
                        this.contentEncodedType,
                        this.contentDecodeType);

                    return instantiateResponse(this.responseCtr,
                        this.responseCtrParamCount,
                        httpResponse.getRequest(),
                        httpResponse,
                        headerObject,
                        decodedContent);
                }
            }
        }
    }

    boolean isExpectedStatusCode(final int statusCode) {
        return this.expectedStatusCodes == null ? statusCode < 400 : this.expectedStatusCodes.get(statusCode);
    }

    HttpResponseExceptionInfo getExceptionInfo(int code) {
        if (this.statusCodeToKnownExceptionInfo != null
            && this.statusCodeToKnownExceptionInfo.containsKey(code)) {
            return this.statusCodeToKnownExceptionInfo.get(code);
        } else {
            return this.defaultExceptionInfo;
        }
    }

    private boolean isBooleanResponseForHead(HttpResponse httpResponse) {
        return httpResponse.getRequest().getHttpMethod() == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(this.contentDecodeType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(this.contentDecodeType, Boolean.class));
    }

    private Object deserializeHttpBody(JacksonSerder jacksonSerder, HttpResponse httpResponse, Type bodyType) {
        try {
            return jacksonSerder.deserialize(httpResponse.getBody(), bodyType,
                SerdeEncoding.fromHeaders(httpResponse.getHeaders().toMap()));

        } catch (SerdeParseException e) {
            throw logger.logExceptionAsError(new HttpResponseException("HTTP response has a malformed body.",
                httpResponse, e));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new HttpResponseException("Deserialization Failed.",
                httpResponse, e));
        }
    }

    private Type extractContentEncodedType(Method swaggerMethod) {
        final ReturnValueWireType wireTypeAnnotation = swaggerMethod.getAnnotation(ReturnValueWireType.class);
        if (wireTypeAnnotation != null) {
            Class<?> returnValueWireType = wireTypeAnnotation.value();
            if (returnValueWireType == Base64Url.class
                || returnValueWireType == UnixTime.class
                || returnValueWireType == DateTimeRfc1123.class) {
                return returnValueWireType;
            } else if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, List.class)) {
                return returnValueWireType.getGenericInterfaces()[0];
            } else if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, Page.class)) {
                return returnValueWireType;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private BitSet extractExpectedStatusCodes(Method swaggerMethod) {
        final ExpectedResponses expectedResponses = swaggerMethod.getAnnotation(ExpectedResponses.class);
        if (expectedResponses != null && expectedResponses.value().length > 0) {
            BitSet expectedStatusCodes = new BitSet();
            for (int code : expectedResponses.value()) {
                expectedStatusCodes.set(code);
            }
            return expectedStatusCodes;
        } else {
            return null;
        }
    }

    private Pair<HttpResponseExceptionInfo, Map<Integer, HttpResponseExceptionInfo>> extractDefaultAndKnownExceptions(
        Method swaggerMethod) {

        final UnexpectedResponseExceptionTypes unexpectedResponseExceptionTypesHolder
            = swaggerMethod.getAnnotation(UnexpectedResponseExceptionTypes.class);

        if (unexpectedResponseExceptionTypesHolder == null
            || unexpectedResponseExceptionTypesHolder.value() == null
            || unexpectedResponseExceptionTypesHolder.value().length == 0) {
            return Pair.create(new HttpResponseExceptionInfo(HttpResponseException.class), null);
        }

        final UnexpectedResponseExceptionType[] unexpectedResponseExceptionTypes
            = unexpectedResponseExceptionTypesHolder.value();

        Map<Integer, HttpResponseExceptionInfo> statusCodeToKnownExceptionInfo
            = new HashMap<>(unexpectedResponseExceptionTypes.length);
        HttpResponseExceptionInfo defaultExceptionInfo = null;

        for (UnexpectedResponseExceptionType exceptionAnnotation : unexpectedResponseExceptionTypes) {
            if (exceptionAnnotation.code().length == 0) {
                defaultExceptionInfo = new HttpResponseExceptionInfo(exceptionAnnotation.value());
            } else {
                final HttpResponseExceptionInfo knownExceptionInfo
                    = new HttpResponseExceptionInfo(exceptionAnnotation.value());
                for (int statusCode : exceptionAnnotation.code()) {
                    statusCodeToKnownExceptionInfo.put(statusCode, knownExceptionInfo);
                }
            }
        }
        if (defaultExceptionInfo == null) {
            defaultExceptionInfo = new HttpResponseExceptionInfo(HttpResponseException.class);
        }

        return Pair.create(defaultExceptionInfo, statusCodeToKnownExceptionInfo);
    }

    private Pair<Type, Type> extractContentAndHeaderDecodeType(Type callbackType, String swaggerMethodName) {
        final Type callbackTypeArgument = TypeUtil.getTypeArgument(callbackType);

        // Pair<Type:HeaderDecodeType, Type:ContentDecodeType>
        Pair<Type, Type> decodeTypes;

        decodeTypes = tryExtractContentAndHeaderDecodeType(callbackTypeArgument,
            ResponseBase.class,
            swaggerMethodName);

        if (decodeTypes != null) {
            return decodeTypes;
        }

        decodeTypes = tryExtractContentAndHeaderDecodeType(callbackTypeArgument,
            PagedResponseBase.class,
            swaggerMethodName);

        if (decodeTypes != null) {
            return decodeTypes;
        }

        Type decodeType;

        decodeType = tryExtractContentDecodeType(callbackTypeArgument,
            PagedResponse.class,
            swaggerMethodName);

        if (decodeType != null) {
            return Pair.create(null, decodeType);
        }

        decodeType = tryExtractContentDecodeType(callbackTypeArgument,
            Response.class,
            swaggerMethodName);

        if (decodeType != null) {
            return Pair.create(null, decodeType);
        }

        throw logger.logExceptionAsError(new IllegalStateException("The type argument of "
            + Callback.class.getName()
            + " in the method " + swaggerMethodName
            + " must either ResponseBase<H, C>, PagedResponseBase<H, C>, PagedResponse<C> or Response<C>"));
    }

    private Pair<Type, Type> tryExtractContentAndHeaderDecodeType(Type callbackTypeArg,
                                                                  Type responseBaseType,
                                                                  String swaggerMethodName) {
        if (!responseBaseType.equals(ResponseBase.class) && !responseBaseType.equals(PagedResponseBase.class)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("responseBaseType must be 'ResponseBase' or 'PagedResponseBase'"));
        }

        if (!TypeUtil.isTypeOrSubTypeOf(callbackTypeArg, responseBaseType)) {
            return null;
        }

        Type responseTypeItr = callbackTypeArg;
        List<Type> responseTypeHierarchy = new ArrayList<>();

        responseTypeHierarchy.add(responseTypeItr);

        while (!TypeUtil.getRawClass(responseTypeItr).equals(responseBaseType)) {
            responseTypeItr = TypeUtil.getSuperType(responseTypeItr);
            responseTypeHierarchy.add(responseTypeItr);
        }

        // responseTypeItr == Ref to 'ResponseBase' or 'PagedResponseBase' in the response type hierarchy.
        //
        if (!(responseTypeItr instanceof ParameterizedType)) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(
                NON_PARAMETERIZED_RESPONSE,
                responseBaseType.toString(), Callback.class.getName(),
                swaggerMethodName, responseBaseType.toString() + "<FooHeader, Foo>>")));
        }

        Type headerDecodeType = null;
        Type contentDecodeType = null;
        for (Type responseType : responseTypeHierarchy) {
            final Type[] responseArgTypes = TypeUtil.getTypeArguments(responseType);
            if (responseArgTypes != null && responseArgTypes.length > 0) {
                if (responseArgTypes.length >= 2) {
                    if (contentDecodeType == null) {
                        contentDecodeType = responseArgTypes[responseArgTypes.length - 1];
                    }
                    if (headerDecodeType == null) {
                        headerDecodeType = responseArgTypes[responseArgTypes.length - 2];
                    }
                } else {
                    if (headerDecodeType == null) {
                        headerDecodeType = responseArgTypes[responseArgTypes.length - 1];
                    }
                }

            }
        }

        return Pair.create(headerDecodeType, contentDecodeType);
    }

    private Type tryExtractContentDecodeType(Type callbackTypeArg,
                                             Type responseInterfaceType,
                                             String swaggerMethodName) {
        if (!responseInterfaceType.equals(Response.class) && !responseInterfaceType.equals(PagedResponse.class)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("responseInterfaceType must be 'Response' or 'PagedResponse'"));
        }

        if (!TypeUtil.isTypeOrSubTypeOf(callbackTypeArg, responseInterfaceType)) {
            return null;
        }

        Type responseTypeItr = callbackTypeArg;
        while (!TypeUtil.getRawClass(responseTypeItr).equals(Object.class)) {
            final Type[] responseArgTypes = TypeUtil.getTypeArguments(responseTypeItr);
            if (responseArgTypes != null && responseArgTypes.length > 0) {
                return responseArgTypes[responseArgTypes.length - 1];
            }
            if (TypeUtil.getRawClass(responseTypeItr).isInterface()) {
                break;
            }
            responseTypeItr = TypeUtil.getSuperType(responseTypeItr);
        }

        throw logger.logExceptionAsError(new IllegalStateException(String.format(
            NON_PARAMETERIZED_RESPONSE,
            responseInterfaceType.toString(), Callback.class.getName(),
            swaggerMethodName, responseInterfaceType .toString() + "<Foo>")));
    }

    private Type expandContentEncodedType(Type contentEncodedType, Type contentDecodeType) {
        Objects.requireNonNull(contentEncodedType);

        if (contentDecodeType == byte[].class) {
            if (contentEncodedType == Base64Url.class) {
                return Base64Url.class;
            }
        } else if (contentDecodeType == OffsetDateTime.class) {
            if (contentEncodedType == DateTimeRfc1123.class) {
                return DateTimeRfc1123.class;
            } else if (contentEncodedType == UnixTime.class) {
                return UnixTime.class;
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(contentDecodeType, List.class)) {
            final Type resultElementType = TypeUtil.getTypeArgument(contentDecodeType);
            final Type wireResponseElementType = expandContentEncodedType(contentEncodedType, resultElementType);

            return TypeUtil.createParameterizedType(((ParameterizedType) contentDecodeType).getRawType(),
                wireResponseElementType);
        } else if (TypeUtil.isTypeOrSubTypeOf(contentDecodeType, Map.class)) {
            final Type[] typeArguments = TypeUtil.getTypeArguments(contentDecodeType);
            final Type resultValueType = typeArguments[1];
            final Type wireResponseValueType = expandContentEncodedType(contentEncodedType, resultValueType);

            return TypeUtil.createParameterizedType(((ParameterizedType) contentDecodeType).getRawType(),
                typeArguments[0], wireResponseValueType);
        }

        return contentDecodeType;
    }

    private Object decodeContent(final Object encodedContent,
                                 final Type encodedType,
                                 final Type decodeType) {
        if (decodeType == byte[].class) {
            if (encodedType == Base64Url.class) {
                return ((Base64Url) encodedContent).decodedBytes();
            }
        } else if (decodeType == OffsetDateTime.class) {
            if (encodedType == DateTimeRfc1123.class) {
                return ((DateTimeRfc1123) encodedContent).getDateTime();
            } else if (encodedType == UnixTime.class) {
                return ((UnixTime) encodedContent).getDateTime();
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(decodeType, List.class)) {
            final Type decodeElementType = TypeUtil.getTypeArgument(decodeType);

            @SuppressWarnings("unchecked") final List<Object> list = (List<Object>) encodedContent;

            final int size = list.size();
            for (int i = 0; i < size; ++i) {
                final Object encodedElement = list.get(i);
                final Object decodedElement = decodeContent(encodedElement,
                    encodedType,
                    decodeElementType);
                if (encodedElement != decodedElement) {
                    list.set(i, decodedElement);
                }
            }

            return list;
        } else if (TypeUtil.isTypeOrSubTypeOf(decodeType, Map.class)) {
            final Type decodeValueType = TypeUtil.getTypeArguments(decodeType)[1];

            @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) encodedContent;

            final Set<Map.Entry<String, Object>> encodedEntries = map.entrySet();
            for (Map.Entry<String, Object> encodedEntry : encodedEntries) {
                final Object encodedValue = encodedEntry.getValue();
                final Object decodedValue = decodeContent(encodedValue,
                    encodedType,
                    decodeValueType);
                if (encodedValue != decodedValue) {
                    map.put(encodedEntry.getKey(), decodedValue);
                }
            }

            return map;
        }

        return encodedContent;
    }

    @SuppressWarnings("unchecked")
    private Constructor<? extends Response<?>> identifyResponseCtr(Type callbackParamType) {
        final Type callbackTypeArgument = TypeUtil.getTypeArgument(callbackParamType);
        Class<? extends Response<?>> responseCls
            = (Class<? extends Response<?>>) TypeUtil.getRawClass(callbackTypeArgument);
        if (responseCls.equals(Response.class)) {
            responseCls = (Class<? extends Response<?>>) (Object) ResponseBase.class;
        } else if (responseCls.equals(PagedResponse.class)) {
            responseCls = (Class<? extends Response<?>>) (Object) PagedResponseBase.class;
        }
        Constructor<?>[] constructors = responseCls.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, (ctr1, ctr2) -> {
            final int paramCount1 = ctr1.getParameterTypes().length;
            final int paramCount2 = ctr2.getParameterTypes().length;
            return paramCount2 - paramCount1;
        });
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterTypes().length;
            if (paramCount >= 3 && paramCount <= 5) {
                try {
                    return (Constructor<? extends Response<?>>) constructor;
                } catch (Throwable t) {
                    throw logger.logExceptionAsError(new RuntimeException(t));
                }
            }
        }

        throw logger.logExceptionAsError(new IllegalStateException(
            "Cannot find suitable constructor for the response class "
                + responseCls));
    }

    private Response<?> instantiateResponse(Constructor<? extends Response<?>> responseCtr,
                                            int responseCtrParamCount,
                                            HttpRequest httpRequest,
                                            HttpResponse httpResponse,
                                            Object headerAsObject,
                                            Object bodyAsObject) {
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        switch (responseCtrParamCount) {
            case 3:
                try {
                    return responseCtr.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders);
                } catch (IllegalAccessException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 3-parameter response. ", e));
                } catch (InvocationTargetException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 3-parameter response. ", e));
                } catch (InstantiationException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 3-parameter response. ", e));
                }
            case 4:
                try {
                    return responseCtr.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders,
                        bodyAsObject);
                } catch (IllegalAccessException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 4-parameter response. ", e));
                } catch (InvocationTargetException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 4-parameter response. ", e));
                } catch (InstantiationException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 4-parameter response. ", e));
                }
            case 5:
                try {
                    return responseCtr.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders,
                        bodyAsObject,
                        headerAsObject);
                } catch (IllegalAccessException e) {
                    String message = String.format("Failed to deserialize 5-parameter response %s decoded headers. ",
                        headerAsObject != null ? "with" : "without");
                    throw logger.logExceptionAsError(new RuntimeException(message, e));
                } catch (InvocationTargetException e) {
                    String message = String.format("Failed to deserialize 5-parameter response %s decoded headers. ",
                        headerAsObject != null ? "with" : "without");
                    throw logger.logExceptionAsError(new RuntimeException(message, e));
                } catch (InstantiationException e) {
                    String message = String.format("Failed to deserialize 5-parameter response %s decoded headers. ",
                        headerAsObject != null ? "with" : "without");
                    throw logger.logExceptionAsError(new RuntimeException(message, e));
                }
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Response constructor with expected parameters not found."));
        }
    }
}