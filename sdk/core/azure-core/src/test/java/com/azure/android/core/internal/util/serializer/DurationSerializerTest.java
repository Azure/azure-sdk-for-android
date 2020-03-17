// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.Test;
import org.threeten.bp.Duration;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DurationSerializerTest {
    @Test
    public void test_getModule() {
        SimpleModule module = DurationSerializer.getModule();

        assertNotNull(module);
    }

    @Test
    public void serializeDuration() throws IOException {
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        DurationSerializer durationSerializer = new DurationSerializer();

        durationSerializer.serialize(Duration.ofDays(3), jsonGenerator, null);
        jsonGenerator.flush();

        assertEquals("\"P3D\"", outputStream.toString());
    }

    @Test
    public void duration_ofDays_toString() {
        assertEquals("P5D", DurationSerializer.toString(Duration.ofDays(5)));
    }

    @Test
    public void duration_ofHours_toString() {
        assertEquals("PT5H", DurationSerializer.toString(Duration.ofHours(5)));
    }

    @Test
    public void duration_ofMinutes_toString() {
        assertEquals("PT5M", DurationSerializer.toString(Duration.ofMinutes(5)));
    }

    @Test
    public void duration_ofSeconds_toString() {
        assertEquals("PT5S", DurationSerializer.toString(Duration.ofSeconds(5)));
    }

    @Test
    public void duration_ofMillis_toString() {
        assertEquals("PT0.005S", DurationSerializer.toString(Duration.ofMillis(5)));
    }

    @Test
    public void duration_ofManyMillis_toString() {
        assertEquals("P17DT8H41M54.373S", DurationSerializer.toString(Duration.ofMillis(1500114373)));
    }

    @Test
    public void duration_ofNanos_toString() {
        assertEquals("PT2.1S", DurationSerializer.toString(Duration.ofNanos(2100114373)));
    }

    @Test
    public void duration_zeroMillis_toString() {
        assertEquals("PT0S", DurationSerializer.toString(Duration.ofMillis(0)));
    }
}
