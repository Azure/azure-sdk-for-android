// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeRfc1123Test {
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("UTC")).withLocale(Locale.US);
    private static final String TEST_DATE = "Tue, 25 Feb 2020 00:59:22 GMT";

    @Test
    public void constructor_withOffsetDateTime() {
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));

        assertEquals(TEST_DATE, dateTimeRfc1123.getDateTime().format(RFC1123_DATE_TIME_FORMATTER));
    }

    @Test
    public void constructor_withString() {
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(TEST_DATE);

        assertEquals(TEST_DATE, dateTimeRfc1123.getDateTime().format(RFC1123_DATE_TIME_FORMATTER));
    }

    @Test
    public void convertToString() {
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));

        assertEquals(TEST_DATE, dateTimeRfc1123.toString());
    }

    @Test
    public void equalTo_identicalObject() {
        final DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));
        final DateTimeRfc1123 otherDateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));

        assertEquals(dateTimeRfc1123, otherDateTimeRfc1123);
    }

    @Test
    public void notEqualTo_differentObject() {
        final DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));
        final DateTimeRfc1123 otherDateTimeRfc1123 = new DateTimeRfc1123(OffsetDateTime.now());

        assertNotEquals(dateTimeRfc1123, otherDateTimeRfc1123);
    }

    @Test
    public void notEqualTo_nullObject() {
        final DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(OffsetDateTime.now());

        assertNotNull(dateTimeRfc1123);
    }

    @Test
    public void notEqualTo_objectFromDifferentClass() {
        final DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(OffsetDateTime.now());

        assertNotEquals("", dateTimeRfc1123);
    }

    @Test
    public void getHashCode() {
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));
        DateTimeRfc1123 otherDateTimeRfc1123 = new DateTimeRfc1123(
            OffsetDateTime.of(LocalDateTime.parse(TEST_DATE, RFC1123_DATE_TIME_FORMATTER), ZoneOffset.UTC));

        assertEquals(dateTimeRfc1123, otherDateTimeRfc1123);
        assertEquals(dateTimeRfc1123.hashCode(), otherDateTimeRfc1123.hashCode());
    }
}
