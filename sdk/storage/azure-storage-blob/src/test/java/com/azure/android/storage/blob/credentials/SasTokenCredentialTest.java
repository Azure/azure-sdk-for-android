package com.azure.android.storage.blob.credentials;

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
        SasTokenCredential sasTokenCredential = new SasTokenCredential(sasTokenWithoutQuestionMark);

        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void constructor_withStringWithoutQuestionMark() {
        SasTokenCredential sasTokenCredential = new SasTokenCredential(sasTokenWithQuestionMark);

        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromSasTokenString_withStringWithQuestionMark() {
        SasTokenCredential sasTokenCredential = SasTokenCredential.fromSasTokenString(sasTokenWithQuestionMark);

        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromSasTokenString_withStringWithoutQuestionMark() {
        SasTokenCredential sasTokenCredential = SasTokenCredential.fromSasTokenString(sasTokenWithoutQuestionMark);

        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromSasTokenString_withNullString() {
        assertNull(SasTokenCredential.fromSasTokenString(null));
    }

    @Test
    public void fromSasTokenString_withEmptyString() {
        assertNull(SasTokenCredential.fromSasTokenString(""));
    }

    @Test
    public void fromQueryParameters() {
        Map<String, String> map = new HashMap<>();
        map.put("sig", "signature");
        map.put("key", "value");

        SasTokenCredential sasTokenCredential = SasTokenCredential.fromQueryParameters(map);

        assertEquals(sasTokenWithoutQuestionMark, sasTokenCredential.getSasToken());
    }

    @Test
    public void fromQueryParameters_withNullParameters() {
        assertNull(SasTokenCredential.fromQueryParameters(null));
    }

    @Test
    public void fromQueryParameters_withEmptyParameters() {
        assertNull(SasTokenCredential.fromQueryParameters(new HashMap<>()));
    }

    @Test
    public void fromQueryParameters_withoutSignature() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        assertNull(SasTokenCredential.fromQueryParameters(map));
    }
}
