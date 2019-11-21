// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import android.util.Base64;

/**
 * Utility type exposing Base64 encoding and decoding methods.
 */
public final class Base64Util {
    /**
     * Encodes a byte array to base64.
     * @param src the byte array to encode
     * @return the base64 encoded bytes
     */
    public static byte[] encode(byte[] src) {
        return src == null ? null : Base64.encode(src, Base64.DEFAULT);
    }

    /**
     * Encodes a byte array to base64 URL format.
     * @param src the byte array to encode
     * @return the base64 URL encoded bytes
     */
    public static byte[] encodeURLWithoutPadding(byte[] src) {
        int flags = Base64.URL_SAFE | Base64.NO_PADDING;
        return src == null ? null : Base64.encode(src, flags);
    }

    /**
     * Encodes a byte array to a base 64 string.
     * @param src the byte array to encode
     * @return the base64 encoded string
     */
    public static String encodeToString(byte[] src) {
        return src == null ? null : Base64.encodeToString(src, Base64.DEFAULT);
    }

    /**
     * Decodes a base64 encoded byte array.
     * @param encoded the byte array to decode
     * @return the decoded byte array
     */
    public static byte[] decode(byte[] encoded) {
        return encoded == null ? null : Base64.decode(encoded, Base64.DEFAULT);
    }

    /**
     * Decodes a byte array in base64 URL format.
     * @param src the byte array to decode
     * @return the decoded byte array
     */
    public static byte[] decodeURL(byte[] src) {
        return src == null ? null : Base64.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a base64 encoded string.
     * @param encoded the string to decode
     * @return the decoded byte array
     */
    public static byte[] decodeString(String encoded) {
        return encoded == null ? null : Base64.decode(encoded, Base64.DEFAULT);
    }

    // Private Ctr
    private Base64Util() {
    }
}
