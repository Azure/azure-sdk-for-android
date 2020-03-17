// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class ByteArraySerializerTest {
    @Test
    public void test_getModule() {
        SimpleModule module = ByteArraySerializer.getModule();

        assertNotNull(module);
    }

    @Test
    public void test_serialize() throws IOException {
        byte[] bytes = "Test string".getBytes();
        Byte[] byteObjects = new Byte[bytes.length];

        for(int i = 0; i < bytes.length; i++) {
            byteObjects[i] = bytes[i];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
        ByteArraySerializer byteArraySerializer = new ByteArraySerializer();

        byteArraySerializer.serialize(byteObjects, jsonGenerator, null);
        jsonGenerator.flush();

        byte[] serializedBytes = outputStream.toByteArray();

        // "VGVzdCBzdHJpbmc="
        assertArrayEquals(new byte[] { 34, 86, 71, 86, 122, 100, 67, 66, 122, 100, 72, 74, 112, 98, 109, 99, 61, 34 },
            serializedBytes);
    }
}
