// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.azure.android.core.util.DateTimeRfc1123;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.module.SimpleModule;

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

public class DateTimeRfc1123SerializerTest {
    public static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("UTC")).withLocale(Locale.US);

    @Test
    public void test_getModule() {
        SimpleModule module = DateTimeRfc1123Serializer.getModule();

        assertNotNull(module);
    }

    @Test
    public void serializeDateTimeRfc1123() throws IOException {
        String testDate = "Tue, 25 Feb 2020 00:59:22 GMT";
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(testDate, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        DateTimeRfc1123Serializer dateTimeRfc1123Serializer = new DateTimeRfc1123Serializer();

        dateTimeRfc1123Serializer.serialize(dateTimeRfc1123, jsonGenerator, null);
        jsonGenerator.flush();

        assertEquals("\"" + testDate + "\"", outputStream.toString());
    }
}
