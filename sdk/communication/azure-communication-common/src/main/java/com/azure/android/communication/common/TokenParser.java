// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import com.azure.android.core.credential.AccessToken;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility for Handling Access Tokens.
 */
final class TokenParser {
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private TokenParser() {
        // Empty constructor to prevent instantiation of this class.
    }

    /**
     * Create AccessToken object from Token string
     *
     * @param tokenStr token string
     * @return AccessToken instance
     */
    static AccessToken createAccessToken(String tokenStr) {
        try {
            Objects.requireNonNull(tokenStr, "'tokenStr' cannot be null.");
            String[] tokenParts = tokenStr.split("\\.");
            String tokenPayload = tokenParts[1];
            byte[] decodedBytes = Base64.decode(tokenPayload, Base64.DEFAULT);
            String decodedPayloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            ObjectNode payloadObj = jsonMapper.readValue(decodedPayloadJson, ObjectNode.class);
            long expire = payloadObj.get("exp").longValue();
            OffsetDateTime offsetExpiry = OffsetDateTime.ofInstant(Instant.ofEpochMilli(expire * 1000), ZoneId.of("UTC"));

            return new AccessToken(tokenStr, offsetExpiry);
        } catch (Exception e) {
            throw new IllegalArgumentException("'tokenStr' is not a valid token string", e);
        }
    }
}


