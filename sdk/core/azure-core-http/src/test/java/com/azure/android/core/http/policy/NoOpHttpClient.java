// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.util.CancellationToken;

/**
 * An HttpClient instance that returns statusCode 200 with empty/no content.
 */
public class NoOpHttpClient implements HttpClient {

    @Override
    public HttpCallDispatcher getHttpCallDispatcher() {
        return new HttpCallDispatcher();
    }

    @Override
    public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
        httpCallback.onSuccess(new MockHttpResponse(httpRequest, 200));
    }
}