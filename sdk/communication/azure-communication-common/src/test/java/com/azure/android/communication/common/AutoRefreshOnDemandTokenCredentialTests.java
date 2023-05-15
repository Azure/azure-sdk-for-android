package com.azure.android.communication.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

public class AutoRefreshOnDemandTokenCredentialTests extends TokenCredentialBaseTest {

    private final int REFRESH_THRESHOLD_SECS = 130;
    private final int EXPIRING_OFFSET_SECONDS = 120;


    @Test()
    public void constructor_shouldNotRefreshWithInitialTokenWithinThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(initialToken, accessToken.getToken());
        assertFalse(accessToken.isExpired());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test()
    public void constructor_shouldNotRefreshWithExpiredTillGetTokenCall() {
        String expiredToken = TokenStubHelper.createTokenStringForOffset(-REFRESH_THRESHOLD_SECS);
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
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
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
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        assertForMultithreadedCalls(tokenCredential, refreshedToken, 1);
    }

    @Test()
    public void getToken_shouldThrowIfRefresherReturnsExpiredToken() {
        String expiredToken = TokenStubHelper.createTokenStringForOffset(-REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(expiredToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        assertRefresherThrowsException(tokenCredential, 1, expiredTokenExceptionMessage);
    }

    @Test()
    public void getToken_refresherThrowsWithoutInitialToken() {
        mockTokenRefresherToThrowException();

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        assertRefresherThrowsException(tokenCredential, 1, mockedExceptionMessage);
    }

    @Test()
    public void getToken_shouldRefreshWithExpiredToken() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(-REFRESH_THRESHOLD_SECS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(refreshedToken, accessToken.getToken());
        assertFalse(accessToken.isExpired());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test()
    public void getToken_shouldRefreshWithExpiredToken_multithreadedCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(-REFRESH_THRESHOLD_SECS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertForMultithreadedCalls(tokenCredential, refreshedToken, 1);
    }

    @Test
    public void getToken_shouldNotRefreshWithTokenWithinThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(initialToken, accessToken.getToken());
        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_shouldNotRefreshWithTokenWithinThreshold_multithreadedCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertForMultithreadedCalls(tokenCredential, initialToken, 0);
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        CommunicationAccessToken accessToken = credential.getToken().get();

        assertNotEquals(initialToken, accessToken.getToken());
        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_RefresherThrowsWithTokenPastThreshold() {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        mockTokenRefresherToThrowException();

        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        assertRefresherThrowsException(credential, 1, mockedExceptionMessage);
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold_multipleCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
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
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
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
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
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
}
