// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.util.UrlBuilder;
import com.azure.android.core.logging.ClientLogger;

/**
 * The pipeline policy that adds a given protocol to each HttpRequest.
 */
public class ProtocolPolicy implements HttpPipelinePolicy {
    private final String protocol;
    private final boolean overwrite;
    private final ClientLogger logger = new ClientLogger(ProtocolPolicy.class);

    /**
     * Creates a new ProtocolPolicy.
     *
     * @param protocol The protocol to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's protocol if it already has one.
     */
    public ProtocolPolicy(String protocol, boolean overwrite) {
        this.protocol = protocol;
        this.overwrite = overwrite;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        final UrlBuilder urlBuilder = UrlBuilder.parse(httpRequest.getUrl());
        if (overwrite || urlBuilder.getScheme() == null) {
            logger.info("Setting protocol to {}", protocol);

            try {
                httpRequest.setUrl(urlBuilder.setScheme(protocol).toString());
            } catch (IllegalArgumentException error) {
                chain.completedError(error);
                return;
            }
            chain.processNextPolicy(httpRequest);
        } else {
            chain.processNextPolicy(httpRequest);
        }
    }
}