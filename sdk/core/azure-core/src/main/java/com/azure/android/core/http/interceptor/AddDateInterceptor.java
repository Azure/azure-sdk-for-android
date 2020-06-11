// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.util.DateTimeRfc1123;

import org.threeten.bp.OffsetDateTime;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Pipeline interceptor that adds a "Date" header with the current date and time in RFC 1123 format when sending an
 * HTTP request.
 */
public class AddDateInterceptor implements Interceptor {
    /**
     * Intercept the current request in the pipeline and apply the HTTP "Date" header.
     *
     * @param chain Provide access to the request to apply the HTTP Date header.
     *
     * @return Response from the next interceptor in the pipeline.
     * @throws IOException If an IO error occurs while processing the request and response.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return chain.proceed(chain.request()
            .newBuilder()
            .header(HttpHeader.DATE, new DateTimeRfc1123(OffsetDateTime.now()).toString())
            .build());
    }
}
