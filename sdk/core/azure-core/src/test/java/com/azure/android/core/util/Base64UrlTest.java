// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static com.azure.android.core.util.Base64Stub.DECODED_BYTES;
import static com.azure.android.core.util.Base64Stub.ENCODED_BYTES;
import static com.azure.android.core.util.Base64Stub.ENCODED_STRING;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Base64UrlTest {
    @BeforeClass
    public static void setUp() {
        Base64Util.setBase64Wrapper(new Base64Stub());
    }

    @Test
    public void constructor_withNullBytes() {
        final Base64Url base64Url = new Base64Url((byte[]) null);

        assertNull(base64Url.encodedBytes());
        assertNull(base64Url.decodedBytes());
        assertEmptyString(base64Url.toString());
    }

    @Test
    public void constructor_withEmptyBytes() {
        final Base64Url base64Url = new Base64Url(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptyBytes() {
        final Base64Url base64Url = new Base64Url(ENCODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals(ENCODED_STRING, base64Url.toString());
    }

    @Test
    public void constructor_withNullString() {
        final Base64Url base64Url = new Base64Url((String) null);

        assertNull(base64Url.encodedBytes());
        assertNull(base64Url.decodedBytes());
        assertEmptyString(base64Url.toString());
    }

    @Test
    public void constructor_withEmptyString() {
        final Base64Url base64Url = new Base64Url("");

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withEmptyDoubleQuotedString() {
        final Base64Url base64Url = new Base64Url("\"\"");

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withEmptySingleQuotedString() {
        final Base64Url base64Url = new Base64Url("\'\'");

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptyString() {
        final Base64Url base64Url = new Base64Url(ENCODED_STRING);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals(ENCODED_STRING, base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptyDoubleQuotedString() {
        final Base64Url base64Url = new Base64Url("\"" + ENCODED_STRING + "\"");

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals(ENCODED_STRING, base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptySingleQuotedString() {
        final Base64Url base64Url = new Base64Url("\'" + ENCODED_STRING + "\'");

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals(ENCODED_STRING, base64Url.toString());
    }

    @Test
    public void encode_withNullBytes() {
        final Base64Url base64Url = Base64Url.encode(null);

        assertNull(base64Url.encodedBytes());
        assertNull(base64Url.decodedBytes());
        assertEmptyString(base64Url.toString());
    }

    @Test
    public void encode_withEmptyBytes() {
        final Base64Url base64Url = Base64Url.encode(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void encode_withNonEmptyBytes() {
        final Base64Url base64Url = Base64Url.encode(DECODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals(ENCODED_STRING, base64Url.toString());
    }

    @Test
    public void toString_withSimpleString() {
        final Base64Url base64Url = new Base64Url(ENCODED_STRING);

        assertEquals(ENCODED_STRING, base64Url.toString());
    }

    @Test
    public void toString_withNullString() {
        final Base64Url base64Url = new Base64Url((String) null);

        assertEquals("", base64Url.toString());
    }

    @Test
    public void hashCode_equalToEncodedBytesHashCode() {
        final Base64Url base64Url = Base64Url.encode(DECODED_BYTES);

        assertEquals(Arrays.hashCode(ENCODED_BYTES), base64Url.hashCode());
    }

    @SuppressWarnings("SimplifiableJUnitAssertion")
    @Test
    public void equalTo_identicalObject() {
        final Base64Url base64Url = new Base64Url((byte[]) null);
        final Base64Url otherBase64Url = new Base64Url((byte[]) null);

        assertTrue(base64Url.equals(otherBase64Url));
    }

    @SuppressWarnings("SimplifiableJUnitAssertion")
    @Test
    public void notEqualTo_differentObject() {
        final Base64Url base64Url = new Base64Url((byte[]) null);
        final Base64Url otherBase64Url = new Base64Url(new byte[0]);

        assertFalse(base64Url.equals(otherBase64Url));
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions"})
    @Test
    public void notEqualTo_nullObject() {
        final Base64Url base64Url = new Base64Url((byte[]) null);

        assertFalse(base64Url.equals(null));
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes"})
    @Test
    public void notEqualTo_objectFromDifferentClass() {
        final Base64Url base64Url = new Base64Url((byte[]) null);

        assertFalse(base64Url.equals(""));
    }

    private static void assertEmptyString(String input) {
        assertEquals("", input);
    }
}
