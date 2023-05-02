// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommunicationTokenCredentialTest {

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_noInitialToken_refresh() throws InterruptedException, ExecutionException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(900);
        mockTokenRefresher.setToken(refreshedToken);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(countDownLatch::countDown);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true));
        countDownLatch.await();

        CommunicationAccessToken accessToken = credential.getToken().get();

        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_initialTokenPastThreshold_refresh() throws InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(600);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(countDownLatch::countDown);

        new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true, tokenString));
        countDownLatch.await();

        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_initialTokenWithinThreshold_notRefreshed() throws InterruptedException, ExecutionException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(700);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();

        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true, tokenString));
        CommunicationAccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_withInitialTokenInvalid() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
                new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true, "This is an invalid token string"));
            });
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_repeats() throws InterruptedException {
        // Set up MockTokenRefresher to return token with expiry that immediately needs to be refreshed
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String tokenPastThreshold = TokenStubHelper.createTokenStringForOffset(600);
        mockTokenRefresher.setToken(tokenPastThreshold);

        // Limit testing repeats
        int numRepeats = 10;
        CountDownLatch countDownLatch = new CountDownLatch(numRepeats);
        mockTokenRefresher.setOnCallReturn(() -> {
            if (countDownLatch.getCount() == 1) {
                // Last token returned will not require immediate refresh
                String tokenWithinThreshold = TokenStubHelper.createTokenStringForOffset(1200);
                mockTokenRefresher.setToken(tokenWithinThreshold);
            }
            countDownLatch.countDown();
        });

        new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true));
        countDownLatch.await();

        assertEquals(numRepeats, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_whileProactiveRefresh_singleResult() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(1200);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true));

        CompletableFuture<CommunicationAccessToken> accessTokenFuture = credential.getToken();
        blockedRefresh.run();
        CommunicationAccessToken accessToken = accessTokenFuture.get();

        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void dispose_proactiveRefresh_inProgressCancelled() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(1200);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, true));
        CompletableFuture<CommunicationAccessToken> accessTokenFuture = credential.getToken();
        credential.dispose();
        blockedRefresh.run();

        assertTrue(accessTokenFuture.isDone());

        assertThrows(IllegalStateException.class,
            () -> {
                accessTokenFuture.get();
            });
    }

    private Runnable arrangeBlockedRefresh(MockTokenRefresher mockTokenRefresher) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return countDownLatch::countDown;
    }
}
