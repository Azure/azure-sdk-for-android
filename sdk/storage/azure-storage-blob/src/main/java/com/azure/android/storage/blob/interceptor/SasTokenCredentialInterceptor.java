// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.interceptor;

import com.azure.android.core.util.CoreUtils;
import com.azure.android.storage.blob.credentials.SasTokenCredential;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *  Interceptor that append SAS token to the request Uri.
 */
public class SasTokenCredentialInterceptor implements Interceptor {
    private final SasTokenCredential credential;

    public SasTokenCredentialInterceptor(SasTokenCredential credential) {
        this.credential = credential;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl requestURL = chain.request().url();

        String encodedQuery = requestURL.encodedQuery();
        if (!CoreUtils.isNullOrEmpty(encodedQuery)) {
            encodedQuery += "&";
        }

        String sasToken = this.credential.getSasToken();
        // SAS token is already encoded so its safe to append it to the encoded query from source request.
        encodedQuery += sasToken.startsWith("?")
            ? sasToken.substring(1)
            : sasToken;

        HttpUrl newURL = requestURL
            .newBuilder()
            .query(encodedQuery)
            .build();

        Request newRequest = chain.request()
                .newBuilder()
                .url(newURL)
                .build();
        return chain.proceed(newRequest);
    }
}
