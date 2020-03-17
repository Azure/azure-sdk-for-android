// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.junit.BeforeClass;
import org.junit.Test;

import static com.azure.android.core.util.Base64Stub.DECODED_BYTES;
import static com.azure.android.core.util.Base64Stub.ENCODED_BYTES;
import static com.azure.android.core.util.Base64Stub.ENCODED_STRING;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Base64UtilTest {
    @BeforeClass
    public static void setUp() {
        Base64Util.setBase64Wrapper(new Base64Stub());
    }

    @Test
    public void encode_byteArray() {
        assertArrayEquals(ENCODED_BYTES, Base64Util.encode(DECODED_BYTES));
    }

    @Test
    public void decode_byteArray() {
        assertArrayEquals(DECODED_BYTES, Base64Util.decode(ENCODED_BYTES));
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
        assertEquals(ENCODED_STRING, Base64Util.encodeToString(DECODED_BYTES));
    }

    @Test
    public void decode_string() {
        assertArrayEquals(DECODED_BYTES, Base64Util.decodeString(ENCODED_STRING));
    }

    @Test
    public void decode_nullString() {
        assertNull(Base64Util.decodeString(null));
    }

    @Test
    public void encodeUrlWithoutPadding() {
        assertNull(Base64Util.encodeUrlWithoutPadding(null));
    }

    @Test
    public void encodeUrlWithoutPadding_usingNullValue() {
        assertNull(Base64Util.encodeUrlWithoutPadding(null));
    }

    @Test
    public void decodeUrl_fromNullByteArray() {
        assertNull(Base64Util.decodeUrl((String) null));
    }

    @Test
    public void decodeUrl_fromNullString() {
        assertNull(Base64Util.decodeUrl((String) null));
    }
}
