// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.threeten.bp.Duration;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link Duration} object into ISO8601 formats.
 */
final class DurationSerializer extends JsonSerializer<Duration> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson {@link ObjectMapper}.
     *
     * @return A simple module to be plugged onto Jackson {@link ObjectMapper}.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Duration.class, new DurationSerializer());

        return module;
    }

    @Override
    public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
        jsonGenerator.writeString(DurationSerializer.toString(duration));
    }

    /**
     * Convert to provided {@link Duration} to an ISO 8601 String with a days component.
     *
     * @param duration The {@link Duration} to convert.
     * @return The string representation of the provided {@link Duration}.
     */
    public static String toString(Duration duration) {
        String result = null;

        if (duration != null) {
            if (duration.get(ChronoUnit.MILLIS) == 0) {
                result = "PT0S";
            } else {
                final StringBuilder builder = new StringBuilder();

                builder.append('P');

                final long days = duration.get(ChronoUnit.DAYS);

                if (days > 0) {
                    builder.append(days);
                    builder.append('D');

                    duration = duration.minusDays(days);
                }

                final long hours = duration.get(ChronoUnit.HOURS);

                if (hours > 0) {
                    builder.append('T');
                    builder.append(hours);
                    builder.append('H');

                    duration = duration.minusHours(hours);
                }

                final long minutes = duration.get(ChronoUnit.MINUTES);

                if (minutes > 0) {
                    if (hours == 0) {
                        builder.append('T');
                    }

                    builder.append(minutes);
                    builder.append('M');

                    duration = duration.minusMinutes(minutes);
                }

                final long seconds = duration.get(ChronoUnit.SECONDS);

                if (seconds > 0) {
                    if (hours == 0 && minutes == 0) {
                        builder.append('T');
                    }

                    builder.append(seconds);

                    duration = duration.minusSeconds(seconds);
                }

                long milliseconds = duration.get(ChronoUnit.MILLIS);

                if (milliseconds > 0) {
                    if (hours == 0 && minutes == 0 && seconds == 0) {
                        builder.append("T");
                    }

                    if (seconds == 0) {
                        builder.append("0");
                    }

                    builder.append('.');

                    if (milliseconds <= 99) {
                        builder.append('0');

                        if (milliseconds <= 9) {
                            builder.append('0');
                        }
                    }

                    // Remove trailing zeros.
                    while (milliseconds % 10 == 0) {
                        milliseconds /= 10;
                    }

                    builder.append(milliseconds);
                }

                if (seconds > 0 || milliseconds > 0) {
                    builder.append('S');
                }

                result = builder.toString();
            }
        }

        return result;
    }
}
