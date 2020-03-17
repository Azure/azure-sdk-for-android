// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeSerializerTest {
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("UTC")).withLocale(Locale.US);

    @Test
    public void test_getModule() {
        SimpleModule module = DateTimeSerializer.getModule();

        assertNotNull(module);
    }

    @Test
    public void serializeDateTime_asNumber() throws IOException {
        // DateTime
        String testDate = "Tue, 25 Feb 2020 00:59:22 GMT";
        OffsetDateTime dateTime =
            OffsetDateTime.of(LocalDateTime.parse(testDate, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC);

        // JsonGenerator and target OutputStream
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);

        // SerializerProvider
        BaseSettings baseSettings = new BaseSettings(null, new JacksonAnnotationIntrospector(), null,
            TypeFactory.defaultInstance(), null, StdDateFormat.instance, null, Locale.getDefault(), null,
            Base64Variants.getDefaultVariant());
        SerializationConfig serializationConfig = new SerializationConfig(
            baseSettings.withClassIntrospector(new BasicClassIntrospector()), new StdSubtypeResolver(),
            new SimpleMixInResolver(null), new RootNameLookup(), new ConfigOverrides());
        SerializerProvider serializerProviderStub =
            new SerializerProviderStub(new SerializerProviderStub(), serializationConfig, null);

        // Actual serialization
        new DateTimeSerializer().serialize(dateTime, jsonGenerator, serializerProviderStub);
        jsonGenerator.flush();

        assertEquals("1582592362000", outputStream.toString());
    }

    @Test
    public void serializeDateTime_asString() throws IOException {
        //DateTime
        String testDate = "Tue, 25 Feb 2020 00:59:22 GMT";
        OffsetDateTime dateTime =
            OffsetDateTime.of(LocalDateTime.parse(testDate, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC);

        // JsonGenerator and target OutputStream
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);

        // SerializerProvider
        BaseSettings baseSettings = new BaseSettings(null, new JacksonAnnotationIntrospector(), null,
            TypeFactory.defaultInstance(), null, StdDateFormat.instance, null, Locale.getDefault(), null,
            Base64Variants.getDefaultVariant());
        SerializationConfig serializationConfig = new SerializationConfig(
            baseSettings.withClassIntrospector(new BasicClassIntrospector()), new StdSubtypeResolver(),
            new SimpleMixInResolver(null), new RootNameLookup(), new ConfigOverrides());
        serializationConfig = serializationConfig.without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);SerializerProvider serializerProviderStub =
            new SerializerProviderStub(new SerializerProviderStub(), serializationConfig, null);

        // Actual serialization
        new DateTimeSerializer().serialize(dateTime, jsonGenerator, serializerProviderStub);
        jsonGenerator.flush();

        assertEquals("\"2020-02-25T00:59:22Z\"", outputStream.toString());
    }

    @Test
    public void test_ToString() {
        OffsetDateTime dateTime =
            OffsetDateTime.of(LocalDateTime.parse("Tue, 25 Feb 2020 00:59:22 GMT", RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC);

        assertEquals("2020-02-25T00:59:22Z", DateTimeSerializer.toString(dateTime));
    }

    private static class SerializerProviderStub extends SerializerProvider {
        SerializerProviderStub() {
            super();
        }

        SerializerProviderStub(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
            super(src, config, f);
        }

        @Override
        public WritableObjectId findObjectId(Object forPojo, ObjectIdGenerator<?> generatorType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> serializerInstance(Annotated annotated, Object serDef) throws JsonMappingException {
            return null;
        }

        @Override
        public Object includeFilterInstance(BeanPropertyDefinition forProperty, Class<?> filterClass) throws JsonMappingException {
            return null;
        }

        @Override
        public boolean includeFilterSuppressNulls(Object filter) throws JsonMappingException {
            return false;
        }
    }
}
