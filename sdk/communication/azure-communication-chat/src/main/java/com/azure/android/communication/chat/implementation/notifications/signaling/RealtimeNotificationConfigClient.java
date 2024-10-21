// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.policy.RetryPolicy;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RealtimeNotificationConfigClient {
    private final HttpPipeline httpPipeline;

    private final ClientLogger logger = new ClientLogger(RealtimeNotificationConfigClient.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";

    RealtimeNotificationConfigClient(HttpPipeline httpPipeline) {
        this.httpPipeline = createHttpPipeline(httpPipeline);
    }

    /**
     * Fetches the RealTimeNotificationConfiguration from the server.
     *
     * @param token             The Bearer token for authorization.
     * @param endpoint          The base endpoint URL.
     * @param configApiVersion  The API version parameter.
     */
    public RealtimeNotificationConfig getTrouterSettings(String token, String endpoint, String configApiVersion) {
        /// Construct the URL
        String urlString = endpoint + "/chat/config/realTimeNotifications?api-version=" + configApiVersion;

        // Build the HttpRequest
        HttpRequest request = new HttpRequest(HttpMethod.GET, urlString);
        request
            .setHeader(AUTHORIZATION_HEADER, "Bearer " + token)
            .setHeader("Accept", "application/json");

        // Initialize CountDownLatch and error holder
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] requestError = { null };
        final RealtimeNotificationConfig[] configResult = {null};
        // Send the request asynchronously using HttpPipeline
        httpPipeline.send(request, RequestContext.NONE, CancellationToken.NONE, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                int statusCode = response.getStatusCode();
                logger.info("Retrieve realtime notification config HTTP response code: " + statusCode);
                if (statusCode != 200) {
                    try {
                        String errorBody = response.getBodyAsString();
                        requestError[0] = new RuntimeException("Registrar register request failed with HTTP status code "
                            + statusCode
                            + ". Error message: "
                            + errorBody);
                    } catch (Exception e) {
                        requestError[0] = new RuntimeException("Failed to read error response body", e);
                    }
                } else {
                    // Convert the response content to RealtimeNotificationConfig
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        configResult[0] = objectMapper.readValue(response.getBodyAsString(), RealtimeNotificationConfig.class);
                        logger.info("Successfully converted response to RealtimeNotificationConfig.");
                    } catch (Exception e) {
                        logger.error("Failed to parse response body to RealtimeNotificationConfig: " + e.getMessage(), e);
                        requestError[0] = new RuntimeException("Failed to parse response body", e);
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                logger.error("HTTP request failed: " + error.getMessage(), error);
                requestError[0] = error;
                latch.countDown();
            }
        });

        // Wait for the asynchronous operation to complete (with a timeout for safety)
        boolean completed = awaitOnLatch(latch);
        if (!completed) {
            throw logger.logThrowableAsError(new RuntimeException("HTTP request timed out."));
        }

        // Check for errors and throw an exception if necessary
        if (requestError[0] != null) {
            throw logger.logThrowableAsError(new RuntimeException("All retry attempts failed.", requestError[0]));
        }

        // Return the result
        return configResult[0];
    }

    // Create HttpPipeline based on polices passed from Contonso. We only apply retry policy and user agent policy.
    private HttpPipeline createHttpPipeline(HttpPipeline httpPipeline) {
        List<HttpPipelinePolicy> customPolicies = getEssentialPolicies(httpPipeline);
        // httpPipeline policies
        HttpPipeline newPipeline = new HttpPipelineBuilder()
            .httpClient(HttpClient.createDefault())
            .policies(customPolicies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return newPipeline;
    }

    /**
     * Retrieves all policies from the given HttpPipeline instance.
     *
     * @param pipeline The HttpPipeline instance.
     * @return A list of HttpPipelinePolicy instances.
     */
    public static List<HttpPipelinePolicy> getEssentialPolicies(HttpPipeline pipeline) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        int index = 0;
        while (true) {
            try {
                HttpPipelinePolicy policy = pipeline.getPolicy(index);
                // Only add retry policy and user agent policy for now
                if (policy instanceof RetryPolicy || policy instanceof UserAgentPolicy) {
                    policies.add(policy);
                }
                index++;
            } catch (IndexOutOfBoundsException e) {
                // No more policies
                break;
            }
        }
        return policies;
    }

    private boolean awaitOnLatch(CountDownLatch latch) {
        long timeoutInSec = 10;
        try {
            return latch.await(timeoutInSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException("Operation didn't complete within " + timeoutInSec + " seconds"));
        }
    }
}
