package com.azure.android.core.http;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ResponseBaseTest {
    @Test
    public void test_getDeserializedHeaders() {
        Map<String, String> deserializedHeaders = new HashMap<>();
        deserializedHeaders.put("First-Header", "First Value");
        deserializedHeaders.put("Second-Header", "Second Value");

        ResponseBase<Map<String, String>, String> responseBase =
            new ResponseBase<>(null, 0, null, null, deserializedHeaders);

        Map<String, String> responseHeaders = responseBase.getDeserializedHeaders();

        assertEquals(2, responseHeaders.size());
        assertEquals("First Value", responseHeaders.get("First-Header"));
        assertEquals("Second Value", responseHeaders.get("Second-Header"));
    }
}
