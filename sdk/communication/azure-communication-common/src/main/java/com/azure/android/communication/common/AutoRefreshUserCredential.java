// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.credential.AccessToken;
import com.azure.android.core.logging.ClientLogger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class AutoRefreshUserCredential extends UserCredential {
    private static final int ON_DEMAND_REFRESH_BUFFER_SECS = 120;
    private static final int PROACTIVE_REFRESH_BUFFER_SECS = 600;

    private final ClientLogger logger = new ClientLogger(AutoRefreshUserCredential.class);

    private Callable<String> tokenRefresher;
    private final Callable<AccessToken> accessTokenCallable;
    private FutureTask<AccessToken> tokenFuture;
    private final Timer timer;
    private TimerTask proactiveRefreshTask;

    AutoRefreshUserCredential(Callable<String> tokenRefresher) {
        this(tokenRefresher, false);
    }

    AutoRefreshUserCredential(Callable<String> tokenRefresher, String initialToken) {
        this(tokenRefresher, false, initialToken);
    }

    AutoRefreshUserCredential(Callable<String> tokenRefresher, boolean refreshProactively) {
        this(tokenRefresher, refreshProactively, null);
    }

    AutoRefreshUserCredential(Callable<String> tokenRefresher, boolean refreshProactively, String initialToken) {
        this.tokenRefresher = tokenRefresher;
        this.timer = new Timer();
        this.accessTokenCallable = setupAccessTokenCallable(refreshProactively);

        AccessToken initialAccessToken = null;

        if (initialToken != null) {
            initialAccessToken = TokenParser.createAccessToken(initialToken);
            this.tokenFuture = setupInitialTokenFuture(initialAccessToken);
        }

        if (refreshProactively) {
            this.scheduleProactiveRefresh(initialAccessToken);
        }
    }

    @Override
    public Future<AccessToken> getToken() {
        if (shouldRefreshTokenOnDemand()) {
            this.updateTokenFuture();
        }

        return this.tokenFuture;
    }

    @Override
    public void dispose() {
        if (this.tokenFuture != null) {
            this.tokenFuture.cancel(true);
        }

        if (this.proactiveRefreshTask != null) {
            this.proactiveRefreshTask.cancel();
        }

        this.timer.cancel();
        this.timer.purge();

        this.tokenRefresher = null;
        this.tokenFuture = null;
        this.proactiveRefreshTask = null;

        super.dispose();
    }

    private FutureTask<AccessToken> setupInitialTokenFuture(AccessToken initialAccessToken) {
        FutureTask<AccessToken> initialTokenFuture = new FutureTask<>(() -> initialAccessToken);
        initialTokenFuture.run();
        return initialTokenFuture;
    }

    private Callable<AccessToken> setupAccessTokenCallable(boolean refreshProactively) {
        if (!refreshProactively) {
            return this::refreshAccessToken;
        }

        return () -> {
            AccessToken accessToken = this.refreshAccessToken();
            this.scheduleProactiveRefresh(accessToken);
            return accessToken;
        };
    }

    private AccessToken refreshAccessToken() throws Exception {
        String tokenStr = this.tokenRefresher.call();
        AccessToken accessToken = TokenParser.createAccessToken(tokenStr);
        return accessToken;
    }

    private boolean shouldRefreshTokenOnDemand() {
        boolean shouldRefreshTokenOnDemand = false;
        if (this.tokenFuture == null || this.tokenFuture.isCancelled()) {
            shouldRefreshTokenOnDemand = true;
        } else if (this.tokenFuture.isDone()) {
            try {
                AccessToken accessToken = this.tokenFuture.get();
                long refreshEpochSecond = accessToken.getExpiresAt().toEpochSecond() - ON_DEMAND_REFRESH_BUFFER_SECS;
                long currentEpochSecond = System.currentTimeMillis() / 1000;
                shouldRefreshTokenOnDemand = currentEpochSecond >= refreshEpochSecond;
            } catch (ExecutionException | InterruptedException e) {
                shouldRefreshTokenOnDemand = true;
            }
        }

        return shouldRefreshTokenOnDemand;
    }

    private synchronized void updateTokenFuture() {
        // Ignore update if disposed
        if (this.isDisposed()) {
            return;
        }

        // Ignore update if tokenFuture in progress
        if (this.tokenFuture != null && !this.tokenFuture.isDone() && !this.tokenFuture.isCancelled()) {
            return;
        }

        FutureTask<AccessToken> futureTask = new FutureTask<>(this.accessTokenCallable);
        this.tokenFuture = futureTask;

        ScheduledTask scheduledTask = new ScheduledTask(futureTask::run);
        try {
            this.timer.schedule(scheduledTask, 0);
        } catch (IllegalStateException e) {
            logger.warning("AutoRefreshUserCredential has been disposed. Unable to schedule token refresh.", e);
        }
    }

    private synchronized void scheduleProactiveRefresh(AccessToken accessToken) {
        if (this.isDisposed()) {
            return;
        }

        long delayMs = 0;

        if (accessToken != null) {
            long refreshEpochSecond = accessToken.getExpiresAt().toEpochSecond() - PROACTIVE_REFRESH_BUFFER_SECS;
            long currentEpochSecond = System.currentTimeMillis() / 1000;
            delayMs = Math.max((refreshEpochSecond - currentEpochSecond) * 1000, 0);
        }

        if (this.proactiveRefreshTask != null) {
            this.proactiveRefreshTask.cancel();
        }

        this.proactiveRefreshTask = new ScheduledTask(this::updateTokenFuture);
        this.timer.schedule(this.proactiveRefreshTask, delayMs);
    }

    private static final class ScheduledTask extends TimerTask {
        private final Runnable runnable;

        ScheduledTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }
    }
}
