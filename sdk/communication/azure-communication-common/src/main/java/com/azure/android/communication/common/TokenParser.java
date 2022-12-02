// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import android.util.Base64;

import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility for Handling Access Tokens.
 */
final class TokenParser {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    TokenParser() {
    }

    /**
     * Create AccessToken object from Token string
     *
     * @param tokenStr token string
     * @return AccessToken instance
     */
    static CommunicationAccessToken createAccessToken(String tokenStr) {
        try {
            if (tokenStr == null) {
                throw new NullPointerException("'tokenStr' cannot be null.");
            }
            String[] tokenParts = tokenStr.split("\\.");
            String tokenPayload = tokenParts[1];
            byte[] decodedBytes = Base64.decode(tokenPayload, Base64.DEFAULT);
            String decodedPayloadJson = new String(decodedBytes, Charset.forName("UTF-8"));

            ObjectNode payloadObj = JSON_MAPPER.readValue(decodedPayloadJson, ObjectNode.class);
            long expire = payloadObj.get("exp").longValue();
            OffsetDateTime offsetExpiry = OffsetDateTime.ofInstant(Instant.ofEpochMilli(expire * 1000),
                ZoneId.of("UTC"));

            return new CommunicationAccessToken(tokenStr, offsetExpiry);
        } catch (Exception e) {
            throw new IllegalArgumentException("'tokenStr' is not a valid token string", e);
        }
    }
}