package com.azure.android.communication.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;

public class StaticUserCredentialTests {

    @Test()
    public void constructor_shouldStoreAnyValidToken() throws ExecutionException, InterruptedException {
        long expiryEpochSecond = System.currentTimeMillis() / 1000 + 60;
        String sampleToken = TokenStubHelper.createTokenString(expiryEpochSecond);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(sampleToken);

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(sampleToken, accessToken.getToken());
        assertEquals(expiryEpochSecond, accessToken.getExpiresAt().toEpochSecond());
        assertFalse(accessToken.isExpired());
    }

    @Test()
    public void constructor_shouldStoreExpiredValidToken() throws ExecutionException, InterruptedException {
        long expiryEpochSecond = System.currentTimeMillis() / 1000 - 60;
        String sampleToken = TokenStubHelper.createTokenString(expiryEpochSecond);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(sampleToken);

        CommunicationAccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(sampleToken, accessToken.getToken());
        assertEquals(expiryEpochSecond, accessToken.getExpiresAt().toEpochSecond());
        assertTrue(accessToken.isExpired());
    }

    @Test()
    public void constructor_shouldThrowForAnyInvalidToken() {
        String [] invalidTokens = {"foo", "foo.bar", "foo.bar.foobar"};
        for (String invalidToken: invalidTokens) {
            assertThrows(IllegalArgumentException.class,
                () -> { new CommunicationTokenCredential(invalidToken);
                });
        }
    }

    @Test
    public void dispose_shouldCancelCompletableFuture() {
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

}
