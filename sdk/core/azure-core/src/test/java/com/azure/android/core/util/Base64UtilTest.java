// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import android.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Base64.class)
public class Base64UtilTest {
    private static final byte[] UNENCODED_BYTES =
        new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 };
    private static final byte[] ENCODED_BYTES =
        new byte[] { 81, 85, 70, 70, 81, 48, 70, 51, 85, 85, 90, 67, 90, 50, 78, 74, 81, 49, 69, 61 };
    private static final String ENCODED_STRING = "QUFFQ0F3UUZCZ2NJQ1E=";

    @Test
    public void encode_byteArray() {
        spy(Base64.class);
        when(Base64.encode(notNull(), anyInt())).thenReturn(ENCODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, Base64Util.encode(UNENCODED_BYTES));
    }

    @Test
    public void decode_byteArray() {
        spy(Base64.class);
        when(Base64.decode((byte[]) notNull(), anyInt())).thenReturn(UNENCODED_BYTES);

        assertArrayEquals(UNENCODED_BYTES, Base64Util.decode(ENCODED_BYTES));
    }

    @Test
    public void encode_nullByteArray() {
        assertNull(Base64Util.encode(null));
    }

    @Test
    public void decode_nullByteArray() {
        assertNull(Base64Util.decode(null));
    }

    @Test
    public void encode_toString() {
        spy(Base64.class);
        when(Base64.encodeToString(notNull(), anyInt())).thenReturn(ENCODED_STRING);

        assertEquals(ENCODED_STRING, Base64Util.encodeToString(UNENCODED_BYTES));
    }

    @Test
    public void decode_string() {
        spy(Base64.class);
        when(Base64.decode((String) notNull(), anyInt())).thenReturn(UNENCODED_BYTES);

        assertArrayEquals(UNENCODED_BYTES, Base64Util.decodeString(ENCODED_STRING));
    }

    @Test
    public void decode_nullString() {
        assertNull(Base64Util.decodeString(null));
    }

    @Test
    public void encodeUrlWithoutPadding() {
        assertNull(Base64Util.encodeURLWithoutPadding(null));
    }

    @Test
    public void encodeUrlWithoutPadding_usingNullValue() {
        assertNull(Base64Util.encodeURLWithoutPadding(null));
    }

    @Test
    public void decodeUrl_fromNullByteArray() {
        byte[] encodedContent = null;

        assertNull(Base64Util.decodeUrl(encodedContent));
    }

    @Test
    public void decodeUrl_fromNullString() {
        String encodedContent = null;

        assertNull(Base64Util.decodeUrl(encodedContent));
    }
}
