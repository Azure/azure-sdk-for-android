// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.util.UrlBuilder;
import com.azure.core.logging.ClientLogger;

import java.net.MalformedURLException;

/**
 * The pipeline policy that adds a given port to each {@link HttpRequest}.
 */
public class PortPolicy implements HttpPipelinePolicy {
    private final int port;
    private final boolean overwrite;
    private final ClientLogger logger = new ClientLogger(PortPolicy.class);

    /**
     * Creates a new PortPolicy object.
     *
     * @param port The port to set.
     * @param overwrite Whether or not to overwrite a {@link HttpRequest HttpRequest's} port if it already has one.
     */
    public PortPolicy(int port, boolean overwrite) {
        this.port = port;
        this.overwrite = overwrite;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        final UrlBuilder urlBuilder = UrlBuilder.parse(httpRequest.getUrl());
        if (overwrite || urlBuilder.getPort() == null) {
            logger.info("Changing port to {}", port);
            try {
                httpRequest.setUrl(urlBuilder.setPort(port).toUrl().toString());
            } catch (MalformedURLException error) {
                chain.completedError(error);
                return;
            }
            chain.processNextPolicy(httpRequest);
        }
    }
}
