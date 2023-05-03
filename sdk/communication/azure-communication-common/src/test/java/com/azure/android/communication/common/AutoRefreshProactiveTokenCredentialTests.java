package com.azure.android.communication.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import java9.util.concurrent.CompletableFuture;

public class AutoRefreshProactiveTokenCredentialTests extends TokenCredentialBaseTest {
    private final int REFRESH_THRESHOLD_SECS = 700;
    private final int EXPIRING_OFFSET_SECONDS = 601;

    @Test()
    public void constructor_shouldNotRefreshWithInitialTokenWithinThreshold() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        countDownLatch.await(1, TimeUnit.SECONDS);

        assertEquals(0, mockTokenRefresher.getCallCount());
    }

    @Test()
    public void constructor_shouldRefreshWithExpiredToken() throws InterruptedException {
        String expiredToken = TokenStubHelper.createTokenStringForOffset(-120);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(expiredToken));
        countDownLatch.await();
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    private CommunicationTokenRefreshOptions createRefreshOptions(String token){
        return new CommunicationTokenRefreshOptions(mockTokenRefresher)
            .setRefreshProactively(true)
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
    public void getToken_shouldThrowIfRefresherReturnsExpiredToken() throws InterruptedException {
        String expiredToken = TokenStubHelper.createTokenStringForOffset(-130);
        mockTokenRefresher.setToken(expiredToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        countDownLatch.await(1, TimeUnit.SECONDS);

        assertRefresherThrowsException(tokenCredential, 1, expiredTokenExceptionMessage);
    }

    @Test()
    public void getToken_refresherThrowsNoInitialToken() throws InterruptedException {
        mockTokenRefresherToThrowException();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
        countDownLatch.await(1, TimeUnit.SECONDS);

        assertRefresherThrowsException(tokenCredential, 2, mockedExceptionMessage);
    }

    @Test()
    public void getToken_shouldRefreshWithExpiredToken() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(-120);
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
        String initialToken = TokenStubHelper.createTokenStringForOffset(-120);
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
        countDownLatch.await();

        assertEquals(1, mockTokenRefresher.getCallCount());
        CommunicationAccessToken accessToken = credential.getToken().get();
        assertEquals(refreshedToken, accessToken.getToken());
    }

    @Test
    public void getToken_RefresherThrowsWithTokenPastThreshold() throws InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        mockTokenRefresherToThrowException();
        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        countDownLatch.await(1, TimeUnit.SECONDS);
        assertRefresherThrowsException(credential, 2, mockedExceptionMessage);
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold_multipleCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        countDownLatch.await();

        Set<CommunicationAccessToken> accessTokens = new HashSet<>();
        int numCalls = 3;
        for (int i = 0; i < numCalls; i++) {
            CommunicationAccessToken accessToken = tokenCredential.getToken().get();
            accessTokens.add(accessToken);
        }

        assertEquals(1, accessTokens.size());
        assertEquals(refreshedToken, accessTokens.iterator().next().getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_shouldRefreshWithTokenPastThreshold_multithreadedCalls() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(REFRESH_THRESHOLD_SECS);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        countDownLatch.await();

        assertForMultithreadedCalls(tokenCredential, refreshedToken, 1);
    }

    @Test
    public void getToken_whileProactiveRefresh_singleResult() throws ExecutionException, InterruptedException {
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(1200);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);
        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(null));

        CompletableFuture<CommunicationAccessToken> accessTokenFuture = credential.getToken();
        blockedRefresh.run();
        CommunicationAccessToken accessToken = accessTokenFuture.get();

        assertEquals(refreshedToken, accessToken.getToken());
        assertEquals(1, mockTokenRefresher.getCallCount());
    }

    @Test
    public void getToken_refresherShouldBeCalledAgainAfterFirstRefreshCall() throws ExecutionException, InterruptedException {
        String initialToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS);
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(EXPIRING_OFFSET_SECONDS + 2);
        mockTokenRefresher.setToken(refreshedToken);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        countDownLatch.await();

        assertEquals(1, mockTokenRefresher.getCallCount());
        CommunicationAccessToken accessToken = credential.getToken().get();
        assertFalse(accessToken.isExpired(), "Refreshable AccessToken should not expire after refresh");

        CountDownLatch secondCountDownLatch = new CountDownLatch(1);
        mockTokenRefresher.setOnCallReturn(secondCountDownLatch::countDown);
        secondCountDownLatch.await();

        assertEquals(2, mockTokenRefresher.getCallCount());
        assertFalse(accessToken.isExpired(), "Refreshable AccessToken should not expire after refresh");
        assertEquals(refreshedToken, accessToken.getToken());
    }

    @Test
    public void getToken_fractionalBackoffAppliedWhenTokenExpiring() throws InterruptedException {
        int validForSecs = 8;
        double expectedTotalCallsTillLastSecond = Math.floor(Math.log(validForSecs));
        String initialToken = TokenStubHelper.createTokenStringForOffset(validForSecs);
        CommunicationAccessToken accessToken = TokenParser.createAccessToken(initialToken);
        mockTokenRefresher.setToken(initialToken);

        CommunicationTokenCredential credential = new CommunicationTokenCredential(createRefreshOptions(initialToken));
        countDownLatch.await();

        long tokenTtlSecs = 0;
        do {
            tokenTtlSecs = accessToken.getExpiresAt().toInstant().getEpochSecond() - Instant.now().getEpochSecond();
        } while (tokenTtlSecs > 1);
        credential.dispose();
        assertEquals(expectedTotalCallsTillLastSecond, mockTokenRefresher.getCallCount());
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
        String refreshedToken = TokenStubHelper.createTokenStringForOffset(1200);
        mockTokenRefresher.setToken(refreshedToken);
        Runnable blockedRefresh = this.arrangeBlockedRefresh(mockTokenRefresher);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(createRefreshOptions(null));
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
