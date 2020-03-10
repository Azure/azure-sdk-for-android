// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;


import android.util.Base64;

/**
 * Utility for Base64 encoding and decoding.
 */
public final class Base64Util {
    private Base64Util() {
        // Empty constructor to prevent instantiation of this class.
    }

    /**
     * Encodes a byte array to Base64.
     *
     * @param src The byte array to encode.
     * @return The Base64 encoded bytes.
     */
    public static byte[] encode(byte[] src) {
        return src == null ? null : Base64.encode(src, Base64.DEFAULT);
    }

    /**
     * Encodes a byte array to Base64 URL format.
     *
     * @param src The byte array to encode.
     * @return The Base64 URL encoded bytes.
     */
    public static byte[] encodeUrlWithoutPadding(byte[] src) {
        int flags = Base64.URL_SAFE | Base64.NO_PADDING;

        return src == null ? null : Base64.encode(src, flags);
    }

    /**
     * Encodes a byte array to a Base64 string.
     *
     * @param src The byte array to encode.
     * @return The Base64 encoded bytes.
     */
    public static String encodeToString(byte[] src) {
        return src == null ? null : Base64.encodeToString(src, Base64.DEFAULT);
    }

    /**
     * Decodes a Base64 encoded byte array.
     *
     * @param encoded The byte array to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(byte[] encoded) {
        return encoded == null ? null : Base64.decode(encoded, Base64.DEFAULT);
    }

    /**
     * Decodes a byte array in Base64 URL format.
     *
     * @param src The byte array to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeUrl(byte[] src) {
        return src == null ? null : Base64.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a string in Base64 URL format.
     *
     * @param src The string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeUrl(String src) {
        return src == null ? null : Base64.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a Base64 encoded string.
     *
     * @param encoded The string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeString(String encoded) {
        return encoded == null ? null : Base64.decode(encoded, Base64.DEFAULT);
    }
}
