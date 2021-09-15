// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImplBuilder;
import com.azure.android.communication.common.CommunicationAccessToken;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.policy.CookiePolicy;
import com.azure.android.core.http.policy.HttpLogOptions;
import com.azure.android.core.http.policy.HttpLoggingPolicy;
import com.azure.android.core.http.policy.RetryPolicy;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.annotation.ServiceClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

/**
 * Builder for creating clients of Azure Communication Service Chat
 */
@ServiceClientBuilder(serviceClients = {ChatThreadAsyncClient.class, ChatThreadClient.class})
public final class ChatThreadClientBuilder {
    private final ClientLogger logger = new ClientLogger(ChatThreadClientBuilder.class);

    private String chatThreadId;
    private String endpoint;
    private HttpClient httpClient;
    private CommunicationTokenCredential communicationTokenCredential;
    private RetryPolicy retryPolicy;
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private HttpPipeline httpPipeline;
    private ChatServiceVersion serviceVersion;

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @throws NullPointerException if endpoint is null.
     * @return The updated {@link ChatThreadClientBuilder} object
     */
    public ChatThreadClientBuilder endpoint(String endpoint) {
        if (endpoint == null) {
            throw logger.logExceptionAsError(new NullPointerException("endpoint is required."));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set HttpClient to use
     *
     * @param httpClient HttpClient to use
     * @throws NullPointerException if httpClient is null.
     * @return The updated {@link ChatThreadClientBuilder} object
     */
    public ChatThreadClientBuilder httpClient(HttpClient httpClient) {
        if (httpClient == null) {
            throw logger.logExceptionAsError(new NullPointerException("httpClient is required."));
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Set a token credential for authorization
     *
     * @param communicationTokenCredential valid token credential as a string
     * @throws NullPointerException if communicationTokenCredential is null.
     * @return The updated {@link ChatThreadClientBuilder} object
     */
    public ChatThreadClientBuilder credential(CommunicationTokenCredential communicationTokenCredential) {
        if (communicationTokenCredential == null) {
            throw logger.logExceptionAsError(new NullPointerException("communicationTokenCredential is required."));
        }
        this.communicationTokenCredential = communicationTokenCredential;
        return this;
    }

    /**
     * Apply additional {@link HttpPipelinePolicy}
     *
     * @param pipelinePolicy HttpPipelinePolicy objects to be applied after
     *                       AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy
     * @throws NullPointerException if pipelinePolicy is null.
     * @return The updated {@link ChatThreadClientBuilder} object
     */
    public ChatThreadClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        if (pipelinePolicy == null) {
            throw logger.logExceptionAsError(new NullPointerException("pipelinePolicy is required."));
        }
        this.customPolicies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @throws NullPointerException if retryPolicy is null.
     * @return The updated {@link ChatThreadClientBuilder} object.
     */
    public ChatThreadClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        if (retryPolicy == null) {
            throw logger.logExceptionAsError(new NullPointerException("retryPolicy is required."));
        }
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @throws NullPointerException if logOptions is null.
     * @return The updated {@link ChatThreadClientBuilder} object
     */
    public ChatThreadClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        if (logOptions == null) {
            throw logger.logExceptionAsError(new NullPointerException("logOptions is required."));
        }
        this.logOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link ChatServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param serviceVersion {@link ChatServiceVersion} of the service to be used when making requests.
     * @return The updated {@link ChatThreadClientBuilder} object.
     */
    public ChatThreadClientBuilder serviceVersion(ChatServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;

        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return The updated {@link ChatThreadClientBuilder} object.
     */
    public ChatThreadClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the ChatThreadId used to construct a client for this chat thread.
     *
     * @param chatThreadId The id of the chat thread.
     * @return The updated {@link ChatThreadClientBuilder} object.
     */
    public ChatThreadClientBuilder chatThreadId(String chatThreadId) {
        this.chatThreadId = chatThreadId;
        return this;
    }

    /**
     * Create synchronous chat thread client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @throws NullPointerException if chatThreadId, endpoint or communicationTokenCredential is not set.
     * @return A {@link ChatThreadClient} instance.
     */
    public ChatThreadClient buildClient() {
        return new ChatThreadClient(buildAsyncClient());
    }

    /**
     * Create asynchronous chat thread client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @throws NullPointerException if chatThreadId, endpoint or communicationTokenCredential is not set.
     * @return A {@link ChatThreadAsyncClient} instance.
     */
    public ChatThreadAsyncClient buildAsyncClient() {
        if (chatThreadId == null) {
            throw logger.logExceptionAsError(new NullPointerException("'chatThreadId' is required."));
        }

        return new ChatThreadAsyncClient(createInternalClient(), chatThreadId);
    }

    private AzureCommunicationChatServiceImpl createInternalClient() {
        if (endpoint == null) {
            throw logger.logExceptionAsError(new NullPointerException("'endpoint' is required."));
        }

        HttpPipeline pipeline;

        if (this.httpPipeline != null) {
            pipeline = this.httpPipeline;
        } else {
            if (this.communicationTokenCredential == null) {
                throw logger
                    .logExceptionAsError(
                        new NullPointerException(
                            "CommunicationTokenCredential is required."));
            }

            HttpPipelinePolicy authorizationPolicy = chain -> {
                final CompletableFuture<CommunicationAccessToken> tokenFuture
                    = this.communicationTokenCredential.getToken();
                final CommunicationAccessToken token;
                try {
                    token = tokenFuture.get();
                } catch (ExecutionException e) {
                    chain.completedError(e);
                    return;
                } catch (InterruptedException e) {
                    chain.completedError(e);
                    return;
                }
                HttpRequest httpRequest = chain.getRequest();
                httpRequest.getHeaders().put("Authorization", "Bearer " + token.getToken());
                chain.processNextPolicy(httpRequest);
            };

            pipeline = createHttpPipeline(this.httpClient,
                authorizationPolicy,
                this.customPolicies);
        }

        AzureCommunicationChatServiceImplBuilder clientBuilder = new AzureCommunicationChatServiceImplBuilder()
            .apiVersion((this.serviceVersion == null)
                ? ChatServiceVersion.getLatest().getVersion()
                : this.serviceVersion.getVersion())
            .endpoint(this.endpoint)
            .pipeline(pipeline);

        return clientBuilder.buildClient();
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            List<HttpPipelinePolicy> additionalPolicies) {

        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        applyRequiredPolicies(policies, authorizationPolicy);

        if (additionalPolicies != null && additionalPolicies.size() > 0) {
            policies.addAll(additionalPolicies);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private void applyRequiredPolicies(List<HttpPipelinePolicy> policies, HttpPipelinePolicy authorizationPolicy) {
        policies.add(
            new UserAgentPolicy(null, "azure-communication-chat", "1.1.0-beta.3"));
        policies.add(retryPolicy == null ? RetryPolicy.withExponentialBackoff() : retryPolicy);
        policies.add(new CookiePolicy());
        policies.add(authorizationPolicy);
        policies.add(new HttpLoggingPolicy(this.logOptions));
    }
}
