package com.azure.android.storage.blob.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.HttpHeader;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor that wraps any potential error responses from the service and applies post processing of the response's
 * ETag header to standardize the value.
 */
public class NormalizeEtagInterceptor implements Interceptor {
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
