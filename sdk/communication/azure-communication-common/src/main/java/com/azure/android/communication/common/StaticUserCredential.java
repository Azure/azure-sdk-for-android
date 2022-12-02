// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import java9.util.concurrent.CompletableFuture;

final class StaticUserCredential extends UserCredential {
    private final CompletableFuture<CommunicationAccessToken> tokenFuture;

    StaticUserCredential(String userToken) {
        this.tokenFuture = CompletableFuture.completedFuture(TokenParser.createAccessToken(userToken));
    }

    @Override
    public CompletableFuture<CommunicationAccessToken> getToken() {
        return this.tokenFuture;
    }
}