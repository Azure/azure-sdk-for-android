// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Wrapper over java.util.Date used for specifying RFC1123 formatted time.
 */
public final class DateTimeRfc1123 {

    private static final String RFC1123_DATE_TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * The actual Date object.
     */
    private final Date dateTime;

    /**
     * Creates a new DateTimeRfc1123 object with the specified DateTime.
     * @param dateTime The Date object to wrap.
     */
    public DateTimeRfc1123(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Creates a new DateTimeRfc1123 object with the specified DateTime.
     * @param formattedString The Date string in RFC1123 format
     */
    public DateTimeRfc1123(String formattedString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(RFC1123_DATE_TIME_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            this.dateTime = dateFormat.parse(formattedString);
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }

    /**
     * Returns the underlying Date.
     * @return The underlying Date.
     */
    public Date dateTime() {
        if (this.dateTime == null) {
            return null;
        }
        return this.dateTime;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(RFC1123_DATE_TIME_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(this.dateTime);
    }

    @Override
    public int hashCode() {
        return this.dateTime.hashCode();
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
        return this.dateTime.equals(rhs.dateTime());
    }
}
