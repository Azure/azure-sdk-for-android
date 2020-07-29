package com.azure.android.core.credential;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenRequestHandleTest {
    private final List<String> scopes = new ArrayList<>(Arrays.asList("testScope1", "testScope2"));
    private TokenRequestHandle tokenRequestHandle;

    @Before
    public void setUp() {
        tokenRequestHandle = new TokenRequestHandle(scopes);
    }

    @Test
    public void waitForToken() throws Throwable {
        AccessToken accessToken = new AccessToken("Token 1", OffsetDateTime.now());

        tokenRequestHandle.setToken(accessToken);

        assertEquals(accessToken, tokenRequestHandle.waitForToken(Duration.ofMillis(100)));
    }

    @Test(expected = RuntimeException.class)
    public void waitForToken_withError() throws Throwable {
        AccessToken accessToken = new AccessToken("Token 1", OffsetDateTime.now());

        tokenRequestHandle.setToken(accessToken);
        tokenRequestHandle.setError(new RuntimeException("Exception 1"));

        tokenRequestHandle.waitForToken(Duration.ofMillis(100));
    }

    @Test
    public void getScopes() {
        List<String> scopes = tokenRequestHandle.getScopes();

        assertEquals("testScope1", scopes.get(0));
        assertEquals("testScope2", scopes.get(1));
    }

    @Test
    public void handle_isConsumed() {
        assertFalse(tokenRequestHandle.isConsumed());
    }

    @Test
    public void handle_isNotConsumed() {
        tokenRequestHandle.isConsumed();
        assertTrue(tokenRequestHandle.isConsumed());
    }

    @Test
    public void setToken() {
        tokenRequestHandle.setToken(new AccessToken("Token 1", OffsetDateTime.now()));
    }

    @Test(expected = IllegalStateException.class)
    public void setToken_whenTokenHasBeenSet() {
        tokenRequestHandle.setToken(new AccessToken("Token 1", OffsetDateTime.now()));
        tokenRequestHandle.setToken(new AccessToken("Token 2", OffsetDateTime.now()));
    }

    @Test
    public void setError() {
        tokenRequestHandle.setError(new RuntimeException("Exception 1"));
    }

    @Test(expected = IllegalStateException.class)
    public void setError_whenErrorHasBeenSet() {
        tokenRequestHandle.setError(new RuntimeException("Exception 1"));
        tokenRequestHandle.setError(new RuntimeException("Exception 2"));
    }
}
