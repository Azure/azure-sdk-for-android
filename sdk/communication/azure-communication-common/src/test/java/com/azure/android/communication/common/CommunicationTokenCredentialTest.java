// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommunicationTokenCredentialTest {

    @Test()
    public void constructor_withStaticInitialTokenInvalid() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                new CommunicationTokenCredential("This is an invalid token string");
            });
    }

    @Test
    public void constructor_withTokenRefresher_withInitialTokenInvalid() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
                new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, "This is an invalid token string"));
            });
    }

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
    public void getToken_staticInitialTokenActive() throws ExecutionException, InterruptedException {
        long expiryEpochSecond = System.currentTimeMillis() / 1000 + 60;
        String tokenString = TokenStubHelper.createTokenString(expiryEpochSecond);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(tokenString);
        CommunicationAccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(expiryEpochSecond, accessToken.getExpiresAt().toEpochSecond());
        assertFalse(accessToken.isExpired());
    }

    @Test
    public void getToken_staticInitialTokenExpired() throws ExecutionException, InterruptedException {
        long expiryEpochSecond = System.currentTimeMillis() / 1000 - 60;
        String tokenString = TokenStubHelper.createTokenString(expiryEpochSecond);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(tokenString);
        CommunicationAccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(expiryEpochSecond, accessToken.getExpiresAt().toEpochSecond());
        assertTrue(accessToken.isExpired());
    }

    @Test
    public void getToken_staticInitialToken_isDisposed_cancelledFuture() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(1200);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(tokenString);

        credential.dispose();
        CompletableFuture<CommunicationAccessToken> accessTokenFuture = credential.getToken();

        assertTrue(accessTokenFuture.isDone());
        ExecutionException executionException = assertThrows(ExecutionException.class,
            () -> {
                accessTokenFuture.get();
            });
        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof IllegalStateException);
    }

    @Test
    public void getToken_onDemandAutoRefresh_noInitialToken_firstFetch() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false));

        CommunicationAccessToken accessToken = credential.getToken().get();

        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
        assertFalse(accessToken.isExpired());
    }

    @Test
    public void getToken_onDemandAutoRefresh_noInitialToken_firstFetch_exception() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        RuntimeException mockTokenRefresherException = new RuntimeException("Mock Token Refresh Exception");
        mockTokenRefresher.setOnCallReturn(() -> {
            throw mockTokenRefresherException;
        });
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false));

        ExecutionException thrown = assertThrows(ExecutionException.class,
            () -> {
                try {
                    credential.getToken().get();
                } finally {
                    assertEquals(1, mockTokenRefresher.getCallCount());
                }
            });

        assertTrue(thrown.getMessage().contains(mockTokenRefresherException.getMessage()));
    }

    @Test
    public void getToken_onDemandAutoRefresh_noInitialToken_firstFetch_multithreadedSameResult() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false));

        // Set up blocked multithreaded calls
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        int numCalls = 5;
        List<CompletableFuture<CommunicationAccessToken>> accessTokenFutures = new ArrayList<>();
        for (int i = 0; i < numCalls; i++) {
            accessTokenFutures.add(credential.getToken());
        }

        // Unblock refresh and wait for results
        blockedRefresh.run();
        Set<CommunicationAccessToken> accessTokenResults = new HashSet<>();
        for (CompletableFuture<CommunicationAccessToken> accessTokenFuture : accessTokenFutures) {
            accessTokenResults.add(accessTokenFuture.get());
        }

        assertEquals(1, accessTokenResults.size());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenWithinThresholdNotRefreshed_singleCall() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(130);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));

        CommunicationAccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenWithinThresholdNotRefreshed_multithreadedCalls() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(130);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));

        // Set up blocked multithreaded calls
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        int numCalls = 5;
        List<CompletableFuture<CommunicationAccessToken>> accessTokenFutures = new ArrayList<>();
        for (int i = 0; i < numCalls; i++) {
            accessTokenFutures.add(credential.getToken());
        }

        // Unblock refresh and wait for results
        blockedRefresh.run();
        Set<CommunicationAccessToken> accessTokenResults = new HashSet<>();
        for (CompletableFuture<CommunicationAccessToken> accessTokenFuture : accessTokenFutures) {
            accessTokenResults.add(accessTokenFuture.get());
        }

        assertEquals(1, accessTokenResults.size());
        assertEquals(tokenString, accessTokenResults.iterator().next().getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenPastThresholdRefreshed_singleCall() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(120);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));
        CommunicationAccessToken accessToken = credential.getToken().get();

        assertNotEquals(tokenString, accessToken.getToken());
        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenPastThresholdRefreshed_exception() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(120);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        RuntimeException mockTokenRefresherException = new RuntimeException("Mock Token Refresh Exception");
        mockTokenRefresher.setOnCallReturn(() -> {
            throw mockTokenRefresherException;
        });
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));

        ExecutionException thrown = assertThrows(ExecutionException.class,
            () -> {
                try {
                    credential.getToken().get();
                } finally {
                    assertEquals(1, mockTokenRefresher.getCallCount());
                }
            });

        assertTrue(thrown.getMessage().contains(mockTokenRefresherException.getMessage()));
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenPastThresholdRefreshed_multipleCalls() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(120);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));

        Set<CommunicationAccessToken> accessTokens = new HashSet<>();
        int numCalls = 3;
        for (int i = 0; i < numCalls; i++) {
            CommunicationAccessToken accessToken = credential.getToken().get();
            accessTokens.add(accessToken);
        }

        assertEquals(1, accessTokens.size());
        assertNotEquals(tokenString, accessTokens.iterator().next().getToken());
        assertEquals(refreshedToken, accessTokens.iterator().next().getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenPastThresholdRefreshed_multithreadedCalls() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(120);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));

        // Set up blocked multithreaded calls
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        int numCalls = 5;
        List<CompletableFuture<CommunicationAccessToken>> accessTokenFutures = new ArrayList<>();
        for (int i = 0; i < numCalls; i++) {
            accessTokenFutures.add(credential.getToken());
        }

        // Unblock refresh and wait for results
        blockedRefresh.run();
        Set<CommunicationAccessToken> accessTokenResults = new HashSet<>();
        for (CompletableFuture<CommunicationAccessToken> accessTokenFuture : accessTokenFutures) {
            accessTokenResults.add(accessTokenFuture.get());
        }

        assertEquals(1, accessTokenResults.size());
        assertNotEquals(tokenString, accessTokenResults.iterator().next().getToken());
        assertEquals(refreshedToken, accessTokenResults.iterator().next().getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_isDisposed_cancelledFuture() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false));

        credential.dispose();
        CompletableFuture<CommunicationAccessToken> accessTokenFuture = credential.getToken();

        assertTrue(accessTokenFuture.isDone());
        ExecutionException executionException = assertThrows(ExecutionException.class,
            () -> {
                accessTokenFuture.get();
            });
        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof IllegalStateException);
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
    public void dispose_onDemandAutoRefresh_inProgressCancelled() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(120);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(mockTokenRefresher, false, tokenString));
        CompletableFuture<CommunicationAccessToken> accessTokenFuture = credential.getToken();
        credential.dispose();
        blockedRefresh.run();

        assertTrue(accessTokenFuture.isDone());

        assertThrows(IllegalStateException.class,
            () -> {
                accessTokenFuture.get();
            });
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