// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.credential;

/**
 * The interface for credentials that can provide a token.
 */
@FunctionalInterface
public interface TokenCredential {
    /**
     * Asynchronously get a token for a given resource/audience.
     *
     * This method is called automatically by Azure SDK client libraries.
     * You may call this method directly, but you must also handle token
     * caching and token refreshing.
     *
     * @param request the details of the token request
     * @param callback the callback to receive the token retrieval result.
     */
    void getToken(TokenRequestContext request, TokenCredentialCallback callback);

    /**
     * The callback type to receive the token retrieval result.
     */
    interface TokenCredentialCallback {
        /**
         * Called when the access token is successfully retrieved.
         *
         * @param accessToken The access token.
         */
        void onSuccess(AccessToken accessToken);

        /**
         * Called when the {@code request} call could not be executed due an error.
         *
         * @param throwable The reason for failure in retrieving token.
         */
        void onError(Throwable throwable);
    }
}
