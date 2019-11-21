// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;

/**
 * Custom serializer for serializing {@code HttpHeaders} objects into Base64 strings.
 */
final class HttpHeadersSerializer extends JsonSerializer<Headers> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Headers.class, new HttpHeadersSerializer());
        return module;
    }

    @Override
    public void serialize(Headers value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {
        final Map<String, String> result = new HashMap<>();
        for (String headerName : value.names()) {
            result.put(headerName, value.get(headerName));
        }
        jgen.writeObject(result);
    }
}
