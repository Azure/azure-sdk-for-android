// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.micro.util.Context;

/**
 * The pipeline policy that adds a particular set of headers to HTTP requests.
 */
public class AddHeadersPolicy implements HttpPipelinePolicy {
    private final HttpHeaders headers;

    /**
     * Creates a AddHeadersPolicy.
     *
     * @param headers The headers to add to outgoing requests.
     */
    public AddHeadersPolicy(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain, Context context) {
        HttpRequest httpRequest = chain.getRequest();
        for (HttpHeader header : headers) {
            httpRequest.setHeader(header.getName(), header.getValue());
        }
        chain.processNextPolicy(httpRequest);
    }
}
