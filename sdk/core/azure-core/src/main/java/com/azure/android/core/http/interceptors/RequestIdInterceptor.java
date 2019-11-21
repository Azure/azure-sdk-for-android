// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptors;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The pipeline interceptor that puts a UUID in the request header. Azure uses the request ID as
 * the unique identifier for the request.
 */
public class RequestIdInterceptor implements Interceptor {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String requestId = request.header(REQUEST_ID_HEADER);

        if (requestId == null)
            request = request.newBuilder()
                .addHeader(REQUEST_ID_HEADER, UUID.randomUUID().toString())
                .build();

        return chain.proceed(request);
    }
}
