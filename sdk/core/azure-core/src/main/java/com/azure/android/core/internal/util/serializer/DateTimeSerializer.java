// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link OffsetDateTime} object into ISO8601 formats.
 */
final class DateTimeSerializer extends JsonSerializer<OffsetDateTime> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson {@link ObjectMapper}.
     *
     * @return A simple module to be plugged onto Jackson {@link ObjectMapper}.
     */
    static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(OffsetDateTime.class, new DateTimeSerializer());

        return module;
    }

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException {
        if (provider.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
            jsonGenerator.writeNumber(value.toInstant().toEpochMilli());
        } else {
            jsonGenerator.writeString(toString(value));
        }
    }

    /**
     * Convert the provided {@link OffsetDateTime} to its String representation.
     *
     * @param offsetDateTime The {@link OffsetDateTime} to convert.
     * @return The string representation of the provided {@code offsetDateTime}.
     */
    public static String toString(OffsetDateTime offsetDateTime) {
        String result = null;

        if (offsetDateTime != null) {
            offsetDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
            result = DateTimeFormatter.ISO_INSTANT.format(offsetDateTime);

            if (result.startsWith("+")) {
                result = result.substring(1);
            }
        }

        return result;
    }
}
