package com.azure.android.storage.common.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor that wraps any potential error responses from the service and applies post processing of the response's
 * ETag header to standardize the value.
 */
public class NormalizeEtagInterceptor implements Interceptor {
    private static final String ETAG = "ETag";

    /**
     * The service is inconsistent in whether or not the ETag header value has quotes. This method will check if the
     * response returns an ETag value, and if it does, remove any quotes that may be present to give the user a more
     * predictable format to work with.
     *
     * @return An updated response with post processing steps applied.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        String eTag = response.header(ETAG);

        if (eTag == null) {
            return response;
        }

        eTag = eTag.replace("\"", "");

        return response.newBuilder()
            .header(ETAG, eTag)
            .build();
    }
}
