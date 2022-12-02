// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.common;

import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

public class JwtTokenMocker {

    public String generateRawToken(String resourceId, String userIdentity, int validForSeconds) {
        String skypeId = generateMockId(resourceId, userIdentity);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("skypeid", skypeId);
        OffsetDateTime expiresOnTimestamp = OffsetDateTime.now(ZoneId.of("UTC")).plusSeconds(validForSeconds);
        ZonedDateTime ldtUTC = expiresOnTimestamp.toZonedDateTime();
        long expSeconds = ldtUTC.toInstant().toEpochMilli() / 1000;
        builder.claim("exp", expSeconds);

        JWTClaimsSet claims =  builder.build();
        JWT idToken = new PlainJWT(claims);
        return idToken.serialize();
    }

    public String generateMockId(String resourceId, String userIdentity) {
        return "communication:" + resourceId + "." + userIdentity;
    }

}