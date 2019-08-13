package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;

import java.io.IOException;

public interface HttpPipelinePolicy {
    HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) throws IOException;
}
