package com.azure.core.http;

import java.io.IOException;

public class HttpPipelineNextPolicy {
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext context;
    private final HttpRequestSender sender;
    private int currentPolicyIndex;

    HttpPipelineNextPolicy(final HttpPipeline pipeline, HttpPipelineCallContext context, final HttpRequestSender sender) {
        this.pipeline = pipeline;
        this.context = context;
        this.sender = sender;
        this.currentPolicyIndex = -1;
    }

    public HttpResponse process() throws IOException {
        final int size = this.pipeline.getPolicyCount();
        if (this.currentPolicyIndex > size) {
            throw  new IllegalStateException("There is no more policies to execute.");
        }
        //
        this.currentPolicyIndex++;
        if (this.currentPolicyIndex == size) {
           return sender.send(this.context.httpRequest());
        } else {
            return this.pipeline.getPolicy(this.currentPolicyIndex).process(this.context, this);
        }
    }

    @Override
    public HttpPipelineNextPolicy clone() {
        HttpPipelineNextPolicy cloned = new HttpPipelineNextPolicy(this.pipeline, this.context, this.sender);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
