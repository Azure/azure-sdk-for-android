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
public final class CommunicationTokenRefreshOptions {
    private final ClientLogger logger = new ClientLogger(CommunicationTokenRefreshOptions.class);
    private final Callable<String> tokenRefresher;
    private boolean refreshProactively;
    private String initialToken;

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
     * @deprecated Use {@link #CommunicationTokenRefreshOptions(Callable)} instead
     *           and chain fluent setter {@link #setRefreshProactively(boolean)}
     */
    @Deprecated
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
     * @deprecated Use {@link #CommunicationTokenRefreshOptions(Callable)} instead
     *             and chain fluent setter {@link #setRefreshProactively(boolean)}
     */
    @Deprecated
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
     * Creates a CommunicationTokenRefreshOptions object
     *
     * @param tokenRefresher The callable token refresher that acquires a fresh token from
     *                       the Communication Identity API.
     *                       The returned token must be valid (its expiration date
     *                       must be set in the future).
     */
    public CommunicationTokenRefreshOptions(Callable<String> tokenRefresher) {
        if (tokenRefresher == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("Missing required parameters 'tokenRefresher'."));
        }
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = false;
        this.initialToken = null;
    }

    /**
     * @return The token refresher to provide capacity to fetch fresh token
     */
    public Callable<String> getTokenRefresher() {
        return tokenRefresher;
    }

    /**
     * @return Whether or not to refresh token proactively
     */
    public boolean isRefreshProactively() {
        return refreshProactively;
    }

    /**
     * Set whether the token should be proactively renewed prior to its expiry or on
     * demand.
     *
     * @param refreshProactively the refreshProactively value to set.
     * @return the CommunicationTokenRefreshOptions object itself.
     */
    public CommunicationTokenRefreshOptions setRefreshProactively(boolean refreshProactively) {
        this.refreshProactively = refreshProactively;
        return this;
    }

    /**
     * @return The initial serialized JWT token
     */
    public String getInitialToken() {
        return initialToken;
    }

    /**
     * Set the optional serialized JWT token
     *
     * @param initialToken the initialToken value to set.
     * @return the CommunicationTokenRefreshOptions object itself.
     */
    public CommunicationTokenRefreshOptions setInitialToken(String initialToken) {
        this.initialToken = initialToken;
        return this;
    }
}
