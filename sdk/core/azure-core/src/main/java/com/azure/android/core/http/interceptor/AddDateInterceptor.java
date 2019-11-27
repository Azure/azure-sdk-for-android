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
 * The pipeline interceptor that adds a "Date" header in RFC 1123 format when sending an HTTP request.
 */
public class AddDateInterceptor implements Interceptor {
    private static final String DATE_HEADER = "Date";
    private static final DateTimeFormatter format = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
        .withZone(ZoneId.of("UTC"))
        .withLocale(Locale.US);

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return chain.proceed(chain.request()
            .newBuilder()
            .header(DATE_HEADER, format.format(OffsetDateTime.now()))
            .build());
    }
}
