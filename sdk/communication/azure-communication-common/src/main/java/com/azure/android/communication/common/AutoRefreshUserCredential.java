// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.logging.ClientLogger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import java9.util.concurrent.CompletableFuture;

final class AutoRefreshUserCredential extends UserCredential {
    private static final String CREDENTIAL_DISPOSED = "UserCredential has been disposed.";
    private static final int ON_DEMAND_REFRESH_BUFFER_SECS = 120;
    private static final int PROACTIVE_REFRESH_BUFFER_SECS = 600;

    private final ClientLogger logger = new ClientLogger(AutoRefreshUserCredential.class);

    private final Callable<String> tokenRefreshCallable;
    private final boolean refreshProactively;
    private volatile CompletableFuture<CommunicationAccessToken> tokenFuture;
    private CompletableFuture<Void> tokenFutureUpdater;

    AutoRefreshUserCredential(final Callable<String> tokenRefresher) {
        this(tokenRefresher, false);
    }

    AutoRefreshUserCredential(final Callable<String> tokenRefresher,
                              final String initialToken) {
        this(tokenRefresher, false, initialToken);
    }

    AutoRefreshUserCredential(final Callable<String> tokenRefresher,
                              final boolean refreshProactively) {
        this(tokenRefresher, refreshProactively, null);
    }

    AutoRefreshUserCredential(final Callable<String> tokenRefresher,
                              final boolean refreshProactively,
                              final String initialToken) {
        this.tokenRefreshCallable = tokenRefresher;
        this.refreshProactively = refreshProactively;

        CommunicationAccessToken initialAccessToken = null;

        if (initialToken != null) {
            initialAccessToken = TokenParser.createAccessToken(initialToken);
            this.tokenFuture = CompletableFuture.completedFuture(initialAccessToken);
        }

        if (this.refreshProactively) {
            this.scheduleTokenFutureUpdate(initialAccessToken);
        }
    }

    @Override
    public CompletableFuture<CommunicationAccessToken> getToken() {
        if (this.shouldRefreshOnDemand()) {
            this.updateTokenFuture();
        }
        return this.tokenFuture;
    }

    private boolean shouldRefreshOnDemand() {
        final CompletableFuture<CommunicationAccessToken> tokenFuture = this.tokenFuture;
        if (tokenFuture == null || tokenFuture.isCancelled()) {
            return true;
        } else if (tokenFuture.isDone()) {
            try {
                CommunicationAccessToken accessToken = tokenFuture.get();
                long refreshEpochSecond = accessToken.getExpiresAt().toEpochSecond() - ON_DEMAND_REFRESH_BUFFER_SECS;
                long currentEpochSecond = System.currentTimeMillis() / 1000;
                return currentEpochSecond >= refreshEpochSecond;
            } catch (ExecutionException | InterruptedException e) {
                return true;
            }
        }

        return false;
    }

    private synchronized void updateTokenFuture() {
        if (this.isDisposed()) {
            this.tokenFuture = CompletableFuture.failedFuture(new IllegalStateException(CREDENTIAL_DISPOSED));
            return;
        }

        final CompletableFuture<CommunicationAccessToken> tokenFuture = this.tokenFuture;
        if (tokenFuture != null && !tokenFuture.isDone() && !tokenFuture.isCancelled()) {
            // don't update if the tokenFuture in-progress.
            return;
        }

        this.tokenFuture = CompletableFuture.supplyAsync(() -> {
            if (this.isDisposed()) {
                throw logger.logExceptionAsError(new IllegalStateException(CREDENTIAL_DISPOSED));
            }

            final CommunicationAccessToken accessToken;

            try {
                final String tokenStr = this.tokenRefreshCallable.call();
                accessToken = TokenParser.createAccessToken(tokenStr);
            } catch (Exception e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }

            if (this.refreshProactively) {
                this.scheduleTokenFutureUpdate(accessToken);
            }

            return accessToken;
        });
    }

    private synchronized void scheduleTokenFutureUpdate(CommunicationAccessToken accessToken) {
        if (this.isDisposed()) {
            return;
        }

        if (this.tokenFutureUpdater != null) {
            this.tokenFutureUpdater.cancel(true);
        }

        long delayMs = 0;
        if (accessToken != null) {
            long refreshEpochSecond = accessToken.getExpiresAt().toEpochSecond() - PROACTIVE_REFRESH_BUFFER_SECS;
            long currentEpochSecond = System.currentTimeMillis() / 1000;
            delayMs = Math.max((refreshEpochSecond - currentEpochSecond) * 1000, 0);
        }

        this.tokenFutureUpdater = CompletableFuture.runAsync(this::updateTokenFuture,
            CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS));
    }

    @Override
    public void dispose() {
        if (this.tokenFuture != null) {
            this.tokenFuture.cancel(true);
        }

        if (this.tokenFutureUpdater != null) {
            this.tokenFutureUpdater.cancel(true);
        }

        super.dispose();
    }

}