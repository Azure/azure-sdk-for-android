// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;

import com.azure.android.core.util.DateTimeRfc1123;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

/**
 * The pipeline policy that adds a "Date" header in RFC 1123 format when sending an HTTP request.
 */
public class AddDatePolicy implements HttpPipelinePolicy {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
        .withZone(ZoneId.of("UTC"))
        .withLocale(Locale.US);

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();

        OffsetDateTime now = OffsetDateTime.now();
        try {
            httpRequest.getHeaders().put("Date", DateTimeRfc1123.toRfc1123String(now));
        } catch (IllegalArgumentException ignored) {
            httpRequest.getHeaders().put("Date", FORMATTER.format(now));
        }

        chain.processNextPolicy(httpRequest);
    }
}
