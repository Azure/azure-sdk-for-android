// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.implementation.AzureCommunicationChatServiceImplBuilder;
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
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private HttpPipeline httpPipeline;
//    private Configuration configuration;

//    private static final String APP_CONFIG_PROPERTIES = "azure-communication-chat.properties";
//    private static final String SDK_NAME = "name";
//    private static final String SDK_VERSION = "version";

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

//    /**
//     * Sets the {@link ChatServiceVersion} that is used when making API requests.
//     * <p>
//     * If a service version is not provided, the service version that will be used will be the latest known service
//     * version based on the version of the client library being used. If no service version is specified, updating
//     * to a newer version of the client library will have the result of potentially moving to a newer service version.
//     * <p>
//     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
//     *
//     * @param version {@link ChatServiceVersion} of the service to be used when making requests.
//     * @return the updated ChatClientBuilder object
//     */
//    public ChatClientBuilder serviceVersion(ChatServiceVersion version) {
//        return this;
//    }

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

//    /**
//     * Sets the configuration object used to retrieve environment configuration values during building of the client.
//     *
//     * @param configuration Configuration store used to retrieve environment configurations.
//     * @return the updated BlobServiceClientBuilder object
//     */
//    public ChatClientBuilder configuration(Configuration configuration) {
//        this.configuration = configuration;
//        return this;
//    }

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
            if (this.communicationTokenCredential == null) {
                throw logger.logExceptionAsError(new NullPointerException("CommunicationTokenCredential is required."));
            }
            if (this.httpClient == null) {
                throw logger.logExceptionAsError(new NullPointerException("HttpClient is required."));
            }
            pipeline = createHttpPipeline(this.httpClient,
                chain -> {
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
                },
                this.customPolicies);
        }

        AzureCommunicationChatServiceImplBuilder clientBuilder = new AzureCommunicationChatServiceImplBuilder();
        clientBuilder.endpoint(this.endpoint)
            .pipeline(pipeline);
        return new ChatAsyncClient(clientBuilder.buildClient());
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
        policies.add(getUserAgentPolicy());
        policies.add(RetryPolicy.withExponentialBackoff());
        policies.add(new CookiePolicy());
        policies.add(new HttpLoggingPolicy(this.logOptions));
    }

    /*
     * Creates a {@link UserAgentPolicy} using the default chat service module name and version.
     *
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private UserAgentPolicy getUserAgentPolicy() {
//        Map<String, String> properties = CoreUtils.getProperties(APP_CONFIG_PROPERTIES);
//        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
//        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
//        return new UserAgentPolicy(logOptions.getApplicationId(), clientName, clientVersion, configuration);
        return new UserAgentPolicy(null, "azure-communication-chat", "1.0.0-beta.8");
    }
}
