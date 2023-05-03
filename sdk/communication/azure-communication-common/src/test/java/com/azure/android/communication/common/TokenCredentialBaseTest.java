// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenCredentialBaseTest {

    protected String mockedExceptionMessage = "Mock Token Refresh Exception";
    protected String expiredTokenExceptionMessage = "The token returned from the tokenRefresher is expired.";
    protected MockTokenRefresher mockTokenRefresher;
    protected CountDownLatch countDownLatch;

    @BeforeEach
    public void setup(){
        mockTokenRefresher = new MockTokenRefresher();
        countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(countDownLatch::countDown);
    }

    protected void mockTokenRefresherToThrowException(){
        RuntimeException mockTokenRefresherException = new RuntimeException(mockedExceptionMessage);
        mockTokenRefresher.setOnCallReturn(() -> {
            throw mockTokenRefresherException;
        });
    }

    protected void assertRefresherThrowsException(CommunicationTokenCredential tokenCredential, int expectedCalls, String exceptionMessage){
        ExecutionException thrown = assertThrows(ExecutionException.class,
            () -> {
                try {
                    tokenCredential.getToken().get();
                } finally {
                    assertEquals(expectedCalls, mockTokenRefresher.getCallCount());
                }
            });

        assertTrue(thrown.getMessage().contains(exceptionMessage));
    }

    protected void assertForMultithreadedCalls(CommunicationTokenCredential credential, String expectedToken, int expectedCalls) throws ExecutionException, InterruptedException {
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
        assertEquals(expectedToken, accessTokenResults.iterator().next().getToken());
        assertEquals(expectedCalls, mockTokenRefresher.getCallCount());
    }

    protected Runnable arrangeBlockedRefresh(MockTokenRefresher mockTokenRefresher) {
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
