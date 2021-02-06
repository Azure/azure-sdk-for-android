// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.util.UrlBuilder;
import com.azure.android.core.rest.annotation.BodyParam;
import com.azure.android.core.rest.annotation.Delete;
import com.azure.android.core.rest.annotation.FormParam;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.Head;
import com.azure.android.core.rest.annotation.HeaderParam;
import com.azure.android.core.rest.annotation.Headers;
import com.azure.android.core.rest.annotation.HostParam;
import com.azure.android.core.rest.annotation.Patch;
import com.azure.android.core.rest.annotation.PathParam;
import com.azure.android.core.rest.annotation.Post;
import com.azure.android.core.rest.annotation.Put;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.rest.annotation.QueryParam;
import com.azure.android.core.serde.jackson.JacksonSerderAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestMapperTests {
    interface OperationMethods {
        void noMethod();

        @Get("test")
        void getMethod();

        @Put("test")
        void putMethod();

        @Head("test")
        void headMethod();

        @Delete("test")
        void deleteMethod();

        @Post("test")
        void postMethod();

        @Patch("test")
        void patchMethod();
    }

    @Test
    public void noHttpMethodAnnotation() throws NoSuchMethodException {
        Method noHttpMethodAnnotation = OperationMethods.class.getDeclaredMethod("noMethod");
        Exception ex = null;
        try {
            new HttpRequestMapper("s://raw.host.com", noHttpMethodAnnotation, new JacksonSerderAdapter());
        } catch (RuntimeException e) {
            ex = e;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex.getMessage().contains("Either Get, Put, Head, Delete, Post or Patch "
            + "annotation must be defined on the method"));
    }

    private static Stream<Arguments> httpMethodSupplier() throws NoSuchMethodException {
        Class<OperationMethods> clazz = OperationMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("getMethod"), HttpMethod.GET, "test",
                "com.azure.android.core.rest.httpRequestMapperTests$OperationMethods.getMethod"),
            Arguments.of(clazz.getDeclaredMethod("putMethod"), HttpMethod.PUT, "test",
                "com.azure.android.core.rest.httpRequestMapperTests$OperationMethods.putMethod"),
            Arguments.of(clazz.getDeclaredMethod("headMethod"), HttpMethod.HEAD, "test",
                "com.azure.android.core.rest.httpRequestMapperTests$OperationMethods.headMethod"),
            Arguments.of(clazz.getDeclaredMethod("deleteMethod"), HttpMethod.DELETE, "test",
                "com.azure.android.core.rest.httpRequestMapperTests$OperationMethods.deleteMethod"),
            Arguments.of(clazz.getDeclaredMethod("postMethod"), HttpMethod.POST, "test",
                "com.azure.android.core.rest.httpRequestMapperTests$OperationMethods.postMethod"),
            Arguments.of(clazz.getDeclaredMethod("patchMethod"), HttpMethod.PATCH, "test",
                "com.azure.android.core.rest.httpRequestMapperTests$OperationMethods.patchMethod")
        );
    }

    @ParameterizedTest
    @MethodSource("httpMethodSupplier")
    public void httpMethod(Method method, HttpMethod expectedMethod, String expectedRelativePath,
                           String expectedFullyQualifiedName) {

        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com", method, new JacksonSerderAdapter());
        assertEquals(expectedMethod, mapper.getHttpMethod());
        assertEquals(expectedRelativePath, mapper.applyPathMappings(null));
    }

    interface HeaderMethods {
        @Get("test")
        void noHeaders();

        @Get("test")
        @Headers({"", ":", "nameOnly:", ":valueOnly"})
        void malformedHeaders();

        @Get("test")
        @Headers({"name1:value1", " name2: value2", "name3 :value3 "})
        void headers();

        @Get("test")
        @Headers({"name:value1", "name:value2"})
        void sameKeyTwiceLastWins();
    }

    private static Stream<Arguments> headersSupplier() throws NoSuchMethodException {
        Class<HeaderMethods> clazz = HeaderMethods.class;
        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noHeaders"), new HttpHeaders()),
            Arguments.of(clazz.getDeclaredMethod("malformedHeaders"), new HttpHeaders()),
            Arguments.of(clazz.getDeclaredMethod("headers"), new HttpHeaders()
                .put("name1", "value1").put("name2", "value2").put("name3", "value3")),
            Arguments.of(clazz.getDeclaredMethod("sameKeyTwiceLastWins"), new HttpHeaders().put("name", "value2"))
        );
    }

    @ParameterizedTest
    @MethodSource("headersSupplier")
    public void headers(Method method, HttpHeaders expectedHeaders) {
        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com", method, new JacksonSerderAdapter());

        HttpHeaders actual = new HttpHeaders();
        mapper.applyHeaderMappings(null, actual);

        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.getValue(header.getName()), header.getValue());
        }
    }

    interface HostSubstitutionMethods {
        @Get("test")
        void noSubstitutions(String sub1);

        @Get("test")
        void substitution(@HostParam("sub1") String sub1);

        @Get("test")
        void encodingSubstitution(@HostParam(value = "sub1", encoded = false) String sub1);
    }

    private static Stream<Arguments> hostSubstitutionSupplier() throws NoSuchMethodException {
        String sub1RawHost = "https://{sub1}.host.com";
        String sub2RawHost = "https://{sub2}.host.com";

        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, sub1RawHost, toObjectArray("raw"), "https://{sub1}.host.com"),
            Arguments.of(noSubstitutions, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("raw"), "https://raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("{sub1}"), "https://{sub1}.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray((String) null), "https://.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "https://{sub1}.host.com"),
            Arguments.of(substitution, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("raw"), "https://raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("{sub1}"), "https://%7Bsub1%7D.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray((String) null), "https://.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "https://{sub1}.host.com"),
            Arguments.of(encodingSubstitution, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com")
        );
    }

    @ParameterizedTest
    @MethodSource("hostSubstitutionSupplier")
    public void hostSubstitution(Method method, String rawHost, Object[] arguments, String expectedUrl) {
        HttpRequestMapper mapper = new HttpRequestMapper(rawHost, method, new JacksonSerderAdapter());
        UrlBuilder urlBuilder = new UrlBuilder();
        mapper.applySchemeAndHostMapping(arguments, urlBuilder);
        assertEquals(expectedUrl, urlBuilder.toString());
    }

    private static Stream<Arguments> schemeSubstitutionSupplier() throws NoSuchMethodException {
        String sub1RawHost = "{sub1}://raw.host.com";
        String sub2RawHost = "{sub2}://raw.host.com";

        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, sub1RawHost, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(noSubstitutions, sub2RawHost, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("http"), "http://raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("ĥttps"), "ĥttps://raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray((String) null), "raw.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "raw.host.com"),
            Arguments.of(substitution, sub2RawHost, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("http"), "http://raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("ĥttps"), "raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray((String) null), "raw.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "raw.host.com"),
            Arguments.of(encodingSubstitution, sub2RawHost, toObjectArray("raw"), "raw.host.com")
        );
    }

    @ParameterizedTest
    @MethodSource("schemeSubstitutionSupplier")
    public void schemeSubstitution(Method method, String rawHost, Object[] arguments, String expectedUrl) {
        HttpRequestMapper mapper = new HttpRequestMapper(rawHost, method, new JacksonSerderAdapter());
        UrlBuilder urlBuilder = new UrlBuilder();
        mapper.applySchemeAndHostMapping(arguments, urlBuilder);
        assertEquals(expectedUrl, urlBuilder.toString());
    }

    interface PathSubstitutionMethods {
        @Get("{sub1}")
        void noSubstitutions(String sub1);

        @Get("{sub1}")
        void substitution(@PathParam("sub1") String sub1);

        @Get("{sub1}")
        void encodedSubstitution(@PathParam(value = "sub1", encoded = true) String sub1);
    }

    private static Stream<Arguments> pathSubstitutionSupplier() throws NoSuchMethodException {
        Class<PathSubstitutionMethods> clazz = PathSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodedSubstitution = clazz.getDeclaredMethod("encodedSubstitution", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, toObjectArray("path"), "{sub1}"),
            Arguments.of(encodedSubstitution, toObjectArray("path"), "path"),
            Arguments.of(encodedSubstitution, toObjectArray("{sub1}"), "{sub1}"),
            Arguments.of(encodedSubstitution, toObjectArray((String) null), ""),
            Arguments.of(substitution, toObjectArray("path"), "path"),
            Arguments.of(substitution, toObjectArray("{sub1}"), "%7Bsub1%7D"),
            Arguments.of(substitution, toObjectArray((String) null), "")
        );
    }

    @ParameterizedTest
    @MethodSource("pathSubstitutionSupplier")
    public void pathSubstitution(Method method, Object[] arguments, String expectedPath) {
        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com", method, new JacksonSerderAdapter());
        assertEquals(expectedPath, mapper.applyPathMappings(arguments));
    }

    interface QuerySubstitutionMethods {
        @Get("test")
        void substitutions(@QueryParam("sub1") String sub1, @QueryParam("sub2") boolean sub2);

        @Get("test")
        void encodedSubstitutions(@QueryParam(value = "sub1", encoded = true) String sub1,
                                  @QueryParam(value = "sub2", encoded = true) boolean sub2);
    }

    private static Stream<Arguments> querySubstitutionSupplier() throws NoSuchMethodException {
        Class<QuerySubstitutionMethods> clazz = QuerySubstitutionMethods.class;
        Method substitution = clazz.getDeclaredMethod("substitutions", String.class, boolean.class);
        Method encodedSubstitution = clazz.getDeclaredMethod("encodedSubstitutions", String.class, boolean.class);

        return Stream.of(
            Arguments.of(substitution, null, "https://raw.host.com"),
            Arguments.of(substitution, toObjectArray("raw", true), "https://raw.host.com?sub1=raw&sub2=true"),
            Arguments.of(substitution, toObjectArray(null, true), "https://raw.host.com?sub2=true"),
            Arguments.of(substitution, toObjectArray("{sub1}", false),
                "https://raw.host.com?sub1=%7Bsub1%7D&sub2=false"),
            Arguments.of(encodedSubstitution, null, "https://raw.host.com"),
            Arguments.of(encodedSubstitution, toObjectArray("raw", true), "https://raw.host.com?sub1=raw&sub2=true"),
            Arguments.of(encodedSubstitution, toObjectArray(null, true), "https://raw.host.com?sub2=true"),
            Arguments.of(encodedSubstitution, toObjectArray("{sub1}", false),
                "https://raw.host.com?sub1={sub1}&sub2=false")
        );
    }

    @ParameterizedTest
    @MethodSource("querySubstitutionSupplier")
    public void querySubstitution(Method method, Object[] arguments, String expectedUrl) {
        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com", method, new JacksonSerderAdapter());
        UrlBuilder urlBuilder = UrlBuilder.parse("https://raw.host.com");
        mapper.applyQueryMappings(arguments, urlBuilder);
        assertEquals(expectedUrl, urlBuilder.toString());
    }

    interface HeaderSubstitutionMethods {
        @Get("test")
        void addHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @Get("test")
        @Headers({ "sub1:sub1", "sub2:false" })
        void overrideHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @Get("test")
        void headerMap(@HeaderParam("x-ms-meta-") Map<String, String> headers);
    }

    private static Stream<Arguments> headerSubstitutionSupplier() throws NoSuchMethodException {
        Class<HeaderSubstitutionMethods> clazz = HeaderSubstitutionMethods.class;
        Method addHeaders = clazz.getDeclaredMethod("addHeaders", String.class, boolean.class);
        Method overrideHeaders = clazz.getDeclaredMethod("overrideHeaders", String.class, boolean.class);
        Method headerMap = clazz.getDeclaredMethod("headerMap", Map.class);

        Map<String, String> simpleHeaderMap = Collections.singletonMap("key", "value");
        Map<String, String> expectedSimpleHeadersMap = Collections.singletonMap("x-ms-meta-key", "value");

        Map<String, String> complexHeaderMap = new HttpHeaders().put("key1", null).put("key2", "value2").toMap();
        Map<String, String> expectedComplexHeaderMap = Collections.singletonMap("x-ms-meta-key2", "value2");

        return Stream.of(
            Arguments.of(addHeaders, null, null),
            Arguments.of(addHeaders, toObjectArray("header", true), createExpectedParameters("header", true)),
            Arguments.of(addHeaders, toObjectArray(null, true), createExpectedParameters(null, true)),
            Arguments.of(addHeaders, toObjectArray("{sub1}", false), createExpectedParameters("{sub1}", false)),
            Arguments.of(overrideHeaders, null, createExpectedParameters("sub1", false)),
            Arguments.of(overrideHeaders, toObjectArray(null, true), createExpectedParameters("sub1", true)),
            Arguments.of(overrideHeaders, toObjectArray("header", false), createExpectedParameters("header", false)),
            Arguments.of(overrideHeaders, toObjectArray("{sub1}", true), createExpectedParameters("{sub1}", true)),
            Arguments.of(headerMap, null, null),
            Arguments.of(headerMap, toObjectArray(simpleHeaderMap), expectedSimpleHeadersMap),
            Arguments.of(headerMap, toObjectArray(complexHeaderMap), expectedComplexHeaderMap)
        );
    }

    @ParameterizedTest
    @MethodSource("headerSubstitutionSupplier")
    public void headerSubstitution(Method method, Object[] arguments, Map<String, String> expectedHeaders) {
        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com", method, new JacksonSerderAdapter());
        HttpHeaders actual = new HttpHeaders();
        mapper.applyHeaderMappings(arguments, actual);
        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.get(header.getName()), header.getValue());
        }
    }

    interface FormSubstitutionMethods {
//        @Get("test")
//        void applicationJsonBody(@BodyParam("application/json") String jsonBody);

        @Get("test")
        void formBody(@FormParam("name") String name, @FormParam("age") Integer age,
                      @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @Get("test")
        void encodedFormBody(@FormParam(value = "name", encoded = true) String name, @FormParam("age") Integer age,
                             @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @Get("test")
        void encodedFormKey(@FormParam(value = "x:ms:value") String value);

        @Get("test")
        void encodedFormKey2(@FormParam(value = "x:ms:value", encoded = true) String value);
    }

    private static Stream<Arguments> formSubstitutionSupplier() throws NoSuchMethodException {
        Class<FormSubstitutionMethods> clazz = FormSubstitutionMethods.class;
        Method formBody = clazz.getDeclaredMethod("formBody", String.class, Integer.class, OffsetDateTime.class,
            List.class);
        Method encodedFormBody = clazz.getDeclaredMethod("encodedFormBody", String.class, Integer.class,
            OffsetDateTime.class, List.class);
        Method encodedFormKey = clazz.getDeclaredMethod("encodedFormKey", String.class);
        Method encodedFormKey2 = clazz.getDeclaredMethod("encodedFormKey2", String.class);

        OffsetDateTime dob = OffsetDateTime.of(1980, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        List<String> favoriteColors = Arrays.asList("blue", "green");
        List<String> badFavoriteColors = Arrays.asList(null, "green");

        return Stream.of(
            Arguments.of(formBody, null, "application/x-www-form-urlencoded", null),
            Arguments.of(formBody, toObjectArray("John Doe", null, dob, null),
                "application/x-www-form-urlencoded",
                "name=John+Doe&dob=1980-01-01T00%3A00%3A00Z"),
            Arguments.of(formBody, toObjectArray("John Doe", 40, null, favoriteColors),
                "application/x-www-form-urlencoded",
                "name=John+Doe&age=40&favoriteColors=blue&favoriteColors=green"),
            Arguments.of(formBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                "application/x-www-form-urlencoded",
                "name=John+Doe&age=40&favoriteColors=green"),
            Arguments.of(encodedFormBody, null, "application/x-www-form-urlencoded", null),
            Arguments.of(encodedFormBody, toObjectArray("John Doe", null, dob, null),
                "application/x-www-form-urlencoded",
                "name=John Doe&dob=1980-01-01T00%3A00%3A00Z"),
            Arguments.of(encodedFormBody, toObjectArray("John Doe", 40, null, favoriteColors),
                "application/x-www-form-urlencoded",
                "name=John Doe&age=40&favoriteColors=blue&favoriteColors=green"),
            Arguments.of(encodedFormBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                "application/x-www-form-urlencoded",
                "name=John Doe&age=40&favoriteColors=green"),
            Arguments.of(encodedFormKey, toObjectArray("value"),
                "application/x-www-form-urlencoded",
                "x%3Ams%3Avalue=value"),
            Arguments.of(encodedFormKey2, toObjectArray("value"),
                "application/x-www-form-urlencoded",
                "x%3Ams%3Avalue=value")
        );
    }

    @ParameterizedTest
    @MethodSource("formSubstitutionSupplier")
    public void formSubstitution(Method method,
                                 Object[] arguments,
                                 String expectedBodyContentType,
                                 Object expectedBody) {
        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com", method, new JacksonSerderAdapter());
        assertEquals(expectedBody, mapper.applyFormDataMapping(arguments));
    }

    interface JsonBodySubstitutionMethods {
        @Get("test")
        void applicationJsonStringBody(@BodyParam("application/json") String jsonBody,
                                       Callback<Response<Void>> callback);
        @Get("test2")
        void applicationJsonPojoBody(@BodyParam("application/json") Person jsonBody,
                                     Callback<Response<Void>> callback);
    }

    private static class Person {
        private final String name;
        private final int age;
        private final OffsetDateTime dob;

        Person(String name, int age, OffsetDateTime dob) {
            this.name = name;
            this.age = age;
            this.dob = dob;
        }
    }

    private static Stream<Arguments> jsonBodySubstitutionSupplier() throws NoSuchMethodException {
        Class<JsonBodySubstitutionMethods> clazz = JsonBodySubstitutionMethods.class;
        Method jsonStringBody = clazz.getDeclaredMethod("applicationJsonStringBody",
            String.class, Callback.class);
        Method jsonPojoBody = clazz.getDeclaredMethod("applicationJsonPojoBody",
            Person.class, Callback.class);

        return Stream.of(
            Arguments.of(jsonStringBody, null, "application/json", null),
            Arguments.of(jsonStringBody, toObjectArray("{name:John Doe,age:40,dob:01-01-1980}"),
                "application/json",
                "\"{name:John Doe,age:40,dob:01-01-1980}\""),
            // Goal here is not to test the serializer extensively but to validate that expected code path
            // in HttpRequestMapper is hit.
            Arguments.of(jsonPojoBody, toObjectArray(
                new Person("John Doe", 40, OffsetDateTime.parse("1980-01-01T10:00:00Z"))),
                "application/json",
                "{\"name\":\"John Doe\",\"age\":40,\"dob\":\"1980-01-01T10:00:00Z\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("jsonBodySubstitutionSupplier")
    public void jsonBodySubstitution(Method method,
                                     Object[] arguments,
                                     String expectedBodyContentType,
                                     Object expectedBody) throws IOException {
        HttpRequestMapper mapper = new HttpRequestMapper("https://raw.host.com",
            method,
            new JacksonSerderAdapter());

        HttpRequest httpRequest = mapper.map(arguments);
        byte[] content = httpRequest.getBody();
        if (expectedBody == null) {
            Assertions.assertNull(content);
        } else {
            assertEquals(expectedBodyContentType, httpRequest.getHeaders().getValue("Content-Type"));
            assertEquals(expectedBody, new String(content, StandardCharsets.UTF_8));
        }
    }

    interface FormAndBodyMethods {
        @Get("test")
        void formAndBody(@FormParam("name") String param0,
                         @BodyParam("application/json") String param1,
                         Callback<Response<Void>> callback);
    }

    @Test
    public void formAndBody() throws NoSuchMethodException {
        Class<FormAndBodyMethods> clazz = FormAndBodyMethods.class;
        Method formAndBodyMethod = clazz.getDeclaredMethod("formAndBody", String.class, String.class, Callback.class);

        RuntimeException ex = null;
        try {
            new HttpRequestMapper("https://raw.host.com", formAndBodyMethod, new JacksonSerderAdapter());

        } catch (RuntimeException e) {
            ex = e;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex.getMessage().contains("'FormParam' and 'BodyParam' are mutually exclusive, but the method"));
    }

    interface StringByteNoContentTypeMethods {
        @Get("test0")
        void byteBody(@BodyParam(value = "") byte[] body, Callback<Response<Void>> callback);
        @Get("test1")
        void stringBody(@BodyParam(value = "") String body, Callback<Response<Void>> callback);
    }

    @Test
    public void stringByteNoContentType() throws NoSuchMethodException, IOException {
        Class<StringByteNoContentTypeMethods> clazz = StringByteNoContentTypeMethods.class;

        Method byteBodyMethod = clazz.getDeclaredMethod("byteBody", (new byte[0]).getClass(), Callback.class);
        Method stringBodyMethod = clazz.getDeclaredMethod("stringBody", String.class, Callback.class);
        HttpRequestMapper requestMapper = new HttpRequestMapper("https://raw.host.com",
            byteBodyMethod,
            new JacksonSerderAdapter());

        byte[] bytesBody = new byte[2];
        bytesBody[0] = 'A';
        bytesBody[1] = 'B';

        HttpRequest httpRequest = requestMapper.map(toObjectArray(bytesBody));
        assertEquals("application/octet-stream", httpRequest.getHeaders().getValue("Content-Type"));
        assertArrayEquals(bytesBody, httpRequest.getBody());

        requestMapper = new HttpRequestMapper("https://raw.host.com",
            stringBodyMethod,
            new JacksonSerderAdapter());
        httpRequest = requestMapper.map(toObjectArray("hello"));
        assertEquals("application/octet-stream", httpRequest.getHeaders().getValue("Content-Type"));
        assertEquals("hello", new String(httpRequest.getBody()));
    }

    private static Object[] toObjectArray(Object... objects) {
        return objects;
    }

    private static Map<String, String> createExpectedParameters(String sub1Value, boolean sub2Value) {
        Map<String, String> expectedParameters = new HashMap<>();
        if (sub1Value != null) {
            expectedParameters.put("sub1", sub1Value);
        }
        expectedParameters.put("sub2", String.valueOf(sub2Value));
        return expectedParameters;
    }
}
