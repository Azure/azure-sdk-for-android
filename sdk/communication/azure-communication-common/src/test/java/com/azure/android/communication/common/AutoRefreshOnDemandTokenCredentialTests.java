package com.azure.android.communication.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

public class AutoRefreshOnDemandTokenCredentialTests {

    private MockTokenRefresher mockTokenRefresher;
    private CountDownLatch countDownLatch;

    @BeforeEach
    public void setup(){
        mockTokenRefresher = new MockTokenRefresher();
        countDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(countDownLatch::countDown);
    }

    @Test()
    public void constructor_shouldNotRefreshWithInitialTokenWithinThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(130);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(initialToken, accessToken.getToken());
        assertFalse(accessToken.isExpired());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test()
    public void constructor_shouldNotRefreshWithExpiredTillGetTokenCall() throws ExecutionException, InterruptedException {
        String expiredToken = TokenStubHelper.createTokenStringForOffset(-120);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(expiredToken));
        countDownLatch.countDown();
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    private CommunicationTokenRefreshOptions createRefreshOptions(String token){
        return new CommunicationTokenRefreshOptions(mockTokenRefresher)
            .setRefreshProactively(false)
            .setInitialToken(token);
    }

    @Test()
    public void constructor_shouldThrowForAnyInvalidToken() {
        String [] invalidTokens = {"foo", "foo.bar", "foo.bar.foobar"};
        for (String invalidToken: invalidTokens) {
            assertThrows(IllegalArgumentException.class,
                () -> { new CommunicationTokenCredential(createRefreshOptions(invalidToken));
                });
        }
    }

    @Test()
    public void getToken_shouldRefreshWithoutInitialToken() throws ExecutionException, InterruptedException {
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(130);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        countDownLatch.await();

        assertEquals(refreshedToken, accessToken.getToken());
        assertFalse(accessToken.isExpired());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test()
    public void getToken_shouldRefreshWithoutInitialToken_multithreadedSameResult() throws ExecutionException, InterruptedException {
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(130);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        assertForMultithreadedCalls(tokenCredential, refreshedToken, 1);
    }

    @Test()
    public void getToken_shouldThrowIfRefresherReturnsExpiredToken() {
        String expiredToken = TokenStubHelper.createTokenStringForOffset(-130);
        mockTokenRefresher.setToken(expiredToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        ExecutionException thrown = assertThrows(ExecutionException.class,
            () -> {
                try {
                    tokenCredential.getToken().get();
                } finally {
                    assertEquals(1, mockTokenRefresher.getCallCount());
                }
            });

        assertTrue(thrown.getMessage().contains("The token returned from the tokenRefresher is expired."));
    }

    @Test()
    public void getToken_refresherThrowsWithoutInitialToken() {
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(600);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        assertForRefresherThrowsException(tokenCredential);
    }

    private void assertForRefresherThrowsException(CommunicationTokenCredential tokenCredential){
        RuntimeException mockTokenRefresherException = new RuntimeException("Mock Token Refresh Exception");
        mockTokenRefresher.setOnCallReturn(() -> {
            throw mockTokenRefresherException;
        });

        ExecutionException thrown = assertThrows(ExecutionException.class,
            () -> {
                try {
                    tokenCredential.getToken().get();
                } finally {
                    assertEquals(1, mockTokenRefresher.getCallCount());
                }
            });

        assertTrue(thrown.getMessage().contains(mockTokenRefresherException.getMessage()));
    }

    @Test()
    public void getToken_shouldRefreshWithExpiredToken() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(-120);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(130);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(refreshedToken, accessToken.getToken());
        assertFalse(accessToken.isExpired());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test()
    public void getToken_shouldRefreshWithExpiredToken_multithreadedCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(-120);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(130);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertForMultithreadedCalls(tokenCredential, refreshedToken, 1);
    }

    @Test
    public void getToken_shouldNotRefreshWithTokenWithinThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(130);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(initialToken, accessToken.getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_shouldNotRefreshWithTokenWithinThreshold_multithreadedCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(130);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertForMultithreadedCalls(tokenCredential, initialToken, 0);
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(120);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        CommunicationAccessToken accessToken = credential.getToken().get();

        assertNotEquals(initialToken, accessToken.getToken());
        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_RefresherThrowsWithTokenPastThreshold() {
        String initialToken = TokenStubHelper.createTokenStringForOffset(120);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertForRefresherThrowsException(credential);
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold_multipleCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(120);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        Set<CommunicationAccessToken> accessTokens = new HashSet<>();
        int numCalls = 3;
        for (int i = 0; i < numCalls; i++) {
            CommunicationAccessToken accessToken = tokenCredential.getToken().get();
            accessTokens.add(accessToken);
        }

        assertEquals(1, accessTokens.size());
        assertNotEquals(initialToken, accessTokens.iterator().next().getToken());
        assertEquals(refreshedToken, accessTokens.iterator().next().getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold_multithreadedCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(120);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertForMultithreadedCalls(tokenCredential, refreshedToken, 1);
    }

    @Test
    public void dispose_shouldCancelCompletableFuture(){
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        tokenCredential.dispose();
        CompletableFuture<CommunicationAccessToken> accessTokenFuture = tokenCredential.getToken();

        assertTrue(accessTokenFuture.isDone());
        ExecutionException executionException = assertThrows(ExecutionException.class,
            () -> {
                accessTokenFuture.get();
            });
        assertNotNull(executionException.getCause());
        assertTrue(executionException.getCause() instanceof IllegalStateException);
    }

    @Test
    public void dispose_shouldCancelInProgressCompletableFuture(){
        String initialToken = TokenStubHelper.createTokenStringForOffset(120);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(300);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        CompletableFuture<CommunicationAccessToken> accessTokenFuture = tokenCredential.getToken();
        tokenCredential.dispose();
        blockedRefresh.run();

        assertTrue(accessTokenFuture.isDone());

        assertThrows(IllegalStateException.class,
            () -> {
                accessTokenFuture.get();
            });
    }

    private void assertForMultithreadedCalls(CommunicationTokenCredential credential, String expectedToken, int expectedCalls) throws ExecutionException, InterruptedException {
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
