// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.implementation.Util;

import java.util.UUID;

/**
 * The pipeline policy that puts a UUID in the request header. Azure uses the request id as
 * the unique identifier for the request.
 */
public class RequestIdPolicy implements HttpPipelinePolicy {

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final String requestIdHeaderName;

    /**
     * Creates  {@link RequestIdPolicy} with provided {@code requestIdHeaderName}.
     * @param requestIdHeaderName to be used to set in {@link HttpRequest}.
     */
    public RequestIdPolicy(String requestIdHeaderName) {
        this.requestIdHeaderName = Util.requireNonNull(requestIdHeaderName,
            "'requestIdHeaderName' cannot be null.");
    }

    /**
     * Creates default {@link RequestIdPolicy} with default header name 'x-ms-client-request-id'.
     */
    public RequestIdPolicy() {
        requestIdHeaderName = REQUEST_ID_HEADER;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        String requestId = httpRequest.getHeaders().getValue(requestIdHeaderName);
        if (requestId == null) {
            httpRequest.getHeaders().put(requestIdHeaderName, UUID.randomUUID().toString());
        }
        chain.processNextPolicy(httpRequest);
    }
}

