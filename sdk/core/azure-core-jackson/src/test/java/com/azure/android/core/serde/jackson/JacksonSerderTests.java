// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonSerderTests {
    @Test
    public void emptyMap() throws IOException {
        final Map<String, String> map = new HashMap<>();
        final JacksonSerder serializer = new JacksonSerder();
        assertEquals("{}", serializer.serialize(map, SerdeEncoding.JSON));
    }

    @Test
    public void mapWithNullKey() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put(null, null);
        final JacksonSerder serializer = new JacksonSerder();
        assertEquals("{}", serializer.serialize(map, SerdeEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        final JacksonSerder serializer = new JacksonSerder();
        assertEquals("{\"map\":{\"\":null}}", serializer.serialize(mapHolder, SerdeEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");
        final JacksonSerder serializer = new JacksonSerder();
        assertEquals("{\"map\":{\"\":\"\"}}", serializer.serialize(mapHolder, SerdeEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put("", "test");
        final JacksonSerder serializer = new JacksonSerder();
        assertEquals("{\"\":\"test\"}", serializer.serialize(map, SerdeEncoding.JSON));
    }

    private static class MapHolder {
        @JsonInclude(content = JsonInclude.Include.ALWAYS)
        private Map<String, String> map = new HashMap<>();

        public Map<String, String> map() {
            return map;
        }

        public void map(Map<String, String> map) {
            this.map = map;
        }
    }

    @JacksonXmlRootElement(localName = "XmlString")
    private static class XmlString {
        @JsonProperty("Value")
        private String value;

        public String getValue() {
            return value;
        }
    }

    @ParameterizedTest
    @MethodSource("deserializeJsonSupplier")
    public void deserializeJson(String json, OffsetDateTime expected) throws IOException {
        DateTimeWrapper wrapper = JacksonSerder.createDefaultSerdeAdapter()
            .deserialize(json, DateTimeWrapper.class, SerdeEncoding.JSON);

        assertEquals(expected, wrapper.getOffsetDateTime());
    }

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormat = "{\"OffsetDateTime\":\"%s\"}";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
            Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00"), minValue),
            Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00"), unixEpoch),
            Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00Z"), unixEpoch)
        );
    }

    @ParameterizedTest
    @MethodSource("deserializeXmlSupplier")
    public void deserializeXml(String xml, OffsetDateTime expected) throws IOException {
        DateTimeWrapper wrapper = JacksonSerder.createDefaultSerdeAdapter()
            .deserialize(xml, DateTimeWrapper.class, SerdeEncoding.XML);

        assertEquals(expected, wrapper.getOffsetDateTime());
    }

    private static Stream<Arguments> deserializeXmlSupplier() {
        final String xmlFormat = "<Wrapper><OffsetDateTime>%s</OffsetDateTime></Wrapper>";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
            Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00"), minValue),
            Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00"), unixEpoch),
            Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00Z"), unixEpoch)
        );
    }

    @JacksonXmlRootElement(localName = "Wrapper")
    private static class DateTimeWrapper {
        @JsonProperty(value = "OffsetDateTime", required = true)
        private OffsetDateTime offsetDateTime;

        public DateTimeWrapper setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;
            return this;
        }

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }
    }
}
