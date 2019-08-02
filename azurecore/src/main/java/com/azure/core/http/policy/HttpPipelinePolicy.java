package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineNextPolicy;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public interface HttpPipelinePolicy {
    Response process(Request request, HttpPipelineNextPolicy next) throws IOException;
}
