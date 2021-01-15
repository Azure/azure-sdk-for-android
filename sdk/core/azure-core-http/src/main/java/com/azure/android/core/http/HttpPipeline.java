// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import com.azure.android.core.micro.util.CancellationToken;
import com.azure.android.core.micro.util.Context;

import java.util.List;
import java.util.Objects;

/**
 * The HTTP pipeline that HTTP requests and corresponding responses will flow through.
 * <p>
 * The HTTP pipeline may apply a set of {@link HttpPipelinePolicy HttpPipelinePolicies} to the request before it is
 * sent and on the response as it is being returned.
 *
 * @see HttpPipelinePolicy
 */
public final class HttpPipeline {
    private final HttpClient httpClient;
    private final HttpPipelinePolicy[] pipelinePolicies;
    // package-private final vars
    final HttpPipelinePolicy networkPolicy;
    final HttpCallDispatcher httpCallDispatcher;
    final int size;

    /**
     * Creates a HttpPipeline holding array of policies that gets applied to all request initiated through {@link
     * HttpPipeline#send(HttpRequest, Context, CancellationToken, HttpCallback)} and it's response.
     *
     * @param httpClient the http client to write request to wire and receive response from wire.
     * @param pipelinePolicies pipeline policies in the order they need to applied, a copy of this array will be made
     * hence changing the original array after the creation of pipeline will not  mutate the pipeline
     */
    HttpPipeline(HttpClient httpClient, List<HttpPipelinePolicy> pipelinePolicies) {
        Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        Objects.requireNonNull(pipelinePolicies, "'pipelinePolicies' cannot be null.");
        this.httpClient = httpClient;
        this.httpCallDispatcher = this.httpClient.getHttpCallDispatcher();
        this.pipelinePolicies = pipelinePolicies.toArray(new HttpPipelinePolicy[0]);
        this.size = this.pipelinePolicies.length;
        this.networkPolicy = new HttpPipelinePolicy() {
            @Override
            public void process(HttpPipelinePolicyChain chain) {
                // Indicate the HttpClient to prefer the calling thread for sending request.
                chain.getRequest().getTags().put("prefer-running-http-in-calling-thread", null);

                HttpPipeline.this.httpClient.send(chain.getRequest(), chain.getCancellationToken(),
                    new HttpCallback() {
                        @Override
                        public void onSuccess(HttpResponse response) {
                            chain.onCompleted(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                            chain.onCompleted(error);
                        }
                    });
            }
        };
    }

    /**
     * Execute an HTTP call by sending the {@code request} through the HTTP pipeline.
     *
     * @param httpRequest The HTTP request to send.
     * @param context The context that is passed through the HTTP pipeline.
     *     The pipeline policies may inspect the context for any settings specific to the policy.
     *     The policies that want to make Azure SDK API calls may have to provide this context
     *     or a context derived from it (via {@link Context#addData(Object, Object)}) to the API.
     * @param cancellationToken The cancellation token for the HTTP call, on which the caller
     *     may request cancellation of this HTTP call execution. Note that cancellation is best
     *     effort hence not guaranteed.
     * @param httpCallback The HTTP callback to notify the result of the HTTP call.
     */
    public void send(HttpRequest httpRequest,
                     Context context,
                     CancellationToken cancellationToken,
                     HttpCallback httpCallback) {
        HttpPipelinePolicyChainImpl.beginPipelineExecution(this,
            httpRequest,
            context,
            cancellationToken,
            httpCallback);
    }

    /**
     * Get the policy at the provided index in the pipeline.
     *
     * @param index index of the the policy to retrieve.
     * @return the policy stored at that index.
     */
    public HttpPipelinePolicy getPolicy(final int index) {
        return this.pipelinePolicies[index];
    }

    /**
     * Get the {@link HttpClient} associated with the pipeline.
     *
     * @return the {@link HttpClient} associated with the pipeline
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }
}
