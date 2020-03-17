// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.azure.android.core.util.Base64Url;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Base64UrlSerializerTest {
    private static String TEST_URL = "http://127.0.0.1/";

    @Test
    public void test_getModule() {
        SimpleModule module = Base64UrlSerializer.getModule();

        assertNotNull(module);
    }

    @Test
    public void serialize_withNoCharactersToEscape() throws IOException {
        Base64Url base64Url = new Base64Url(TEST_URL);
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        Base64UrlSerializer base64UrlSerializer = new Base64UrlSerializer();

        base64UrlSerializer.serialize(base64Url, jsonGenerator, null);
        jsonGenerator.flush();

        assertEquals("\"" + TEST_URL + "\"", outputStream.toString());
    }

    @Test
    public void serialize_withCharactersToEscape() throws IOException {
        Base64Url base64Url = new Base64Url(TEST_URL + "\\");
        StringBuilderOutputStream outputStream = new StringBuilderOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        Base64UrlSerializer base64UrlSerializer = new Base64UrlSerializer();

        base64UrlSerializer.serialize(base64Url, jsonGenerator, null);
        jsonGenerator.flush();

        assertEquals("\"" + TEST_URL + "\\\\\"", outputStream.toString());
    }
}
