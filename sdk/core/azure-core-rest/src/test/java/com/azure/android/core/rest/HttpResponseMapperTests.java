// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.http.exception.ResourceModifiedException;
import com.azure.android.core.http.exception.ResourceNotFoundException;
import com.azure.android.core.rest.annotation.ExpectedResponses;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.ReturnValueWireType;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionType;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionTypes;
import com.azure.android.core.rest.implementation.TypeUtil;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.serde.jackson.SerdeEncoding;
import com.azure.android.core.util.Base64Url;
import com.azure.android.core.util.DateTimeRfc1123;
import com.azure.android.core.util.UnixTime;
import com.azure.android.core.rest.annotation.Head;
import com.azure.android.core.serde.jackson.JacksonSerderAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.threeten.bp.OffsetDateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpResponseMapperTests {
    private final ClientLogger logger = new ClientLogger(HttpResponseMapperTests.class);

    interface EncodedContentMethods {
        @Get("test")
        void noEncode(Callback<Response<Void>> callback);

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64Url(Callback<Response<byte[]>> callback);

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTime(Callback<Response<OffsetDateTime>> callback);

        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123(Callback<Response<OffsetDateTime>> callback);

        @Get("test")
        @ReturnValueWireType(Page.class)
        void page(Callback<PagedResponse<Integer>> callback);

        @Get("test")
        @ReturnValueWireType(Boolean.class)
        void unknownEncode(Callback<Response<Boolean>> callback);
    }

    private static Stream<Arguments> encodedContentSupplier() throws NoSuchMethodException {
        Class<EncodedContentMethods> clazz = EncodedContentMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noEncode", Callback.class), null, null),
            Arguments.of(clazz.getDeclaredMethod("base64Url", Callback.class), Base64Url.class, Base64Url.class),
            Arguments.of(clazz.getDeclaredMethod("unixTime", Callback.class), UnixTime.class, UnixTime.class),
            Arguments.of(clazz.getDeclaredMethod("dateTimeRfc1123", Callback.class), DateTimeRfc1123.class,  DateTimeRfc1123.class),
            Arguments.of(clazz.getDeclaredMethod("page", Callback.class), Page.class, null),
            Arguments.of(clazz.getDeclaredMethod("unknownEncode", Callback.class), null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("encodedContentSupplier")
    public void encodedContent(Method method, Class<?> expectedWireType, Class<?> expandedExpectedWireType) {
        Type callbackType = extractCallbackType(method);
        HttpResponseMapper mapper = new HttpResponseMapper(method, callbackType, logger);
        assertEquals(expectedWireType, mapper.contentEncodedType);
    }

    interface ExpandedEncodedContentMethods {
        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123List(Callback<Response<List<OffsetDateTime>>> callback);

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTimeList(Callback<Response<List<OffsetDateTime>>> callback);

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64UrlList(Callback<Response<List<byte[]>>> callback);

        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123Map(Callback<Response<Map<String, OffsetDateTime>>> callback);

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTimeMap(Callback<Response<Map<String, OffsetDateTime>>> callback);

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64UrlMap(Callback<Response<Map<String, byte[]>>> callback);
    }

    private static Stream<Arguments> expandedEncodedContentSupplier() throws NoSuchMethodException {
        Class<ExpandedEncodedContentMethods> clazz = ExpandedEncodedContentMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("dateTimeRfc1123List", Callback.class),
                DateTimeRfc1123.class,
                TypeUtil.createParameterizedType(List.class, DateTimeRfc1123.class)),
            Arguments.of(clazz.getDeclaredMethod("unixTimeList", Callback.class),
                UnixTime.class,
                TypeUtil.createParameterizedType(List.class, UnixTime.class)),
            Arguments.of(clazz.getDeclaredMethod("base64UrlList", Callback.class),
                Base64Url.class,
                TypeUtil.createParameterizedType(List.class, Base64Url.class)),
            Arguments.of(clazz.getDeclaredMethod("dateTimeRfc1123Map", Callback.class),
                DateTimeRfc1123.class,
                TypeUtil.createParameterizedType(Map.class, String.class, DateTimeRfc1123.class)),
            Arguments.of(clazz.getDeclaredMethod("unixTimeMap", Callback.class),
                UnixTime.class,
                TypeUtil.createParameterizedType(Map.class, String.class, UnixTime.class)),
            Arguments.of(clazz.getDeclaredMethod("base64UrlMap", Callback.class),
                Base64Url.class,
                TypeUtil.createParameterizedType(Map.class, String.class, Base64Url.class))
        );
    }

    @ParameterizedTest
    @MethodSource("expandedEncodedContentSupplier")
    public void expandedEncodedContent(Method method, Class<?> expectedWireType, ParameterizedType parameterizedType) {
        Type callbackType = extractCallbackType(method);
        HttpResponseMapper mapper = new HttpResponseMapper(method, callbackType, logger);
        assertEquals(expectedWireType, mapper.contentEncodedType);
        assertNotNull(mapper.expandedContentEncodedType);
        assertTrue(mapper.expandedContentEncodedType instanceof ParameterizedType);
        assertEquals(((ParameterizedType) mapper.expandedContentEncodedType).getRawType(),
            parameterizedType.getRawType());
        assertArrayEquals(((ParameterizedType) mapper.expandedContentEncodedType).getActualTypeArguments(),
            parameterizedType.getActualTypeArguments());
    }

    interface ExpectedStatusCodeMethods {
        @Get("test")
        void noExpectedStatusCodes(Callback<Response<Void>> callback);

        @Get("test")
        @ExpectedResponses({ 200 })
        void only200IsExpected(Callback<Response<Void>> callback);

        @Get("test")
        @ExpectedResponses({ 429, 503 })
        void retryAfterExpected(Callback<Response<Void>> callback);
    }

    private static Stream<Arguments> expectedStatusCodeSupplier() throws NoSuchMethodException {
        Class<ExpectedStatusCodeMethods> clazz = ExpectedStatusCodeMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes", Callback.class), 200, null, true),
            Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes", Callback.class), 201, null, true),
            Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes", Callback.class), 400, null, false),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected", Callback.class), 200, new int[] {200}, true),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected", Callback.class), 201, new int[] {200}, false),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected", Callback.class), 400, new int[] {200}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected", Callback.class), 200, new int[] {429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected", Callback.class), 201, new int[] {429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected", Callback.class), 400, new int[] {429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected", Callback.class), 429, new int[] {429, 503}, true),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected", Callback.class), 503, new int[] {429, 503}, true)
        );
    }

    @ParameterizedTest
    @MethodSource("expectedStatusCodeSupplier")
    public void expectedStatusCodeSupplier(Method method, int statusCode, int[] expectedStatusCodes,
                                           boolean matchesExpected) {
        Type callbackType = extractCallbackType(method);
        HttpResponseMapper mapper = new HttpResponseMapper(method, callbackType, logger);

        if (expectedStatusCodes != null) {
            for (int expectedCode : expectedStatusCodes) {
                assertTrue(mapper.isExpectedStatusCode(expectedCode));
            }
        }
        assertEquals(matchesExpected, mapper.isExpectedStatusCode(statusCode));
    }

    interface KnownExceptionMethods {
        @Get("test")
        void noKnownException(Callback<Response<Void>> callback);

        @Get("test")
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = {400, 404})
        })
        void knownException(Callback<Response<Void>> callback);

        @Get("test")
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = {400, 404}),
            @UnexpectedResponseExceptionType(value = ResourceModifiedException.class)
        })
        void knownAndDefaultException(Callback<Response<Void>> callback);
    }

    private static Stream<Arguments> knownExceptionSupplier() throws NoSuchMethodException {
        Class<KnownExceptionMethods> clazz = KnownExceptionMethods.class;
        Method noKnownExceptionMethod = clazz.getDeclaredMethod("noKnownException", Callback.class);
        Method knownExceptionMethod = clazz.getDeclaredMethod("knownException", Callback.class);
        Method knownAndDefaultExceptionMethod = clazz.getDeclaredMethod("knownAndDefaultException", Callback.class);

        return Stream.of(
            Arguments.of(noKnownExceptionMethod, 500, HttpResponseException.class),
            Arguments.of(noKnownExceptionMethod, 400, HttpResponseException.class),
            Arguments.of(noKnownExceptionMethod, 404, HttpResponseException.class),
            Arguments.of(knownExceptionMethod, 500, HttpResponseException.class),
            Arguments.of(knownExceptionMethod, 400, ResourceNotFoundException.class),
            Arguments.of(knownExceptionMethod, 404, ResourceNotFoundException.class),
            Arguments.of(knownAndDefaultExceptionMethod, 500, ResourceModifiedException.class),
            Arguments.of(knownAndDefaultExceptionMethod, 400, ResourceNotFoundException.class),
            Arguments.of(knownAndDefaultExceptionMethod, 404, ResourceNotFoundException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("knownExceptionSupplier")
    public void knownException(Method method, int statusCode, Class<?> expectedExceptionType) {
        Type callbackType = extractCallbackType(method);
        HttpResponseMapper mapper = new HttpResponseMapper(method, callbackType, logger);
        assertEquals(expectedExceptionType, mapper.getExceptionInfo(statusCode).exceptionType);
    }

    interface ResponseTypeArgsMethods {
        @Get("test")
        void responseInterface(Callback<Response<Integer>> callback);

        @Get("test")
        void responseBase(Callback<ResponseBase<Float, Integer>> callback);

        @Get("test")
        void pagedResponseInterface(Callback<PagedResponse<Integer>> callback);

        @Get("test")
        void pagedResponseBase(Callback<PagedResponseBase<Float, Integer>> callback);
    }

    private static Stream<Arguments> responseTypeArgsSupplier() throws NoSuchMethodException {
        Class<ResponseTypeArgsMethods> clazz = ResponseTypeArgsMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("responseInterface", Callback.class), null, Integer.class),
            Arguments.of(clazz.getDeclaredMethod("responseBase", Callback.class), Float.class, Integer.class),
            Arguments.of(clazz.getDeclaredMethod("pagedResponseInterface", Callback.class), null, Integer.class),
            Arguments.of(clazz.getDeclaredMethod("pagedResponseBase", Callback.class), Float.class, Integer.class)
        );
    }

    @ParameterizedTest
    @MethodSource("responseTypeArgsSupplier")
    public void responseTypeArgs(Method method, Class<?> headerType, Class<?> contentType) {
        Type callbackType = extractCallbackType(method);
        HttpResponseMapper mapper = new HttpResponseMapper(method, callbackType, logger);
        assertEquals(contentType, mapper.contentDecodeType);
        assertEquals(headerType, mapper.headerDecodeType);
    }

    interface ResponseMissingTypeArgsMethods {
        @Get("test")
        void responseInterface(Callback<Response> callback);

        @Get("test")
        void responseBase(Callback<ResponseBase> callback);

        @Get("test")
        void responseBaseExtended(Callback<ExResponse<Float, Integer>> callback);
    }

    private static class ExResponse<H, B> extends ResponseBase {
        public ExResponse(HttpRequest request,
                          int statusCode,
                          HttpHeaders headers,
                          Object value,
                          Object deserializedHeaders) {
            super(request, statusCode, headers, value, deserializedHeaders);
        }
    }

    @Test
    public void responseMissingTypeArgs() throws NoSuchMethodException {
        Class<ResponseMissingTypeArgsMethods> clazz = ResponseMissingTypeArgsMethods.class;

        IllegalStateException ex1 = null;
        try {
            Method method1 = clazz.getDeclaredMethod("responseInterface", Callback.class);
            new HttpResponseMapper(method1, extractCallbackType(method1), logger);
        } catch (IllegalStateException e) {
            ex1 = e;
        }
        assertNotNull(ex1);
        assertTrue(ex1.getMessage().contains("The interface com.azure.android.core.rest.Response type argument of the com.azure.android.core.rest.Callback parameter"));

        IllegalStateException ex2 = null;
        try {
            Method method2 = clazz.getDeclaredMethod("responseBase", Callback.class);
            new HttpResponseMapper(method2, extractCallbackType(method2), logger);
        } catch (IllegalStateException e) {
            ex2 = e;
        }
        assertNotNull(ex2);
        assertTrue(ex2.getMessage().contains("The class com.azure.android.core.rest.ResponseBase type argument of the com.azure.android.core.rest.Callback parameter"));


        IllegalStateException ex3 = null;
        try {
            Method method3 = clazz.getDeclaredMethod("responseBaseExtended", Callback.class);
            new HttpResponseMapper(method3, extractCallbackType(method3), logger);
        } catch (IllegalStateException e) {
            ex3 = e;
        }
        assertNotNull(ex3);
        assertTrue(ex3.getMessage().contains("The class com.azure.android.core.rest.ResponseBase type argument of the com.azure.android.core.rest.Callback parameter"));
    }

    interface HeadMethods {
        @Head("test")
        @ExpectedResponses({ 200, 401 })
        void resourceExists(Callback<Response<Boolean>> callback);
    }

    @Test
    public void head() throws Throwable {
        Class<HeadMethods> clazz = HeadMethods.class;
        Method headMethod = clazz.getDeclaredMethod("resourceExists", Callback.class);
        HttpResponseMapper mapper = new HttpResponseMapper(headMethod, extractCallbackType(headMethod), logger);

        MockHttpResponse httpResponse = new MockHttpResponse(HttpMethod.HEAD,
            "https://raw.host.com", 200, new HttpHeaders(), new byte[0]);

       Response<Boolean> restResponse = (Response<Boolean>) mapper.map(httpResponse, new JacksonSerderAdapter());
       assertTrue(restResponse.getValue());
       assertTrue(httpResponse.isClosed());

        httpResponse = new MockHttpResponse(HttpMethod.HEAD,
            "https://raw.host.com", 401, new HttpHeaders(), new byte[0]);

        restResponse = (Response<Boolean>) mapper.map(httpResponse, new JacksonSerderAdapter());
        assertFalse(restResponse.getValue());
        assertTrue(httpResponse.isClosed());
    }

    interface VoidMethods {
        @Get("test")
        void noBody(Callback<Response<Void>> callback);
    }

    @Test
    public void voidBody() throws Throwable {
        Class<VoidMethods> clazz = VoidMethods.class;
        Method headMethod = clazz.getDeclaredMethod("noBody", Callback.class);
        HttpResponseMapper mapper = new HttpResponseMapper(headMethod, extractCallbackType(headMethod), logger);

        MockHttpResponse httpResponse = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), new byte[5]);

        Response<Boolean> restResponse = (Response<Boolean>) mapper.map(httpResponse, new JacksonSerderAdapter());
        assertNull(restResponse.getValue());
        assertTrue(httpResponse.isClosed());
    }

    interface StreamAndBytesMethods {
        @Get("test")
        void photoStream(Callback<Response<InputStream>> callback);

        @Get("test")
        void photoBytes(Callback<Response<byte[]>> callback);
    }

    @Test
    public void streamAndBytes() throws Throwable {
        byte[] wireBytes = new byte[20];
        new Random().nextBytes(wireBytes);

        Class<StreamAndBytesMethods> clazz = StreamAndBytesMethods.class;
        final Method photoStreamMethod = clazz.getDeclaredMethod("photoStream", Callback.class);
        HttpResponseMapper mapper1 = new HttpResponseMapper(photoStreamMethod, extractCallbackType(photoStreamMethod), logger);

        MockHttpResponse httpResponse1 = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireBytes);

        Response<InputStream> restStreamResponse = (Response<InputStream>) mapper1.map(httpResponse1, new JacksonSerderAdapter());
        InputStream photoStream = restStreamResponse.getValue();
        assertNotNull(photoStream);
        assertFalse(httpResponse1.isClosed());
        assertArrayEquals(wireBytes, streamToBytes(photoStream));

        final Method photoBytesMethod = clazz.getDeclaredMethod("photoBytes", Callback.class);
        HttpResponseMapper mapper2 = new HttpResponseMapper(photoBytesMethod, extractCallbackType(photoBytesMethod), logger);

        MockHttpResponse httpResponse2 = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireBytes);

        Response<byte[]> restBytesResponse = (Response<byte[]>) mapper2.map(httpResponse2, new JacksonSerderAdapter());
        byte[] photoBytes = restBytesResponse.getValue();
        assertNotNull(photoBytes);
        assertFalse(httpResponse2.isClosed());
        assertArrayEquals(wireBytes, photoBytes);
    }

    interface DeserializePojoMethods {
        @Get("test")
        void getPerson(Callback<Response<Person>> callback);
    }

    private static class Person {
        private String name;
        private int age;
        private OffsetDateTime dob;

        Person() {
        }

        Person(String name, int age, OffsetDateTime dob) {
            this.name = name;
            this.age = age;
            this.dob = dob;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public OffsetDateTime getDob() {
            return dob;
        }
    }

    @Test
    public void deserializePojo() throws Throwable {
        Class<DeserializePojoMethods> clazz = DeserializePojoMethods.class;
        Method getPersonMethod = clazz.getDeclaredMethod("getPerson", Callback.class);
        HttpResponseMapper mapper = new HttpResponseMapper(getPersonMethod, extractCallbackType(getPersonMethod), logger);

        Person person = new Person("John Doe", 40, OffsetDateTime.parse("1980-01-01T10:00:00Z"));
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();
        String wirePerson = serdeAdapter.serialize(person, SerdeEncoding.JSON);

        MockHttpResponse httpResponse = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wirePerson.getBytes());

        Response<Person> restStreamResponse = (Response<Person>) mapper.map(httpResponse, serdeAdapter);
        Person receivedPerson = restStreamResponse.getValue();

        assertEquals(person.getName(), receivedPerson.getName());
        assertEquals(person.getAge(), receivedPerson.getAge());
        assertEquals(person.getDob(), receivedPerson.getDob());
    }

    interface KnownErrorMethods {
        @Get("test")
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(value = Retry409Exception.class, code = 409)
        })
        void getError409(Callback<Response<Void>> callback);
    }

    public static class ErrorData409 {
        private int code;
        private String message;

        ErrorData409() {}

        ErrorData409(int code, String message) {
            this.code = code;
            this.message = message;
        }

        int getCode() {
            return this.code;
        }

        String getMessage() {
            return this.message;
        }
    }

    public static class Retry409Exception extends HttpResponseException {
        public Retry409Exception(final String message,
                                 final HttpResponse response,
                                 final ErrorData409 value) {
            super(message, response, value);
        }

        @Override
        public ErrorData409 getValue() {
            return (ErrorData409) super.getValue();
        }
    }

    @Test
    public void knownError() throws Throwable {
        Class<KnownErrorMethods> clazz = KnownErrorMethods.class;
        Method getError409Method = clazz.getDeclaredMethod("getError409", Callback.class);
        HttpResponseMapper mapper = new HttpResponseMapper(getError409Method, extractCallbackType(getError409Method), logger);

        ErrorData409 errorData409 = new ErrorData409(677, "retry after 10 sec");
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();
        String wireErrorData409 = serdeAdapter.serialize(errorData409, SerdeEncoding.JSON);

        MockHttpResponse httpResponse = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 409, new HttpHeaders(), wireErrorData409.getBytes());

        Retry409Exception ex = null;
        try {
            mapper.map(httpResponse, serdeAdapter);
        } catch (Retry409Exception e) {
            ex = e;
        }

        assertNotNull(ex);
        assertNotNull(ex.getValue());
        ErrorData409 receivedErrorData409 = ex.getValue();
        assertEquals(errorData409.getCode(), receivedErrorData409.getCode());
        assertEquals(errorData409.getMessage(), receivedErrorData409.getMessage());
    }

    interface AnyErrorMethods {
        @Get("test")
        void getAnyError(Callback<Response<Void>> callback);
    }

    @Test
    public void anyError() throws Throwable {
        Class<AnyErrorMethods> clazz = AnyErrorMethods.class;
        Method getAnyErrorMethod = clazz.getDeclaredMethod("getAnyError", Callback.class);
        HttpResponseMapper mapper = new HttpResponseMapper(getAnyErrorMethod, extractCallbackType(getAnyErrorMethod), logger);

        MockHttpResponse httpResponse = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 409, new HttpHeaders(), "Unknown error".getBytes());

        HttpResponseException ex = null;
        try {
            mapper.map(httpResponse, new JacksonSerderAdapter());
        } catch (HttpResponseException e) {
            ex = e;
        }

        assertNotNull(ex);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Unknown error"));
    }

    interface Base64EncodedContentMethods {
        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64Url(Callback<Response<byte[]>> callback);

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64UrlList(Callback<Response<List<byte[]>>> callback);

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64UrlMap(Callback<Response<Map<String, byte[]>>> callback);
    }

    @Test
    public void base64EncodedContent() throws Throwable {
        Class<Base64EncodedContentMethods> clazz = Base64EncodedContentMethods.class;

        Method base64UrlMethod = clazz.getDeclaredMethod("base64Url", Callback.class);
        HttpResponseMapper mapperBase64 = new HttpResponseMapper(base64UrlMethod, extractCallbackType(base64UrlMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        final String valueToEncode = "hello azure android";

        // aGVsbG8gYXp1cmUgYW5kcm9pZA==  (plain bytes).
        final byte[] base64EncodedBytes = Base64Url.encode(valueToEncode.getBytes()).encodedBytes();

        MockHttpResponse httpResponseBase64EncodedBytes0 = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), base64EncodedBytes);

        Response<byte[]> restResponseBase64DecodedBytes0 = (Response<byte[]>)mapperBase64.map(httpResponseBase64EncodedBytes0, serdeAdapter);
        byte[] decodedBytes0 = restResponseBase64DecodedBytes0.getValue();
        assertNotNull(decodedBytes0);
        assertArrayEquals(valueToEncode.getBytes(), decodedBytes0);

        // "aGVsbG8gYXp1cmUgYW5kcm9pZA==" (bytes wrapped in a string, so a valid json string).
        byte[] base64EncodedBytesAsJsonString = new byte[base64EncodedBytes.length + 2];
        System.arraycopy(base64EncodedBytes, 0, base64EncodedBytesAsJsonString, 1, base64EncodedBytes.length);
        base64EncodedBytesAsJsonString[0] = '"';
        base64EncodedBytesAsJsonString[base64EncodedBytesAsJsonString.length - 1] = '"';

        MockHttpResponse httpResponseBase64EncodedBytes1 = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), base64EncodedBytesAsJsonString);

        Response<byte[]> restResponseBase64DecodedBytes1 = (Response<byte[]>)mapperBase64.map(httpResponseBase64EncodedBytes1, serdeAdapter);
        byte[] decodedBytes1 = restResponseBase64DecodedBytes1.getValue();
        assertNotNull(decodedBytes1);
        assertArrayEquals(valueToEncode.getBytes(), decodedBytes1);
    }

    @Test
    public void base64EncodedContentList() throws Throwable {
        Class<Base64EncodedContentMethods> clazz = Base64EncodedContentMethods.class;

        Method base64UrlListMethod = clazz.getDeclaredMethod("base64UrlList", Callback.class);
        HttpResponseMapper mapperBase64List = new HttpResponseMapper(base64UrlListMethod, extractCallbackType(base64UrlListMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        final String value0ToEncode = "hello azure android";
        final String value1ToEncode = "hello azure android again";

        List<String> base64EncodedBytesList = new ArrayList<>();
        base64EncodedBytesList.add(Base64Url.encode(value0ToEncode.getBytes()).toString());
        base64EncodedBytesList.add(Base64Url.encode(value1ToEncode.getBytes()).toString());
        String wireBase64EncodedList = serdeAdapter.serialize(base64EncodedBytesList, SerdeEncoding.JSON);

        MockHttpResponse httpResponseBase64EncodedBytesList = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireBase64EncodedList.getBytes());

        Response<List<byte[]>> restResponseBase64DecodedBytesList
            = (Response<List<byte[]>>)mapperBase64List.map(httpResponseBase64EncodedBytesList, serdeAdapter);

        List<byte[]> decodedBytesList = restResponseBase64DecodedBytesList.getValue();
        assertNotNull(decodedBytesList);
        assertEquals(2, decodedBytesList.size());
        assertArrayEquals(value0ToEncode.getBytes(), decodedBytesList.get(0));
        assertArrayEquals(value1ToEncode.getBytes(), decodedBytesList.get(1));
    }

    @Test
    public void base64EncodedContentMap() throws Throwable {
        Class<Base64EncodedContentMethods> clazz = Base64EncodedContentMethods.class;

        Method base64UrlListMethod = clazz.getDeclaredMethod("base64UrlMap", Callback.class);
        HttpResponseMapper mapperBase64List = new HttpResponseMapper(base64UrlListMethod, extractCallbackType(base64UrlListMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        final String value0ToEncode = "hello azure android";
        final String value1ToEncode = "hello azure android again";

        Map<String, String> base64EncodedBytesMap = new HashMap<>();
        base64EncodedBytesMap.put("v0", Base64Url.encode(value0ToEncode.getBytes()).toString());
        base64EncodedBytesMap.put("v1", Base64Url.encode(value1ToEncode.getBytes()).toString());
        String wireBase64EncodedList = serdeAdapter.serialize(base64EncodedBytesMap, SerdeEncoding.JSON);

        MockHttpResponse httpResponseBase64DecodedBytesMap = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireBase64EncodedList.getBytes());

        Response<Map<String, byte[]>> restResponseBase64DecodedBytesMap
            = (Response<Map<String, byte[]>>)mapperBase64List.map(httpResponseBase64DecodedBytesMap, serdeAdapter);

        Map<String, byte[]> decodedBytesList = restResponseBase64DecodedBytesMap.getValue();
        assertNotNull(decodedBytesList);
        assertEquals(2, decodedBytesList.size());
        assertArrayEquals(value0ToEncode.getBytes(), decodedBytesList.get("v0"));
        assertArrayEquals(value1ToEncode.getBytes(), decodedBytesList.get("v1"));
    }

    interface DateTimeRfc1123EncodedContentMethods {
        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123(Callback<Response<OffsetDateTime>> callback);

        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123List(Callback<Response<List<OffsetDateTime>>> callback);

        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123Map(Callback<Response<Map<String, OffsetDateTime>>> callback);
    }

    @Test
    public void dateTimeRfc1123EncodedContent() throws Throwable {
        Class<DateTimeRfc1123EncodedContentMethods> clazz = DateTimeRfc1123EncodedContentMethods.class;

        Method dateTimeRfc1123Method = clazz.getDeclaredMethod("dateTimeRfc1123", Callback.class);
        HttpResponseMapper mapperDateTimeRfc1123 = new HttpResponseMapper(dateTimeRfc1123Method, extractCallbackType(dateTimeRfc1123Method), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("1980-01-01T10:00:00Z");
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(offsetDateTime);
        String wireDateTimeRfc1123JsonString = '"' + dateTimeRfc1123.toString() + '"';

        MockHttpResponse httpResponseDateTimeRfc1123 = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireDateTimeRfc1123JsonString.getBytes());

        Response<OffsetDateTime> restResponseBase64OffsetDataTime
            = (Response<OffsetDateTime>)mapperDateTimeRfc1123.map(httpResponseDateTimeRfc1123, serdeAdapter);

        OffsetDateTime dateTimeReceived = restResponseBase64OffsetDataTime.getValue();
        assertNotNull(dateTimeReceived);
        assertEquals(0, dateTimeReceived.compareTo(offsetDateTime));
    }

    @Test
    public void dateTimeRfc1123EncodedContentList() throws Throwable {
        Class<DateTimeRfc1123EncodedContentMethods> clazz = DateTimeRfc1123EncodedContentMethods.class;

        Method dateTimeRfc1123ListMethod = clazz.getDeclaredMethod("dateTimeRfc1123List", Callback.class);
        HttpResponseMapper mapperDateTimeRfc1123
            = new HttpResponseMapper(dateTimeRfc1123ListMethod, extractCallbackType(dateTimeRfc1123ListMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        OffsetDateTime offsetDateTime0 = OffsetDateTime.parse("1980-01-01T10:00:00Z");
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse("1981-01-01T10:00:00Z");

        List<String> wireDateTimeRfc1123List = new ArrayList<>();
        wireDateTimeRfc1123List.add(new DateTimeRfc1123(offsetDateTime0).toString());
        wireDateTimeRfc1123List.add(new DateTimeRfc1123(offsetDateTime1).toString());

        String wireDateTimeRfc1123JsonList = serdeAdapter.serialize(wireDateTimeRfc1123List, SerdeEncoding.JSON);

        MockHttpResponse httpResponseDateTimeRfc1123 = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireDateTimeRfc1123JsonList.getBytes());

        Response<List<OffsetDateTime>> httpResponseOffsetDateTimeList
            = (Response<List<OffsetDateTime>>)mapperDateTimeRfc1123.map(httpResponseDateTimeRfc1123, serdeAdapter);

        List<OffsetDateTime> dateTimeListReceived = httpResponseOffsetDateTimeList.getValue();
        assertNotNull(dateTimeListReceived);
        assertEquals(2, dateTimeListReceived.size());

        assertEquals(0, dateTimeListReceived.get(0).compareTo(offsetDateTime0));
        assertEquals(0, dateTimeListReceived.get(1).compareTo(offsetDateTime1));
    }

    @Test
    public void dateTimeRfc1123EncodedContentMap() throws Throwable {
        Class<DateTimeRfc1123EncodedContentMethods> clazz = DateTimeRfc1123EncodedContentMethods.class;

        Method dateTimeRfc1123MapMethod = clazz.getDeclaredMethod("dateTimeRfc1123Map", Callback.class);
        HttpResponseMapper mapperDateTimeRfc1123
            = new HttpResponseMapper(dateTimeRfc1123MapMethod, extractCallbackType(dateTimeRfc1123MapMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        OffsetDateTime offsetDateTime0 = OffsetDateTime.parse("1980-01-01T10:00:00Z");
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse("1981-01-01T10:00:00Z");

        Map<String, String> wireDateTimeRfc1123Map = new HashMap<>();
        wireDateTimeRfc1123Map.put("v0", new DateTimeRfc1123(offsetDateTime0).toString());
        wireDateTimeRfc1123Map.put("v1", new DateTimeRfc1123(offsetDateTime1).toString());

        String wireDateTimeRfc1123JsonMap = serdeAdapter.serialize(wireDateTimeRfc1123Map, SerdeEncoding.JSON);

        MockHttpResponse httpResponseOffsetDateTimeMap = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireDateTimeRfc1123JsonMap.getBytes());

        Response<Map<String, OffsetDateTime>> restResponseBase64OffsetDataTimeMap
            = (Response<Map<String, OffsetDateTime>>)mapperDateTimeRfc1123.map(httpResponseOffsetDateTimeMap, serdeAdapter);

        Map<String, OffsetDateTime> dateTimeMapReceived = restResponseBase64OffsetDataTimeMap.getValue();
        assertNotNull(dateTimeMapReceived);
        assertEquals(2, dateTimeMapReceived.size());

        assertEquals(0, dateTimeMapReceived.get("v0").compareTo(offsetDateTime0));
        assertEquals(0, dateTimeMapReceived.get("v1").compareTo(offsetDateTime1));
    }

    interface UnixTimeEncodedContentMethods {
        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTime(Callback<Response<OffsetDateTime>> callback);

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTimeList(Callback<Response<List<OffsetDateTime>>> callback);

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTimeMap(Callback<Response<Map<String, OffsetDateTime>>> callback);
    }

    @Test
    public void unixTimeEncodedContent() throws Throwable {
        Class<UnixTimeEncodedContentMethods> clazz = UnixTimeEncodedContentMethods.class;

        Method unixTimeMethod = clazz.getDeclaredMethod("unixTime", Callback.class);
        HttpResponseMapper mapperUnixTime = new HttpResponseMapper(unixTimeMethod, extractCallbackType(unixTimeMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("1980-01-01T10:00:00Z");
        UnixTime unixTime = new UnixTime(offsetDateTime);
        String wireUnixTimeJsonNumber = unixTime.toString();

        MockHttpResponse httpResponseUnixTime = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireUnixTimeJsonNumber.getBytes());

        Response<OffsetDateTime> restResponseUnixTime
            = (Response<OffsetDateTime>)mapperUnixTime.map(httpResponseUnixTime, serdeAdapter);

        OffsetDateTime dateTimeReceived = restResponseUnixTime.getValue();
        assertNotNull(dateTimeReceived);
        assertEquals(0, dateTimeReceived.compareTo(offsetDateTime));
    }

    @Test
    public void unixTimeEncodedContentList() throws Throwable {
        Class<UnixTimeEncodedContentMethods> clazz = UnixTimeEncodedContentMethods.class;

        Method unixTimeListMethod = clazz.getDeclaredMethod("unixTimeList", Callback.class);
        HttpResponseMapper mapperUnixTime
            = new HttpResponseMapper(unixTimeListMethod, extractCallbackType(unixTimeListMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        OffsetDateTime offsetDateTime0 = OffsetDateTime.parse("1980-01-01T10:00:00Z");
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse("1981-01-01T10:00:00Z");

        List<Integer> wireUnixTimeList = new ArrayList<>();
        wireUnixTimeList.add(Integer.parseInt(new UnixTime(offsetDateTime0).toString()));
        wireUnixTimeList.add(Integer.parseInt(new UnixTime(offsetDateTime1).toString()));

        String wireUnixTimeJsonList = serdeAdapter.serialize(wireUnixTimeList, SerdeEncoding.JSON);

        MockHttpResponse httpResponseOffsetDateTimeList = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireUnixTimeJsonList.getBytes());

        Response<List<OffsetDateTime>> restResponseBase64OffsetDataTimeList
            = (Response<List<OffsetDateTime>>)mapperUnixTime.map(httpResponseOffsetDateTimeList, serdeAdapter);

        List<OffsetDateTime> dateTimeListReceived = restResponseBase64OffsetDataTimeList.getValue();
        assertNotNull(dateTimeListReceived);
        assertEquals(2, dateTimeListReceived.size());

        assertEquals(0, dateTimeListReceived.get(0).compareTo(offsetDateTime0));
        assertEquals(0, dateTimeListReceived.get(1).compareTo(offsetDateTime1));
    }

    @Test
    public void unixTimeEncodedContentMap() throws Throwable {
        Class<UnixTimeEncodedContentMethods> clazz = UnixTimeEncodedContentMethods.class;

        Method unixTimeMapMethod = clazz.getDeclaredMethod("unixTimeMap", Callback.class);
        HttpResponseMapper mapperDateTimeRfc1123
            = new HttpResponseMapper(unixTimeMapMethod, extractCallbackType(unixTimeMapMethod), logger);
        JacksonSerderAdapter serdeAdapter = new JacksonSerderAdapter();

        OffsetDateTime offsetDateTime0 = OffsetDateTime.parse("1980-01-01T10:00:00Z");
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse("1981-01-01T10:00:00Z");

        Map<String, Integer> wireUnixTimeMap = new HashMap<>();
        wireUnixTimeMap.put("v0", Integer.parseInt(new UnixTime(offsetDateTime0).toString()));
        wireUnixTimeMap.put("v1", Integer.parseInt(new UnixTime(offsetDateTime1).toString()));

        String wireDateTimeRfc1123JsonMap = serdeAdapter.serialize(wireUnixTimeMap, SerdeEncoding.JSON);

        MockHttpResponse httpResponseDateTimeOffsetMap = new MockHttpResponse(HttpMethod.GET,
            "https://raw.host.com", 200, new HttpHeaders(), wireDateTimeRfc1123JsonMap.getBytes());

        Response<Map<String, OffsetDateTime>> restResponseOffsetDataTimeMap
            = (Response<Map<String, OffsetDateTime>>)mapperDateTimeRfc1123.map(httpResponseDateTimeOffsetMap, serdeAdapter);

        Map<String, OffsetDateTime> dateTimeMapReceived = restResponseOffsetDataTimeMap.getValue();
        assertNotNull(dateTimeMapReceived);
        assertEquals(2, dateTimeMapReceived.size());

        assertEquals(0, dateTimeMapReceived.get("v0").compareTo(offsetDateTime0));
        assertEquals(0, dateTimeMapReceived.get("v1").compareTo(offsetDateTime1));
    }

    private byte[] streamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private Type extractCallbackType(Method swaggerMethod) {
        final Type[] methodParamTypes = swaggerMethod.getGenericParameterTypes();
        final Type methodLastParamType = (methodParamTypes != null && methodParamTypes.length > 0)
            ? methodParamTypes[methodParamTypes.length - 1]
            : null;

        if (methodLastParamType == null || !TypeUtil.isTypeOrSubTypeOf(methodLastParamType, Callback.class)) {
            Assertions.fail();
        }

        final Type callbackType = methodLastParamType;
        if (!(callbackType instanceof ParameterizedType)) {
            Assertions.fail();
        }
        return callbackType;
    }

    private static class MockHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final byte[] body;
        private boolean isClosed;

        protected MockHttpResponse(HttpMethod httpMethod,
                                   String url,
                                   int statusCode,
                                   HttpHeaders headers,
                                   byte [] body) {
            super(new HttpRequest(httpMethod, url));
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
        }

        public boolean isClosed() {
            return this.isClosed;
        }

        @Override
        public int getStatusCode() {
            return this.statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return this.headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(this.body);
        }

        @Override
        public byte[] getBodyAsByteArray() {
            return this.body;
        }

        @Override
        public String getBodyAsString() {
            return this.getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public String getBodyAsString(Charset charset) {
            return new String(this.body, charset);
        }

        @Override
        public void close() {
            super.close();
            this.isClosed = true;
        }
    }
}
