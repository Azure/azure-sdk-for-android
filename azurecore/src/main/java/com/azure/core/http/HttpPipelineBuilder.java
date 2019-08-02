package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpPipelineBuilder {
    private List<HttpPipelinePolicy> pipelinePolicies;


    public HttpPipelineBuilder() {
    }

    public HttpPipeline build() {
        List<HttpPipelinePolicy> policies = (pipelinePolicies == null) ? new ArrayList<>() : pipelinePolicies;
        return new HttpPipeline(policies);
    }

    public HttpPipelineBuilder policies(HttpPipelinePolicy... policies) {
        if (pipelinePolicies == null) {
            pipelinePolicies = new ArrayList<>();
        }
        this.pipelinePolicies.addAll(Arrays.asList(policies));
        return this;
    }
}
