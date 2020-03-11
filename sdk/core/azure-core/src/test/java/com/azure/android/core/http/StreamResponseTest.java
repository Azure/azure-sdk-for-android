package com.azure.android.core.http;

import org.junit.Test;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

import static org.junit.Assert.assertArrayEquals;

public class StreamResponseTest {
    @Test
    public void test_getBytes() throws IOException {
        ResponseBody responseBody = ResponseBody.create(MediaType.get("text/html"), "Test body");
        retrofit2.Response<ResponseBody> response = retrofit2.Response.success(responseBody);
        StreamResponse streamResponse = new StreamResponse(response);

        assertArrayEquals(new byte[] { 84, 101, 115, 116, 32, 98, 111, 100, 121 }, streamResponse.getBytes());
    }
}
