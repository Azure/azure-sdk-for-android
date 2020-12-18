// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.credential.AccessToken;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

public class CommunicationTokenCredentialTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test()
    public void constructor_withStaticInitialTokenInvalid() {
        expectedException.expect(is(instanceOf(IllegalArgumentException.class)));

        new CommunicationTokenCredential("This is an invalid token string");
    }

    @Test
    public void constructor_withTokenRefresher_withInitialTokenInvalid() {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();

        expectedException.expect(is(instanceOf(IllegalArgumentException.class)));
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, "This is an invalid token string");
        new CommunicationTokenCredential(options);
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_noInitialToken_refresh() throws InterruptedException, ExecutionException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(900);
        mockTokenRefresher.setToken(refreshedToken);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(countDownLatch::countDown);

        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);
        countDownLatch.await();

        AccessToken accessToken = credential.getToken().get();

        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_initialTokenPastThreshold_refresh() throws InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(600);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(countDownLatch::countDown);

        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true, tokenString);
        new CommunicationTokenCredential(options);
        countDownLatch.await();

        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_initialTokenWithinThreshold_notRefreshed() throws InterruptedException, ExecutionException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(700);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();

        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);
        AccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void constructor_withTokenRefresher_proactiveRefresh_withInitialTokenInvalid() {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        expectedException.expect(is(instanceOf(IllegalArgumentException.class)));
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true, "This is an invalid token string");
        new CommunicationTokenCredential(options);
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

        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true);
        new CommunicationTokenCredential(options);
        countDownLatch.await();

        assertEquals(numRepeats, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_staticInitialTokenActive() throws ExecutionException, InterruptedException {
        long expiryEpochSecond = System.currentTimeMillis() / 1000 + 60;
        String tokenString = TokenStubHelper.createTokenString(expiryEpochSecond);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(tokenString);
        AccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(expiryEpochSecond, accessToken.getExpiresAt().toEpochSecond());
        assertFalse(accessToken.isExpired());
    }

    @Test
    public void getToken_staticInitialTokenExpired() throws ExecutionException, InterruptedException {
        long expiryEpochSecond = System.currentTimeMillis() / 1000 - 60;
        String tokenString = TokenStubHelper.createTokenString(expiryEpochSecond);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(tokenString);
        AccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(expiryEpochSecond, accessToken.getExpiresAt().toEpochSecond());
        assertTrue(accessToken.isExpired());
    }

    @Test
    public void getToken_staticInitialToken_isDisposed_cancelledFuture() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(1200);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(tokenString);

        credential.dispose();
        Future<AccessToken> accessTokenFuture = credential.getToken();

        assertTrue(accessTokenFuture.isDone());
        assertTrue(accessTokenFuture.isCancelled());
        expectedException.expect(is(instanceOf(CancellationException.class)));
        accessTokenFuture.get();
    }

    @Test
    public void getToken_onDemandAutoRefresh_noInitialToken_firstFetch() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        AccessToken accessToken = credential.getToken().get();

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
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        try {
            expectedException.expectCause(is(mockTokenRefresherException));
            credential.getToken().get();
        } finally {
            assertEquals(1, mockTokenRefresher.getCallCount());
        }
    }

    @Test
    public void getToken_onDemandAutoRefresh_noInitialToken_firstFetch_multithreadedSameResult() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        // Set up blocked multithreaded calls
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        int numCalls = 5;
        List<Future<AccessToken>> accessTokenFutures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numCalls);
        for (int i = 0; i < numCalls; i++) {
            Future<AccessToken> accessTokenFuture = executorService.submit(() -> credential.getToken().get());
            accessTokenFutures.add(accessTokenFuture);
        }

        // Unblock refresh and wait for results
        blockedRefresh.run();
        Set<AccessToken> accessTokenResults = new HashSet<>();
        for (Future<AccessToken> accessTokenFuture : accessTokenFutures) {
            accessTokenResults.add(accessTokenFuture.get());
        }
        executorService.shutdown();

        assertEquals(1, accessTokenResults.size());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenWithinThresholdNotRefreshed_singleCall() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(130);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        AccessToken accessToken = credential.getToken().get();

        assertEquals(tokenString, accessToken.getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenWithinThresholdNotRefreshed_multithreadedCalls() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(130);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        // Set up blocked multithreaded calls
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        int numCalls = 5;
        List<Future<AccessToken>> accessTokenFutures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numCalls);
        for (int i = 0; i < numCalls; i++) {
            Future<AccessToken> accessTokenFuture = executorService.submit(() -> credential.getToken().get());
            accessTokenFutures.add(accessTokenFuture);
        }

        // Unblock refresh and wait for results
        blockedRefresh.run();
        Set<AccessToken> accessTokenResults = new HashSet<>();
        for (Future<AccessToken> accessTokenFuture : accessTokenFutures) {
            accessTokenResults.add(accessTokenFuture.get());
        }
        executorService.shutdown();

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
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);
        AccessToken accessToken = credential.getToken().get();

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
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        try {
            expectedException.expectCause(is(mockTokenRefresherException));
            credential.getToken().get();
        } finally {
            assertEquals(1, mockTokenRefresher.getCallCount());
        }
    }

    @Test
    public void getToken_onDemandAutoRefresh_tokenPastThresholdRefreshed_multipleCalls() throws ExecutionException, InterruptedException {
        String tokenString = TokenStubHelper.createTokenStringForOffset(120);
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        Set<AccessToken> accessTokens = new HashSet<>();
        int numCalls = 3;
        for (int i = 0; i < numCalls; i++) {
            AccessToken accessToken = credential.getToken().get();
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
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        // Set up blocked multithreaded calls
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        int numCalls = 5;
        List<Future<AccessToken>> accessTokenFutures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numCalls);
        for (int i = 0; i < numCalls; i++) {
            Future<AccessToken> accessTokenFuture = executorService.submit(() -> credential.getToken().get());
            accessTokenFutures.add(accessTokenFuture);
        }

        // Unblock refresh and wait for results
        blockedRefresh.run();
        Set<AccessToken> accessTokenResults = new HashSet<>();
        for (Future<AccessToken> accessTokenFuture : accessTokenFutures) {
            accessTokenResults.add(accessTokenFuture.get());
        }
        executorService.shutdown();

        assertEquals(1, accessTokenResults.size());
        assertNotEquals(tokenString, accessTokenResults.iterator().next().getToken());
        assertEquals(refreshedToken, accessTokenResults.iterator().next().getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_onDemandAutoRefresh_isDisposed_cancelledFuture() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        credential.dispose();
        Future<AccessToken> accessTokenFuture = credential.getToken();

        assertTrue(accessTokenFuture.isDone());
        assertTrue(accessTokenFuture.isCancelled());
        expectedException.expect(is(instanceOf(CancellationException.class)));
        accessTokenFuture.get();
    }

    @Test
    public void getToken_whileProactiveRefresh_singleResult() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(1200);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);

        Future<AccessToken> accessTokenFuture = credential.getToken();
        blockedRefresh.run();
        AccessToken accessToken = accessTokenFuture.get();

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

        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, false, tokenString);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);
        Future<AccessToken> accessTokenFuture = credential.getToken();
        credential.dispose();
        blockedRefresh.run();

        assertTrue(accessTokenFuture.isCancelled());
        assertTrue(accessTokenFuture.isDone());

        expectedException.expect(is(instanceOf(CancellationException.class)));
        accessTokenFuture.get();
    }

    @Test
    public void dispose_proactiveRefresh_inProgressCancelled() throws ExecutionException, InterruptedException {
        MockTokenRefresher mockTokenRefresher = new MockTokenRefresher();
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(1200);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);

        CommunicationTokenRefreshOptions options = new CommunicationTokenRefreshOptions(
            mockTokenRefresher, true);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(options);
        Future<AccessToken> accessTokenFuture = credential.getToken();
        credential.dispose();
        blockedRefresh.run();

        assertTrue(accessTokenFuture.isCancelled());
        assertTrue(accessTokenFuture.isDone());

        expectedException.expect(is(instanceOf(CancellationException.class)));
        accessTokenFuture.get();
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
