package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;

import java.io.IOException;

/**
 * The Pipeline policy that adds a particular set of headers to HTTP requests.
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
    public HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) throws IOException {
        for (HttpHeader header : headers) {
            context.httpRequest().header(header.name(), header.value());
        }
        return next.process();
    }
}
