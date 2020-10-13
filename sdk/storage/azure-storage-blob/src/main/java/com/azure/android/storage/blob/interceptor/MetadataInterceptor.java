// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.interceptor;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Pipeline interceptor that properly serializes the x-ms-meta- collection headers.
 */
public class MetadataInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        StorageMultiHeaders multiHeaders
            = chain.request().tag(StorageMultiHeaders.class);
        if (multiHeaders != null) {
            Request.Builder requestBuilder = chain.request().newBuilder();
            Map<String, String> headers = multiHeaders.getHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader("x-ms-meta-" + entry.getKey(), entry.getValue());
            }
            return chain.proceed(requestBuilder.build());
        } else {
            return chain.proceed(chain.request());
        }
    }

    /**
     * A thin wrapper class whose main purpose is to give us a unique type to tag in the Services method signatures.
     */
    public static class StorageMultiHeaders {
        private Map<String, String> headers;

        public StorageMultiHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }
    }
}
