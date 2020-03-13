// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

/**
 * Wrapper over java.time.OffsetDateTime used for specifying RFC1123 format during serialization and deserialization.
 */
public final class DateTimeRfc1123 {
    /**
     * The pattern of the DateTime used for RFC1123 datetime format.
     */
    private static final String RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    /**
     * The formatter used for RFC1123 datetime format.
     */
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern(RFC1123_FORMAT).withZone(ZoneId.of("UTC")).withLocale(Locale.US);

    /**
     * The actual DateTime object.
     */
    private final OffsetDateTime dateTime;

    /**
     * Creates a new {@link DateTimeRfc1123} object with the specified DateTime.
     *
     * @param dateTime The DateTime object to wrap.
     */
    public DateTimeRfc1123(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Creates a new {@link DateTimeRfc1123} object with the specified DateTime.
     *
     * @param formattedString The DateTime string in RFC1123 format.
     */
    public DateTimeRfc1123(String formattedString) {
        dateTime = OffsetDateTime.parse(formattedString, DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    /**
     * Returns the underlying DateTime.
     *
     * @return The underlying DateTime.
     */
    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return RFC1123_DATE_TIME_FORMATTER.format(dateTime);
    }

    @Override
    public int hashCode() {
        return dateTime.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof DateTimeRfc1123)) {
            return false;
        }

        DateTimeRfc1123 rhs = (DateTimeRfc1123) obj;

        return dateTime.equals(rhs.getDateTime());
    }
}
