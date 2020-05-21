package com.azure.android.core.credential;

import org.junit.Test;
import org.threeten.bp.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccessTokenTest {
    private String token = "testToken";
    private OffsetDateTime time; // Expiration is set to 2 minutes before the given time.
    private AccessToken accessToken;

    @Test
    public void constructor() {
        time = OffsetDateTime.now();
        accessToken = new AccessToken(token, time);

        assertEquals(token, accessToken.getToken());
        assertEquals(time.minusMinutes(2), accessToken.getExpiresAt());
    }

    @Test
    public void token_isExpired() {
        time = OffsetDateTime.now().plusMinutes(3);
        accessToken = new AccessToken(token, time);

        assertFalse(accessToken.isExpired());
    }

    @Test
    public void token_isNotExpired() {
        time = OffsetDateTime.now();
        accessToken = new AccessToken(token, time);

        assertTrue(accessToken.isExpired());
    }
}
