// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.android.communication.chat.implementation;

import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.policy.CookiePolicy;
import com.azure.android.core.http.policy.RetryPolicy;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.azure.android.core.serde.jackson.JacksonSerder;

/** Initializes a new instance of the AzureCommunicationChatService type. */
public final class AzureCommunicationChatServiceImpl {
    /** The endpoint of the Azure Communication resource. */
    private final String endpoint;

    /**
     * Gets The endpoint of the Azure Communication resource.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /** Api Version. */
    private final String apiVersion;

    /**
     * Gets Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /** The HTTP pipeline to send requests through. */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /** The serializer to serialize an object into a string. */
    private final JacksonSerder jacksonSerder;

    /**
     * Gets The serializer to serialize an object into a string.
     *
     * @return the jacksonSerder value.
     */
    public JacksonSerder getJacksonSerder() {
        return this.jacksonSerder;
    }

    /** The ChatThreadsImpl object to access its operations. */
    private final ChatThreadImpl chatThreads;

    /**
     * Gets the ChatThreadsImpl object to access its operations.
     *
     * @return the ChatThreadsImpl object.
     */
    public ChatThreadImpl getChatThreadClient() {
        return this.chatThreads;
    }

    /** The ChatsImpl object to access its operations. */
    private final ChatImpl chats;

    /**
     * Gets the ChatsImpl object to access its operations.
     *
     * @return the ChatsImpl object.
     */
    public ChatImpl getChatClient() {
        return this.chats;
    }

    /**
     * Initializes an instance of AzureCommunicationChatService client.
     *
     * @param endpoint The endpoint of the Azure Communication resource.
     * @param apiVersion Api Version.
     */
    AzureCommunicationChatServiceImpl(String endpoint, String apiVersion) {
        this(
                new HttpPipelineBuilder()
                        .policies(new UserAgentPolicy(), RetryPolicy.withExponentialBackoff(), new CookiePolicy())
                        .build(),
                JacksonSerder.createDefault(),
                endpoint,
                apiVersion);
    }

    /**
     * Initializes an instance of AzureCommunicationChatService client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param endpoint The endpoint of the Azure Communication resource.
     * @param apiVersion Api Version.
     */
    AzureCommunicationChatServiceImpl(HttpPipeline httpPipeline, String endpoint, String apiVersion) {
        this(httpPipeline, JacksonSerder.createDefault(), endpoint, apiVersion);
    }

    /**
     * Initializes an instance of AzureCommunicationChatService client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param jacksonSerder The serializer to serialize an object into a string.
     * @param endpoint The endpoint of the Azure Communication resource.
     * @param apiVersion Api Version.
     */
    AzureCommunicationChatServiceImpl(
            HttpPipeline httpPipeline, JacksonSerder jacksonSerder, String endpoint, String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.jacksonSerder = jacksonSerder;
        this.endpoint = endpoint;
        this.apiVersion = apiVersion;
        this.chatThreads = new ChatThreadImpl(this);
        this.chats = new ChatImpl(this);
    }
}
