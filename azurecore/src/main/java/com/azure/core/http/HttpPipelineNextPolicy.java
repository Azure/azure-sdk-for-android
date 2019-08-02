package com.azure.core.http;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class HttpPipelineNextPolicy {
    private final HttpPipeline pipeline;
    private final HttpRequestSender sender;
    private int currentPolicyIndex;

    HttpPipelineNextPolicy(final HttpPipeline pipeline, final HttpRequestSender sender) {
        this.pipeline = pipeline;
        this.sender = sender;
        this.currentPolicyIndex = -1;
    }

    public Response process(Request request) throws IOException {
        final int size = this.pipeline.getPolicyCount();
        if (this.currentPolicyIndex > size) {
            throw  new IllegalStateException("There is no more policies to execute.");
        }
        //
        this.currentPolicyIndex++;
        if (this.currentPolicyIndex == size) {
           return sender.send(request);
        } else {
            return this.pipeline.getPolicy(this.currentPolicyIndex).process(request, this);
        }
    }

    @Override
    public HttpPipelineNextPolicy clone() {
        HttpPipelineNextPolicy cloned = new HttpPipelineNextPolicy(this.pipeline, this.sender);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
