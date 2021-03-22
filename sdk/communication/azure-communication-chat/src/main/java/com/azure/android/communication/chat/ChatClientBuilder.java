// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import android.content.Context;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImplBuilder;
import com.azure.android.communication.chat.signaling.CommunicationSignalingClient;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.credential.AccessToken;
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
import java.util.concurrent.Future;

/**
 * Builder for creating clients of Azure Communication Service Chat
 */
@ServiceClientBuilder(serviceClients = {ChatAsyncClient.class, ChatClient.class})
public final class ChatClientBuilder {
    private final ClientLogger logger = new ClientLogger(ChatClientBuilder.class);

    private String endpoint;
    private HttpClient httpClient;
    private CommunicationTokenCredential communicationTokenCredential;
    private HttpPipelinePolicy credentialPolicy;
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private HttpPipeline httpPipeline;
    private CommunicationSignalingClient communicationSignalingClient;
    private Context context;
    private String userToken;

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return the updated ChatClientBuilder object
     */
    public ChatClientBuilder endpoint(String endpoint) {
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
     * @return the updated ChatClientBuilder object
     */
    public ChatClientBuilder httpClient(HttpClient httpClient) {
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
     * @return the updated ChatClientBuilder object
     */
    public ChatClientBuilder credential(CommunicationTokenCredential communicationTokenCredential) {
        if (communicationTokenCredential == null) {
            throw logger.logExceptionAsError(new NullPointerException("communicationTokenCredential is required."));
        }
        this.communicationTokenCredential = communicationTokenCredential;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that attaches authorization header.
     *
     * @param credentialPolicy the credentials policy.
     * @return the updated ChatClientBuilder object
     */
    public ChatClientBuilder credentialPolicy(HttpPipelinePolicy credentialPolicy) {
        if (credentialPolicy == null) {
            throw logger.logExceptionAsError(new NullPointerException("credentialPolicy is required."));
        }
        this.credentialPolicy = credentialPolicy;
        return this;
    }

    /**
     * Apply additional {@link HttpPipelinePolicy}
     *
     * @param pipelinePolicy HttpPipelinePolicy objects to be applied after
     *                       AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy
     * @return the updated ChatClientBuilder object
     */
    public ChatClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        if (pipelinePolicy == null) {
            throw logger.logExceptionAsError(new NullPointerException("pipelinePolicy is required."));
        }
        this.customPolicies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated ChatClientBuilder object
     */
    public ChatClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        if (logOptions == null) {
            throw logger.logExceptionAsError(new NullPointerException("logOptions is required."));
        }
        this.logOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated BlobServiceClientBuilder object
     */
    public ChatClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    public ChatClientBuilder realtimeNotificationParams(Context context, String userToken) {
        this.context = context;
        this.userToken = userToken;
        return this;
    }

    /**
     * Create synchronous client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return ChatClient instance
     */
    public ChatClient buildClient() {
        ChatAsyncClient asyncClient = buildAsyncClient();
        return new ChatClient(asyncClient);
    }

    /**
     * Create asynchronous client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return ChatAsyncClient instance
     */
    public ChatAsyncClient buildAsyncClient() {
        if (this.endpoint == null) {
            throw logger.logExceptionAsError(new NullPointerException("Endpoint is required."));
        }

        HttpPipeline pipeline;
        if (this.httpPipeline != null) {
            pipeline = this.httpPipeline;
        } else {
            if (this.communicationTokenCredential == null && this.credentialPolicy == null) {
                throw logger
                    .logExceptionAsError(
                        new NullPointerException(
                            "Either CommunicationTokenCredential or CredentialPolicy is required."));
            }
            if (this.httpClient == null) {
                throw logger.logExceptionAsError(new NullPointerException("HttpClient is required."));
            }

            if (context != null && userToken != null) {
                communicationSignalingClient = new CommunicationSignalingClient(userToken, context);
            }

            final HttpPipelinePolicy authorizationPolicy;
            if (this.communicationTokenCredential != null) {
                authorizationPolicy = chain -> {
                    final Future<AccessToken> tokenFuture = this.communicationTokenCredential.getToken();
                    final AccessToken token;
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
            } else {
                authorizationPolicy = this.credentialPolicy;
            }
            pipeline = createHttpPipeline(this.httpClient,
                authorizationPolicy,
                this.customPolicies);
        }

        AzureCommunicationChatServiceImplBuilder clientBuilder = new AzureCommunicationChatServiceImplBuilder();
        clientBuilder.endpoint(this.endpoint)
            .pipeline(pipeline);
        return new ChatAsyncClient(clientBuilder.buildClient(), communicationSignalingClient);
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            List<HttpPipelinePolicy> additionalPolicies) {

        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(authorizationPolicy);
        applyRequiredPolicies(policies);

        if (additionalPolicies != null && additionalPolicies.size() > 0) {
            policies.addAll(additionalPolicies);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private void applyRequiredPolicies(List<HttpPipelinePolicy> policies) {
        policies.add(new UserAgentPolicy(null, "azure-communication-chat", "1.0.0-beta.8"));
        policies.add(RetryPolicy.withExponentialBackoff());
        policies.add(new CookiePolicy());
        policies.add(new HttpLoggingPolicy(this.logOptions));
    }
}
