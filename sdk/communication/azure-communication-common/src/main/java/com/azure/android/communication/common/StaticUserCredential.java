// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.credential.AccessToken;

import java9.util.concurrent.CompletableFuture;

final class StaticUserCredential extends UserCredential {
    private final CompletableFuture<AccessToken> tokenFuture;

    StaticUserCredential(String userToken) {
        AccessToken accessToken = TokenParser.createAccessToken(userToken);
        this.tokenFuture = CompletableFuture.completedFuture(accessToken);
    }

    @Override
    public CompletableFuture<AccessToken> getToken() {
        return this.tokenFuture;
    }
}
