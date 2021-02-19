// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQueries;

import java.io.IOException;

/**
 * Custom deserializer that handles converting ISO8601 dates into {@link OffsetDateTime} objects.
 */
class DateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addDeserializer(OffsetDateTime.class, new DateTimeDeserializer());
    }

    /**
     * Gets a module wrapping this deserializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A {@link SimpleModule} to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        TemporalAccessor temporal = DateTimeFormatter.ISO_DATE_TIME
            .parseBest(parser.getValueAsString(), OffsetDateTime::from, LocalDateTime::from);

        if (temporal.query(TemporalQueries.offset()) == null) {
            return LocalDateTime.from(temporal).atOffset(ZoneOffset.UTC);
        } else {
            return OffsetDateTime.from(temporal);
        }
    }
}
