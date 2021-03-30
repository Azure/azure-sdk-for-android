// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import android.os.Build;

import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;

/**
 * Pipeline policy that adds "User-Agent" header to a request.
 *
 * The format for the "User-Agent" string is outlined in
 * <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>.
 */
public class UserAgentPolicy implements HttpPipelinePolicy {
    private final String userAgent;

    /**
     * Creates a {@link UserAgentPolicy} with a default user agent string i.e. "azsdk-android".
     */
    public UserAgentPolicy() {
        this.userAgent = "azsdk-android";
    }

    /**
     * Creates a UserAgentPolicy with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param applicationId User specified application Id.
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     */
    public UserAgentPolicy(String applicationId, String sdkName, String sdkVersion) {
        StringBuilder userAgentBuilder = new StringBuilder();

        //The application ID is optional.
        if (applicationId != null) {
            // Maximum length of application id is limited to 24 by the design guidelines.
            applicationId = applicationId.length() > 24
                ? applicationId.substring(0, 24)
                : applicationId;
            userAgentBuilder.append(applicationId).append(" ");
        }

        // Add the required default User-Agent string.
        userAgentBuilder.append("azsdk-android")
            .append("-")
            .append(sdkName)
            .append("/")
            .append(sdkVersion);

        // Note: there is no good way to get the Java language version on Android
        // (System.getProperty("java.version") returns "0", for example).

        userAgentBuilder.append(" ")
            .append("(")
            .append(String.format("%s; %s", Build.MANUFACTURER, Build.MODEL))
            .append(")");

        this.userAgent = userAgentBuilder.toString();
    }


    /**
     * Updates the "User-Agent" header with the value supplied at the time of creating policy.
     */
    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        final String existingUserAgent = httpRequest.getHeaders().getValue("User-Agent");
        httpRequest.getHeaders().put("User-Agent",
            existingUserAgent != null && existingUserAgent.length() != 0
                ? existingUserAgent + " " + this.userAgent
                : this.userAgent);
        chain.processNextPolicy(httpRequest);
    }
}
