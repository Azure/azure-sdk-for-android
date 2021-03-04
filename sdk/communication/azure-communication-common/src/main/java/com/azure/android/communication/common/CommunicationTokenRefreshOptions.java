// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import com.azure.android.core.logging.ClientLogger;

import java.util.concurrent.Callable;

/**
 * Options for refreshing CommunicationTokenCredential
 * <p>
 * This class is used to define how CommunicationTokenCredential should be refreshed
 * </p>
 */
public class CommunicationTokenRefreshOptions {
    private final ClientLogger logger = new ClientLogger(CommunicationTokenRefreshOptions.class);
    private final Callable<String> tokenRefresher;
    private final boolean refreshProactively;
    private final String initialToken;

    /**
     * Creates a {@link CommunicationTokenRefreshOptions} object
     * <p>
     * Access token will be fetched on demand and may optionally enable proactive refreshing
     * </p>
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token, cannot be null
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     */
    public CommunicationTokenRefreshOptions(Callable<String> tokenRefresher, boolean refreshProactively) {
        if (tokenRefresher == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("Missing required parameters 'tokenRefresher'."));
        }
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = null;
    }

    /**
     * Creates a {@link CommunicationTokenRefreshOptions} object
     * <p>
     * A valid token is supplied and may optionally enable proactive refreshing
     * </p>
     *
     * @param tokenRefresher the token refresher to provide capacity to fetch fresh token, cannot be null
     * @param refreshProactively when set to true, turn on proactive fetching to call
     *                           tokenRefresher before token expiry by minutes set
     *                           with setCallbackOffsetMinutes or default value of
     *                           two minutes
     * @param initialToken the serialized JWT token, cannot be null
     * @throws IllegalArgumentException if the parameter tokenRefresher or initialToken is null.
     */
    public CommunicationTokenRefreshOptions(Callable<String> tokenRefresher, boolean refreshProactively,
                                            String initialToken) {
        if (tokenRefresher == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("Missing required parameters 'tokenRefresher'."));
        }
        if (initialToken == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("Missing required parameters 'initialToken'."));
        }
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.initialToken = initialToken;
    }

    /**
     * @return the token refresher to provide capacity to fetch fresh token
     */
    public Callable<String> getTokenRefresher() {
        return tokenRefresher;
    }

    /**
     * @return whether or not to refresh token proactively
     */
    public boolean getRefreshProactively() {
        return refreshProactively;
    }

    /**
     * @return the serialized JWT token
     */
    public String getToken() {
        return initialToken;
    }
}
