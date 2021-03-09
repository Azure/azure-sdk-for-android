// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class StaticUserCredential extends UserCredential {

    private Future<CommunicationAccessToken> tokenFuture;

    StaticUserCredential(String userToken) {
        CommunicationAccessToken accessToken = TokenParser.createAccessToken(userToken);
        this.tokenFuture = new CompletedTokenFuture(accessToken);
    }

    @Override
    public Future<CommunicationAccessToken> getToken() {
        return this.tokenFuture;
    }

    private final class CompletedTokenFuture implements Future<CommunicationAccessToken> {
        private final CommunicationAccessToken accessToken;

        CompletedTokenFuture(CommunicationAccessToken accessToken) {
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
        public CommunicationAccessToken get() {
            return this.accessToken;
        }

        @Override
        public CommunicationAccessToken get(long timeout, TimeUnit unit) {
            return accessToken;
        }
    }
}
