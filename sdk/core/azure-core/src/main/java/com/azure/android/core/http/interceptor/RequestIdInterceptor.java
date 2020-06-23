// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.HttpHeader;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Pipeline interceptor that puts a UUID in the request header. Azure uses the request ID as the unique identifier for
 * the request.
 */
public class RequestIdInterceptor implements Interceptor {
    /**
     * Intercept the current request in the pipeline and apply the "x-ms-client-request-id" header.

     * @param chain Provide access to the request to apply the "x-ms-client-request-id" header.
     *
     * @return Response from the next interceptor in the pipeline.
     * @throws IOException If an IO error occurs while processing the request and response.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String requestId = request.header(HttpHeader.CLIENT_REQUEST_ID);

        if (requestId == null) {
            request = request.newBuilder()
                .header(HttpHeader.CLIENT_REQUEST_ID, UUID.randomUUID().toString())
                .build();
        }

        return chain.proceed(request);
    }
}
