// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import org.junit.jupiter.api.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateTimeSerializerTests {
    @Test
    public void toStringWithNull() {
        assertNull(DateTimeSerializer.toString(null));
    }

    @Test
    public void toStringOffsetDateTime() {
        // 2011-12-03T10:15:30+01:00
        assertEquals("0001-01-01T14:00:00Z", DateTimeSerializer.toString(OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-14))));
        assertEquals("10000-01-01T13:59:59.999Z", DateTimeSerializer.toString(OffsetDateTime.of(LocalDate.of(10000, 1, 1), LocalTime.parse("13:59:59.999"), ZoneOffset.UTC)));
        assertEquals("2010-01-01T12:34:56Z", DateTimeSerializer.toString(OffsetDateTime.of(2010, 1, 1, 12, 34, 56, 0, ZoneOffset.UTC)));
        assertEquals("0001-01-01T00:00:00Z", DateTimeSerializer.toString(OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
    }
}