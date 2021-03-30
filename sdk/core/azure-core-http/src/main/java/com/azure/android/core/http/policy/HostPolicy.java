// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.util.UrlBuilder;

/**
 * The pipeline policy that adds the given host to each HttpRequest.
 */
public class HostPolicy implements HttpPipelinePolicy {
    private final String host;

    /**
     * Create HostPolicy.
     *
     * @param host The host to set on every HttpRequest.
     */
    public HostPolicy(String host) {
        this.host = host;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        final UrlBuilder urlBuilder = UrlBuilder.parse(httpRequest.getUrl());
        try {
            httpRequest.setUrl(urlBuilder.setHost(host).toString());
        } catch (IllegalArgumentException error) {
            chain.completedError(error);
            return;
        }
        chain.processNextPolicy(httpRequest);
    }
}

