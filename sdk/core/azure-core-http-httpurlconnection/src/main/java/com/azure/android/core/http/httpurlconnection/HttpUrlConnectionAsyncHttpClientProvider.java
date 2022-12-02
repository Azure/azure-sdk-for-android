// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.httpurlconnection;

import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpClientProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on HttpUrlConnection.
 */
public class HttpUrlConnectionAsyncHttpClientProvider implements HttpClientProvider {
    @Override
    public HttpClient createInstance() {
        return new HttpUrlConnectionAsyncHttpClientBuilder().build();
    }
}