package com.azure.android.core.internal.util.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SerializerProvider.class)
public class DateTimeSerializerTest {
    public static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("UTC")).withLocale(Locale.US);

    @Test
    public void test_getModule() {
        SimpleModule module = DateTimeSerializer.getModule();

        assertNotNull(module);
    }

    @Test
    public void serializeDateTime_asNumber() throws IOException {
        String testDate = "Tue, 25 Feb 2020 00:59:22 GMT";
        OffsetDateTime dateTime =
            OffsetDateTime.of(LocalDateTime.parse(testDate, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC);
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        DateTimeSerializer dateTimeSerializer = new DateTimeSerializer();
        SerializerProvider serializerProviderMock = PowerMockito.mock(SerializerProvider.class);
        PowerMockito.when(serializerProviderMock.isEnabled(any(SerializationFeature.class))).thenReturn(true);

        dateTimeSerializer.serialize(dateTime, jsonGenerator, serializerProviderMock);
        jsonGenerator.flush();

        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(serializerProviderMock).isEnabled(any(SerializationFeature.class));
        assertEquals("1582592362000", outputStream.toString());
    }

    @Test
    public void serializeDateTime_asString() throws IOException {
        String testDate = "Tue, 25 Feb 2020 00:59:22 GMT";
        OffsetDateTime dateTime =
            OffsetDateTime.of(LocalDateTime.parse(testDate, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC);
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        DateTimeSerializer dateTimeSerializer = new DateTimeSerializer();
        SerializerProvider serializerProviderMock = PowerMockito.mock(SerializerProvider.class);
        PowerMockito.when(serializerProviderMock.isEnabled(any(SerializationFeature.class))).thenReturn(false);

        dateTimeSerializer.serialize(dateTime, jsonGenerator, serializerProviderMock);
        jsonGenerator.flush();

        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(serializerProviderMock).isEnabled(any(SerializationFeature.class));
        assertEquals("\"2020-02-25T00:59:22Z\"", outputStream.toString());
    }

    @Test
    public void test_ToString() {
        OffsetDateTime dateTime =
            OffsetDateTime.of(LocalDateTime.parse("Tue, 25 Feb 2020 00:59:22 GMT", RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC);

        assertEquals("2020-02-25T00:59:22Z", DateTimeSerializer.toString(dateTime));
    }
}
