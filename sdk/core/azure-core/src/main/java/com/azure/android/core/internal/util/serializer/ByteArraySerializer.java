// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * Custom serializer for serializing {@code Byte[]} objects into Base64 strings.
 */
final class ByteArraySerializer extends JsonSerializer<Byte[]> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson {@link ObjectMapper}.
     *
     * @return A simple module to be plugged onto Jackson {@link ObjectMapper}.
     */
    static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Byte[].class, new ByteArraySerializer());

        return module;
    }

    @Override
    public void serialize(Byte[] value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        byte[] bytes = new byte[value.length];

        for (int i = 0; i < value.length; i++) {
            bytes[i] = value[i];
        }

        jsonGenerator.writeBinary(bytes);
    }
}
