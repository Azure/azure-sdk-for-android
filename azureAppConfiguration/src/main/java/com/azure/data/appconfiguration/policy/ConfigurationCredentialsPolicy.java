package com.azure.data.appconfiguration.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;

import java.io.IOException;
import java.util.Map;

import okio.Buffer;

public class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    private final ConfigurationClientCredentials credentials;

    /**
     * Creates an instance that is able to apply a {@link ConfigurationClientCredentials} credential to a request in the pipeline.
     *
     * @param credentials the credential information to authenticate to Azure App Configuration service
     */
    public ConfigurationCredentialsPolicy(ConfigurationClientCredentials credentials)  {
        this.credentials = credentials;
    }

    @Override
    public HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) throws IOException {
        Buffer buffer = context.httpRequest().body();
        Map<String, String> authHeaders = credentials.getAuthorizationHeaders(context.httpRequest().url(), context.httpRequest().httpMethod(), buffer);
        for (Map.Entry<String, String> header : authHeaders.entrySet()) {
            context.httpRequest().header(header.getKey(), header.getValue());
        }
        HttpResponse response = next.process();
        response = response.buffer();
        response.bodyAsByteArray();
        return response;
    }
}
