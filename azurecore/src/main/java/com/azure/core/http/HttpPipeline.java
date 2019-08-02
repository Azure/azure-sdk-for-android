package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Request;
import okhttp3.Response;

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

    Response send(Request request, HttpRequestSender sender) throws IOException {
        HttpPipelineNextPolicy next = new HttpPipelineNextPolicy(this, sender);
        return next.process(request);
    }
}
