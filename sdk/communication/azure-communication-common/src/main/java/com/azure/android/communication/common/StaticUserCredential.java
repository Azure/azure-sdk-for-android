// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.credential.AccessToken;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class StaticUserCredential extends UserCredential {

    private Future<AccessToken> tokenFuture;

    StaticUserCredential(String userToken) {
        AccessToken accessToken = TokenParser.createAccessToken(userToken);
        this.tokenFuture = new CompletedTokenFuture(accessToken);
    }

    @Override
    public Future<AccessToken> getToken() {
        return this.tokenFuture;
    }

    private final class CompletedTokenFuture implements Future<AccessToken> {
        private final AccessToken accessToken;

        CompletedTokenFuture(AccessToken accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public AccessToken get() {
            return this.accessToken;
        }

        @Override
        public AccessToken get(long timeout, TimeUnit unit) {
            return accessToken;
        }
    }
}
