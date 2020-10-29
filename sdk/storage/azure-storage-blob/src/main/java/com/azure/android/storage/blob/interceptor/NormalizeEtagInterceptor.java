// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.HttpHeader;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Pipeline interceptor that wraps any potential error responses from the service and applies post processing
 * of the response's ETag header to standardize the value.
 */
public class NormalizeEtagInterceptor implements Interceptor {
    /**
     * Intercept the service returned ETag value and normalize it if required.
     *
     * <p>
     * The service is inconsistent in whether or not the ETag header value has quotes. This method will check if the
     * response returns an ETag value, and if it does, remove any quotes that may be present to give the user a more
     * predictable format to work with.
     *
     * @param chain Provide access to the response containing ETag header to normalize.
     *
     * @return Response From the next interceptor in the pipeline.
     * @throws IOException If an IO error occurs while processing the request and response.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        String eTag = response.header(HttpHeader.ETAG);

        if (eTag == null) {
            return response;
        }

        eTag = eTag.replace("\"", "");

        return response.newBuilder()
            .header(HttpHeader.ETAG, eTag)
            .build();
    }
}
