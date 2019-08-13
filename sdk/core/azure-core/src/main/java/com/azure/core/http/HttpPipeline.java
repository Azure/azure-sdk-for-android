package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class HttpPipeline {
    private final HttpPipelinePolicy[] pipelinePolicies;

    HttpPipeline(List<HttpPipelinePolicy> pipelinePolicies) {
        Objects.requireNonNull(pipelinePolicies);
        this.pipelinePolicies = pipelinePolicies.toArray(new HttpPipelinePolicy[0]);
    }

    public HttpPipelinePolicy getPolicy(final int index) {
        return this.pipelinePolicies[index];
    }

    public int getPolicyCount() {
        return this.pipelinePolicies.length;
    }

    HttpResponse send(HttpPipelineCallContext context, HttpRequestSender sender) throws IOException {
        HttpPipelineNextPolicy next = new HttpPipelineNextPolicy(this, context, sender);
        return next.process();
    }
}
