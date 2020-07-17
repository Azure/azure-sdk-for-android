package com.azure.android.core.credential;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TokenRequestObservableTest {
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    @Test
    public void sendRequest() {
        List<String> scopes = new ArrayList<>();

        scopes.add("testScope1");
        scopes.add("testScope2");

        TokenRequestObservable tokenRequestObservable = new TokenRequestObservable();
        TokenRequestHandle tokenRequestHandle = tokenRequestObservable.sendRequest(scopes);
        List<String> scopesInHandle = tokenRequestHandle.getScopes();

        assertEquals("testScope1", scopesInHandle.get(0));
        assertEquals("testScope2", scopesInHandle.get(1));
    }
}
