// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import android.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class TokenStubHelper {
    private static final String STUB_TOKEN_TEMPLATE =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.%s.adM-ddBZZlQ1WlN3pdPBOF5G4Wh9iZpxNP_fSvpF4cWs";

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    static String createTokenString(long expiryEpochSecond) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("exp", expiryEpochSecond);
        payload.put("jti", UUID.randomUUID());

        try {
            String payloadJson = jsonMapper.writeValueAsString(payload);
            String encodedPayload = Base64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            return String.format(STUB_TOKEN_TEMPLATE, encodedPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String createTokenStringForOffset(long secondsFromNow) {
        return createTokenString(System.currentTimeMillis() / 1000 + secondsFromNow);
    }
}


