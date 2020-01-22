package com.azure.android.core.implementation.util.serializer.threeten;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;

class OffsetDateTimeSerializer extends InstantSerializerBase<OffsetDateTime> {
    private static final long serialVersionUID = 1L;

    public static final OffsetDateTimeSerializer INSTANCE = new OffsetDateTimeSerializer();

    protected OffsetDateTimeSerializer() {
        super(OffsetDateTime.class,
            dt -> dt.toInstant().toEpochMilli(),
            dt -> dt.toEpochSecond(),
            dt -> dt.getNano(),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    protected OffsetDateTimeSerializer(OffsetDateTimeSerializer base,
                                       Boolean useTimestamp, DateTimeFormatter formatter) {
        this(base, useTimestamp, null, formatter);
    }

    protected OffsetDateTimeSerializer(OffsetDateTimeSerializer base,
                                       Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter formatter) {
        super(base, useTimestamp, useNanoseconds, formatter);
    }

    @Override
    protected ThreeTenFormattedSerializerBase<?> withFormat(Boolean useTimestamp,
                                                            DateTimeFormatter formatter, JsonFormat.Shape shape) {
        return new OffsetDateTimeSerializer(this, useTimestamp, formatter);
    }

    @Override
    protected ThreeTenFormattedSerializerBase<?> withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
        return new OffsetDateTimeSerializer(this, _useTimestamp, writeNanoseconds, _formatter);
    }
}
