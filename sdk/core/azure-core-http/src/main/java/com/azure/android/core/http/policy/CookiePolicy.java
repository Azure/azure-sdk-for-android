// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The pipeline policy that which stores cookies based on the response "Set-Cookie" header and adds cookies to requests.
 */
public class CookiePolicy implements HttpPipelinePolicy {
    private final CookieHandler cookies = new CookieManager();

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();

        final URI uri;
        try {
            uri = httpRequest.getUrl().toURI();
        } catch (URISyntaxException error) {
            chain.finishedProcessing(error);
            return;
        }

        Map<String, List<String>> cookieHeaders = new HashMap<>();
        for (HttpHeader header : httpRequest.getHeaders()) {
            cookieHeaders.put(header.getName(), Arrays.asList(chain.getRequest().getHeaders()
                .getValues(header.getName())));
        }

        final Map<String, List<String>> requestCookies;
        try {
            requestCookies = cookies.get(uri, cookieHeaders);
        } catch (IOException error) {
            chain.finishedProcessing(error);
            return;
        }

        for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
            httpRequest.getHeaders().put(entry.getKey(), String.join(",", entry.getValue()));
        }

        chain.processNextPolicy(httpRequest, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                Map<String, List<String>> responseHeaders = new HashMap<>();
                for (HttpHeader header : response.getHeaders()) {
                    responseHeaders.put(header.getName(), Collections.singletonList(header.getValue()));
                }

                try {
                    cookies.put(uri, responseHeaders);
                } catch (IOException error) {
                    chain.finishedProcessing(error);
                    return;
                }
                chain.finishedProcessing(response);
            }

            @Override
            public void onError(Throwable error) {
                chain.finishedProcessing(error);
            }
        });
    }
}
