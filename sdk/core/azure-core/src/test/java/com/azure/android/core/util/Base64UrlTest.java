package com.azure.android.core.util;

import android.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Base64.class, Base64Url.class, Base64Util.class })
public class Base64UrlTest {
    private static final byte[] ENCODED_BYTES = new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 };
    private static final byte[] DECODED_BYTES = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    @Before
    public void setUp() {
        spy(Base64Url.class);
        spy(Base64Util.class);
    }

    @Test
    public void constructor_withNullBytes() {
        final Base64Url base64Url = new Base64Url((byte[]) null);

        assertNull(base64Url.encodedBytes());
        assertNull(base64Url.decodedBytes());
        assertEmptyString(base64Url.toString());
    }

    @Test
    public void constructor_withEmptyBytes() throws Exception {
        final Base64Url base64Url = new Base64Url(new byte[0]);

        doReturn(new byte[0]).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptyBytes() throws Exception {
        final Base64Url base64Url = new Base64Url(ENCODED_BYTES);

        doReturn(ENCODED_BYTES).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(DECODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Url.toString());
    }

    @Test
    public void constructor_withNullString() {
        final Base64Url base64Url = new Base64Url((String) null);

        assertNull(base64Url.encodedBytes());
        assertNull(base64Url.decodedBytes());
        assertEmptyString(base64Url.toString());
    }

    @Test
    public void constructor_withEmptyString() throws Exception {
        final Base64Url base64Url = new Base64Url("");

        doReturn(new byte[0]).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withEmptyDoubleQuotedString() throws Exception {
        final Base64Url base64Url = new Base64Url("\"\"");

        doReturn(new byte[0]).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withEmptySingleQuotedString() throws Exception {
        final Base64Url base64Url = new Base64Url("\'\'");

        doReturn(new byte[0]).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptyString() throws Exception {
        final Base64Url base64Url = new Base64Url("AAECAwQFBgcICQ");

        doReturn(ENCODED_BYTES).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(DECODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptyDoubleQuotedString() throws Exception {
        final Base64Url base64Url = new Base64Url("\"AAECAwQFBgcICQ\"");

        doReturn(ENCODED_BYTES).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(DECODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Url.toString());
    }

    @Test
    public void constructor_withNonEmptySingleQuotedString() throws Exception {
        final Base64Url base64Url = new Base64Url("\'AAECAwQFBgcICQ\'");

        doReturn(ENCODED_BYTES).when(Base64Url.class, "copy", notNull());
        when(Base64Util.decodeUrl((byte[]) notNull())).thenReturn(DECODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Url.toString());
    }

    @Test
    public void encode_withNullBytes() {
        final Base64Url base64Url = Base64Url.encode(null);

        assertNull(base64Url.encodedBytes());
        assertNull(base64Url.decodedBytes());
        assertEmptyString(base64Url.toString());
    }

    @Test
    public void encode_withEmptyBytes() throws Exception {
        spy(Base64.class);
        when(Base64.encode(notNull(), anyInt())).thenReturn(new byte[0]);

        final Base64Url base64Url = Base64Url.encode(new byte[0]);

        doReturn(new byte[0]).when(Base64Url.class, "copy", any());
        when(Base64Util.decodeUrl((byte[]) any())).thenReturn(new byte[0]);

        assertArrayEquals(new byte[0], base64Url.encodedBytes());
        assertArrayEquals(new byte[0], base64Url.decodedBytes());
        assertEquals("", base64Url.toString());
    }

    @Test
    public void encode_withNonEmptyBytes() throws Exception {
        spy(Base64.class);
        when(Base64.encode(notNull(), anyInt())).thenReturn(ENCODED_BYTES);

        final Base64Url base64Url = Base64Url.encode(DECODED_BYTES);

        doReturn(ENCODED_BYTES).when(Base64Url.class, "copy", any());
        when(Base64Util.decodeUrl((byte[]) any())).thenReturn(DECODED_BYTES);

        assertArrayEquals(ENCODED_BYTES, base64Url.encodedBytes());
        assertArrayEquals(DECODED_BYTES, base64Url.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Url.toString());
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
