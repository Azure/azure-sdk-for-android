// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class AutoRefreshUserCredential extends UserCredential {
    private static final int ON_DEMAND_REFRESH_BUFFER_SECS = 120;
    private static final int PROACTIVE_REFRESH_BUFFER_SECS = 600;

    private Callable<String> tokenRefresher;
    private Callable<CommunicationAccessToken> accessTokenCallable;
    private FutureTask<CommunicationAccessToken> tokenFuture;
    private Timer timer;
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

        CommunicationAccessToken initialAccessToken = null;

        if (initialToken != null) {
            initialAccessToken = TokenParser.createAccessToken(initialToken);
            this.tokenFuture = setupInitialTokenFuture(initialAccessToken);
        }

        if (refreshProactively) {
            this.scheduleProactiveRefresh(initialAccessToken);
        }
    }

    @Override
    public Future<CommunicationAccessToken> getToken() {
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

    private FutureTask<CommunicationAccessToken> setupInitialTokenFuture(CommunicationAccessToken initialAccessToken) {
        FutureTask<CommunicationAccessToken> initialTokenFuture = new FutureTask<>(() -> initialAccessToken);
        initialTokenFuture.run();
        return initialTokenFuture;
    }

    private Callable<CommunicationAccessToken> setupAccessTokenCallable(boolean refreshProactively) {
        if (!refreshProactively) {
            return this::refreshAccessToken;
        }

        return () -> {
            CommunicationAccessToken accessToken = this.refreshAccessToken();
            this.scheduleProactiveRefresh(accessToken);
            return accessToken;
        };
    }

    private CommunicationAccessToken refreshAccessToken() throws Exception {
        String tokenStr = this.tokenRefresher.call();
        CommunicationAccessToken accessToken = TokenParser.createAccessToken(tokenStr);
        return accessToken;
    }

    private boolean shouldRefreshTokenOnDemand() {
        boolean shouldRefreshTokenOnDemand = false;
        if (this.tokenFuture == null || this.tokenFuture.isCancelled()) {
            shouldRefreshTokenOnDemand = true;
        } else if (this.tokenFuture.isDone()) {
            try {
                CommunicationAccessToken accessToken = this.tokenFuture.get();
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

        FutureTask<CommunicationAccessToken> futureTask = new FutureTask<>(this.accessTokenCallable);
        this.tokenFuture = futureTask;

        ScheduledTask scheduledTask = new ScheduledTask(futureTask::run);
        try {
            this.timer.schedule(scheduledTask, 0);
        } catch (IllegalStateException e) {
            Log.w("Communication", "AutoRefreshUserCredential has been disposed. Unable to schedule token refresh.");
        }
    }

    private synchronized void scheduleProactiveRefresh(CommunicationAccessToken accessToken) {
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

    private final class ScheduledTask extends TimerTask {
        private Runnable runnable;

        ScheduledTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }
    }
}
