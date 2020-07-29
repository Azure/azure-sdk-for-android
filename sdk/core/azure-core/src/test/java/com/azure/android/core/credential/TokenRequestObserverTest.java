package com.azure.android.core.credential;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TokenRequestObserverTest {
    private final List<String> scopes = new ArrayList<>(Arrays.asList("testScope1", "testScope2"));
    private TokenRequestHandle tokenRequestHandle;

    @Before
    public void setUp() {
        tokenRequestHandle = new TokenRequestHandle(scopes);
    }

    @Test
    public void onChanged_whereHandleIsNotConsumed() {
        TokenRequestObserver tokenRequestObserver = new TokenRequestObserver() {
            @Override
            public void onTokenRequest(String[] scopes, TokenResponseCallback callback) {
                assertEquals("testScope1", scopes[0]);
                assertEquals("testScope2", scopes[1]);
            }
        };

        tokenRequestObserver.onChanged(tokenRequestHandle);
    }

    @Test
    public void onChanged_whereHandleIsConsumed() {
        tokenRequestHandle.isConsumed();

        TokenRequestObserver tokenRequestObserver = new TokenRequestObserver() {
            @Override
            public void onTokenRequest(String[] scopes, TokenResponseCallback callback) {
                fail("Should not enter onTokenRequest if the TokenRequestHandle has already been consumed.");
            }
        };

        tokenRequestObserver.onChanged(tokenRequestHandle);
    }
}
