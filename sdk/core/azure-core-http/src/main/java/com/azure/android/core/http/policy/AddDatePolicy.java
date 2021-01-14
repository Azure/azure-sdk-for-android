// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The pipeline policy that adds a "Date" header in RFC 1123 format when sending an HTTP request.
 */
public class AddDatePolicy implements HttpPipelinePolicy {
    private final DateTimeFormatter format = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
        .withZone(ZoneId.of("UTC"))
        .withLocale(Locale.US);

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        httpRequest.getHeaders().put("Date", format.format(OffsetDateTime.now()));
        chain.processNextPolicy(httpRequest);
    }
}
