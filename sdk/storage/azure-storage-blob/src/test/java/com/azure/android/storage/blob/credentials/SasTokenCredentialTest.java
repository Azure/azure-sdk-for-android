// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.credentials;

import com.azure.android.storage.blob.credential.SasTokenCredential;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SasTokenCredentialTest {
    private final String sasTokenWithQuestionMark = "?sig=signature&key=value";
    private final String sasTokenWithoutQuestionMark = "sig=signature&key=value";

    @Test
    public void constructor_withStringWithQuestionMark() {
        // Given a SAS token string that begins with a question mark.

        // When creating a SasTokenCredential object based on it using the constructor.
        SasTokenCredential sasTokenCredential = new SasTokenCredential(sasTokenWithoutQuestionMark);

        // Then the token stored in the credential object will not include said question mark.
        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void constructor_withStringWithoutQuestionMark() {
        // Given a SAS token string that does not begin with a question mark.

        // When creating a SasTokenCredential object based on it using the constructor.
        SasTokenCredential sasTokenCredential = new SasTokenCredential(sasTokenWithQuestionMark);

        // Then the token stored in the credential object wil not have been modified.
        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromSasTokenString_withStringWithQuestionMark() {
        // Given a SAS token string that begins with a question mark.

        // When creating a SasTokenCredential object based on it using fromSasTokenString().
        SasTokenCredential sasTokenCredential = SasTokenCredential.fromSasTokenString(sasTokenWithQuestionMark);

        // Then the token stored in the credential object will not include said question mark.
        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromSasTokenString_withStringWithoutQuestionMark() {
        // Given a SAS token string that does not begin with a question mark.

        // When creating a SasTokenCredential object based on it using fromSasTokenString().
        SasTokenCredential sasTokenCredential = SasTokenCredential.fromSasTokenString(sasTokenWithoutQuestionMark);

        // Then the token stored in the credential object will not include said question mark.
        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromSasTokenString_withNullString() {
        // Given a null SAS token.

        // When creating a SasTokenCredential object based on it using fromSasTokenString().

        // Then the result will be null.
        assertNull(SasTokenCredential.fromSasTokenString(null));
    }

    @Test
    public void fromSasTokenString_withEmptyString() {
        // Given an empty SAS token string.

        // When creating a SasTokenCredential object based on it using fromSasTokenString().

        // Then the result will be null.
        assertNull(SasTokenCredential.fromSasTokenString(""));
    }

    @Test
    public void fromQueryParameters() {
        // Given a map of query parameters that would form a SAS token string.
        Map<String, String> map = new HashMap<>();
        map.put("sig", "signature");
        map.put("key", "value");

        // When creating a SasTokenCredential object based on it using fromQueryParameters().
        SasTokenCredential sasTokenCredential = SasTokenCredential.fromQueryParameters(map);

        // Then a token will be stored in the resulting credential object not including a question mark.
        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromQueryParameters_withNullParameters() {
        // Given null reference for a map of query parameters that would form a SAS token string.

        // When creating a SasTokenCredential object based on it using fromQueryParameters().

        // Then the result will be null.
        assertNull(SasTokenCredential.fromQueryParameters(null));
    }

    @Test
    public void fromQueryParameters_withEmptyParameters() {
        // Given an empty map of query parameters that would form a SAS token string.

        // When creating a SasTokenCredential object based on it using fromQueryParameters().

        // Then the result will be null.
        assertNull(SasTokenCredential.fromQueryParameters(new HashMap<>()));
    }

    @Test
    public void fromQueryParameters_withoutSignature() {
        // Given a map of query parameters that would form a SAS token string, that do not include a signature.
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        // When creating a SasTokenCredential object based on it using fromQueryParameters().

        // Then the result will be null.
        assertNull(SasTokenCredential.fromQueryParameters(map));
    }
}
