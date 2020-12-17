// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Options for refreshing CommunicationTokenCredential
 * <p>
 * This class is used to define how CommunicationTokenCredential should be refreshed
 * </p>
 */
public class CommunicationTokenRefreshOptions {
    private final Callable<String> tokenRefresher;
    private final boolean refreshProactively;
    private final String token;

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
        Objects.nonNull(tokenRefresher);
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.token = null;
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
     * @param token the serialized JWT token, cannot be null
     */
    public CommunicationTokenRefreshOptions(Callable<String> tokenRefresher, boolean refreshProactively, String token) {
        Objects.nonNull(tokenRefresher);
        Objects.nonNull(token);
        this.tokenRefresher = tokenRefresher;
        this.refreshProactively = refreshProactively;
        this.token = token;
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
        return token;
    }
}
