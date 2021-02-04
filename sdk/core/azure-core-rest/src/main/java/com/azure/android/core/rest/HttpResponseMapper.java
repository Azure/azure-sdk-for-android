// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import android.util.Pair;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.rest.implementation.HttpResponseExceptionInfo;
import com.azure.android.core.logging.ClientLogger;
import com.azure.core.micro.util.Base64Url;
import com.azure.core.micro.util.DateTimeRfc1123;
import com.azure.core.micro.util.UnixTime;
import com.azure.android.core.rest.annotation.ExpectedResponses;
import com.azure.android.core.rest.annotation.ReturnValueWireType;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionType;
import com.azure.android.core.rest.implementation.ItemPage;
import com.azure.android.core.rest.implementation.TypeUtil;
import com.azure.core.serde.SerdeAdapter;
import com.azure.core.serde.SerdeEncoding;
import com.azure.core.serde.SerdeParseException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class HttpResponseMapper {
    private final ClientLogger logger;

    private static final String NON_PARAMETERIZED_RESPONSE
        = "The %s type argument of the %s parameter in the method %s must be parameterized as %s.";
    private static final int CATEGORY_RESPONSE_BASE = 0;
    private static final int CATEGORY_RESPONSE_INTERFACE = 1;

    final Type headerType;
    final Type contentType;
    final Type contentEncodedType;
    final Type expandedContentEncodedType;
    private final BitSet expectedStatusCodes;
    private final HttpResponseExceptionInfo defaultExceptionInfo;
    private final Map<Integer, HttpResponseExceptionInfo> statusCodeToKnownExceptionInfo;
    private final Constructor<? extends Response<?>> responseCtr;

    HttpResponseMapper(Method swaggerMethod, Type callbackType, ClientLogger logger) {
        this.logger = logger;
        final String methodFullName = swaggerMethod.getDeclaringClass().getName() + "." + swaggerMethod.getName();

        final Pair<Type, Type> headerAndContentType = extractHeaderAndContentType(callbackType, methodFullName);
        this.headerType = headerAndContentType.first;
        this.contentType = headerAndContentType.second;

        this.contentEncodedType = extractContentEncodedType(swaggerMethod);
        if (this.contentEncodedType != null && !TypeUtil.isTypeOrSubTypeOf(this.contentEncodedType, Page.class)) {
            this.expandedContentEncodedType = expandContentEncodedType(this.contentEncodedType, this.contentType);
        } else {
            this.expandedContentEncodedType = null;
        }

        this.expectedStatusCodes = extractExpectedStatusCodes(swaggerMethod);

        Pair<HttpResponseExceptionInfo, Map<Integer, HttpResponseExceptionInfo>> defaultAndKnownExceptions
            = extractDefaultAndKnownExceptions(swaggerMethod);
        this.defaultExceptionInfo = defaultAndKnownExceptions.first;
        this.statusCodeToKnownExceptionInfo = defaultAndKnownExceptions.second;

        this.responseCtr = identifyResponseCtr(callbackType);
    }

    Response<?> map(HttpResponse httpResponse, SerdeAdapter serdeAdapter) throws Throwable {
        if (!isExpectedStatusCode(httpResponse.getStatusCode())) {
            final HttpResponseExceptionInfo exceptionInfo = getExceptionInfo(httpResponse.getStatusCode());
            throw logger.logThrowableAsError((exceptionInfo.instantiateException(serdeAdapter,
                httpResponse,
                logger)));
        } else {
            Object headerObject = null;
            if (this.headerType != null) {
                try {
                    headerObject = serdeAdapter.deserialize(httpResponse.getHeaders().toMap(), headerType);
                } catch (IOException ioe) {
                    throw logger.logExceptionAsError(
                        new HttpResponseException("HTTP response has malformed headers", httpResponse, ioe));
                }
            }

            if (isBooleanResponseForHead(httpResponse)) {
                final boolean isSuccess = (httpResponse.getStatusCode() / 100) == 2;
                httpResponse.close();
                return instantiateResponse(this.responseCtr,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    isSuccess);
            } else if (TypeUtil.isTypeOrSubTypeOf(this.contentType, Void.class)) {
                httpResponse.close();
                return instantiateResponse(this.responseCtr,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    null);
            } else if (TypeUtil.isTypeOrSubTypeOf(this.contentType, InputStream.class)) {
                return instantiateResponse(this.responseCtr,
                    httpResponse.getRequest(),
                    httpResponse,
                    headerObject,
                    httpResponse.getBody());
            } else {
                if (this.contentEncodedType == null) {
                    if (TypeUtil.isTypeOrSubTypeOf(this.contentType, byte[].class)) {
                        return instantiateResponse(this.responseCtr,
                            httpResponse.getRequest(),
                            httpResponse,
                            headerObject,
                            httpResponse.getBodyAsByteArray());
                    } else {
                        final Object decodedContent = deserializeHttpBody(serdeAdapter,
                            httpResponse,
                            this.contentType);
                        return instantiateResponse(this.responseCtr,
                            httpResponse.getRequest(),
                            httpResponse,
                            headerObject,
                            decodedContent);
                    }
                } else {
                    if (TypeUtil.isTypeOrSubTypeOf(this.contentEncodedType, Page.class)) {
                        final Type pageType = (this.contentEncodedType == Page.class)
                            ? TypeUtil.createParameterizedType(ItemPage.class, this.contentType)
                            : this.contentEncodedType;

                        final Object decodedContent = deserializeHttpBody(serdeAdapter, httpResponse, pageType);
                        return instantiateResponse(this.responseCtr,
                            httpResponse.getRequest(),
                            httpResponse,
                            headerObject,
                            decodedContent);
                    } else {
                        Objects.requireNonNull(this.expandedContentEncodedType);

                        if (this.expandedContentEncodedType == Base64Url.class
                            && TypeUtil.isTypeOrSubTypeOf(this.contentType, byte[].class)) {
                            final byte[] encodedContent = httpResponse.getBodyAsByteArray();
                            final byte[] decodedContent = new Base64Url(encodedContent).decodedBytes();
                            return instantiateResponse(this.responseCtr,
                                httpResponse.getRequest(),
                                httpResponse,
                                headerObject,
                                decodedContent);
                        } else {
                            final Object encodedContent = deserializeHttpBody(serdeAdapter,
                                httpResponse,
                                this.expandedContentEncodedType);
                            final Object decodedContent = decodeContent(encodedContent,
                                this.contentEncodedType,
                                this.contentType);

                            return instantiateResponse(this.responseCtr,
                                httpResponse.getRequest(),
                                httpResponse,
                                headerObject,
                                decodedContent);
                        }
                    }
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
            && (TypeUtil.isTypeOrSubTypeOf(this.contentType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(this.contentType, Boolean.class));
    }

    private Object deserializeHttpBody(SerdeAdapter serdeAdapter, HttpResponse httpResponse, Type bodyType) {
        try {
            return serdeAdapter.deserialize(httpResponse.getBody(), bodyType,
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

        final UnexpectedResponseExceptionType[] unexpectedResponseExceptionTypes
            = swaggerMethod.getAnnotationsByType(UnexpectedResponseExceptionType.class);

        if (unexpectedResponseExceptionTypes != null && unexpectedResponseExceptionTypes.length > 0) {

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
        } else {
            return Pair.create(new HttpResponseExceptionInfo(HttpResponseException.class), null);
        }
    }

    private Pair<Type, Type> extractHeaderAndContentType(Type callbackType, String swaggerMethodName) {
        final int category = identifyResponseCategory(callbackType, swaggerMethodName);
        if (category == CATEGORY_RESPONSE_BASE) {

            Type userHeaderType = null;
            Type userContentType = null;
            Type responseTypeItr = TypeUtil.getTypeArgument(callbackType);
            do {
                final Type[] responseArgTypes = TypeUtil.getTypeArguments(responseTypeItr);
                if (responseArgTypes != null && responseArgTypes.length > 0) {
                    if (responseArgTypes.length >= 2) {
                        if (userHeaderType == null) {
                            userHeaderType = responseArgTypes[responseArgTypes.length - 2];
                        }
                    }
                    if (userContentType == null) {
                        userContentType = responseArgTypes[responseArgTypes.length - 1];
                    }
                }
                responseTypeItr = TypeUtil.getSuperType(responseTypeItr);
            } while (userHeaderType == null || userContentType == null);
            return Pair.create(userHeaderType, userContentType);

        } else if (category == CATEGORY_RESPONSE_INTERFACE) {

            Type userContentType = null;
            Type responseTypeItr = TypeUtil.getTypeArgument(callbackType);
            do {
                final Type[] responseArgTypes = TypeUtil.getTypeArguments(responseTypeItr);
                if (responseArgTypes != null && responseArgTypes.length > 0) {
                    userContentType = responseArgTypes[responseArgTypes.length - 1];
                }
                responseTypeItr = TypeUtil.getSuperType(responseTypeItr);
            } while (userContentType == null);

            return Pair.create(null, userContentType);

        } else {
            throw logger.logExceptionAsError(new IllegalStateException("The type argument of "
                + Callback.class.getTypeName()
                + " in the method " + swaggerMethodName
                + " must either ResponseBase<H, C>, PagedResponseBase<H, C>, PagedResponse<C> or Response<C>"));
        }
    }

    private int identifyResponseCategory(Type callbackType, String swaggerMethodName) {
        final Type callbackTypeArgument = TypeUtil.getTypeArgument(callbackType);
        final boolean isResponseBase = TypeUtil.isTypeOrSubTypeOf(callbackTypeArgument, ResponseBase.class);
        if (isResponseBase) {
            final Type responseBaseType = TypeUtil.getSuperType(callbackTypeArgument, ResponseBase.class);
            if (!(responseBaseType instanceof ParameterizedType)) {
                throw logger.logExceptionAsError(new IllegalStateException(String.format(
                    NON_PARAMETERIZED_RESPONSE,
                    ResponseBase.class.getTypeName(), Callback.class.getTypeName(),
                    swaggerMethodName, "ResponseBase<FooHeader, Foo>>")));
            }
            return CATEGORY_RESPONSE_BASE;
        }

        final boolean isPagedResponseBase = TypeUtil.isTypeOrSubTypeOf(callbackTypeArgument, PagedResponseBase.class);
        if (isPagedResponseBase) {
            final Type responseBaseType = TypeUtil.getSuperType(callbackTypeArgument, PagedResponseBase.class);
            if (!(responseBaseType instanceof ParameterizedType)) {
                throw logger.logExceptionAsError(new IllegalStateException(String.format(
                    NON_PARAMETERIZED_RESPONSE,
                    PagedResponseBase.class.getTypeName(), Callback.class.getTypeName(),
                    swaggerMethodName, "PagedResponseBase<FooHeader, Foo>>")));
            }
            return CATEGORY_RESPONSE_BASE;
        }

        final boolean isPagedResponseInterface = TypeUtil.isTypeOrSubTypeOf(callbackTypeArgument, PagedResponse.class);
        if (isPagedResponseInterface) {
            final Type responseInterfaceType = TypeUtil.getSuperType(callbackTypeArgument, PagedResponse.class);
            if (!(responseInterfaceType instanceof ParameterizedType)) {
                throw logger.logExceptionAsError(new IllegalStateException(String.format(
                    NON_PARAMETERIZED_RESPONSE,
                    PagedResponse.class.getTypeName(), Callback.class.getTypeName(),
                    swaggerMethodName, "PagedResponse<Foo>")));
            }
            return CATEGORY_RESPONSE_INTERFACE;
        }

        final boolean isResponseInterface = TypeUtil.isTypeOrSubTypeOf(callbackTypeArgument, Response.class);
        if (isResponseInterface) {
            final Type responseInterfaceType = TypeUtil.getSuperType(callbackTypeArgument, Response.class);
            if (!(responseInterfaceType instanceof ParameterizedType)) {
                throw logger.logExceptionAsError(new IllegalStateException(String.format(
                    NON_PARAMETERIZED_RESPONSE,
                    Response.class.getTypeName(), Callback.class.getTypeName(),
                    swaggerMethodName, "Response<Foo>")));
            }
            return CATEGORY_RESPONSE_INTERFACE;
        }

        return -1;
    }

    private Type expandContentEncodedType(Type contentEncodedType, Type contentType) {
        Objects.requireNonNull(contentEncodedType);

        if (contentType == byte[].class) {
            if (contentEncodedType == Base64Url.class) {
                return Base64Url.class;
            }
        } else if (contentType == OffsetDateTime.class) {
            if (contentEncodedType == DateTimeRfc1123.class) {
                return DateTimeRfc1123.class;
            } else if (contentEncodedType == UnixTime.class) {
                return UnixTime.class;
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(contentType, List.class)) {
            final Type resultElementType = TypeUtil.getTypeArgument(contentType);
            final Type wireResponseElementType = expandContentEncodedType(contentEncodedType, resultElementType);

            return TypeUtil.createParameterizedType(((ParameterizedType) contentType).getRawType(),
                wireResponseElementType);
        } else if (TypeUtil.isTypeOrSubTypeOf(contentType, Map.class)) {
            final Type[] typeArguments = TypeUtil.getTypeArguments(contentType);
            final Type resultValueType = typeArguments[1];
            final Type wireResponseValueType = expandContentEncodedType(contentEncodedType, resultValueType);

            return TypeUtil.createParameterizedType(((ParameterizedType) contentType).getRawType(),
                typeArguments[0], wireResponseValueType);
        }
        return contentType;
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
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();
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

    private Response<?> instantiateResponse(Constructor<? extends Response<?>> constructor,
                                            HttpRequest httpRequest,
                                            HttpResponse httpResponse,
                                            Object headerAsObject,
                                            Object bodyAsObject) {
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = constructor.getParameterCount();
        switch (paramCount) {
            case 3:
                try {
                    return constructor.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 3-parameter response. ", e));
                }
            case 4:
                try {
                    return constructor.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders,
                        bodyAsObject);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw logger.logExceptionAsError(
                        new RuntimeException("Failed to deserialize 4-parameter response. ", e));
                }
            case 5:
                try {
                    return constructor.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders,
                        bodyAsObject,
                        headerAsObject);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    if (headerAsObject != null) {
                        throw logger.logExceptionAsError(
                            new RuntimeException("Failed to deserialize 5-parameter response"
                                + " with decoded headers. ", e));
                    } else {
                        throw logger.logExceptionAsError(
                            new RuntimeException("Failed to deserialize 5-parameter response"
                                + "without decoded headers.", e));
                    }
                }
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Response constructor with expected parameters not found."));
        }
    }
}
