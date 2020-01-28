// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Pipeline interceptor that adds a "Date" header with the current date and time in RFC 1123 format when sending an
 * HTTP request.
 */
public class AddDateInterceptor implements Interceptor {
    private static final String DATE_HEADER = "Date";
    private static final DateTimeFormatter httpDateTimeFormatter = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
        .withZone(ZoneId.of("UTC"))
        .withLocale(Locale.US);

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return chain.proceed(chain.request()
            .newBuilder()
            .header(DATE_HEADER, httpDateTimeFormatter.format(OffsetDateTime.now()))
            .build());
    }
}
