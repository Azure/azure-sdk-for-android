// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;


import android.util.Base64;

/**
 * Utility for Base64 encoding and decoding.
 */
public interface Base64Util {
    /**
     * Encodes a byte array to Base64.
     *
     * @param src The byte array to encode.
     * @return The Base64 encoded bytes.
     */
    static byte[] encode(byte[] src) {
        return src == null ? null : Base64.encode(src, Base64.DEFAULT);
    }

    /**
     * Encodes a byte array to Base64 URL format.
     *
     * @param src The byte array to encode.
     * @return The Base64 URL encoded bytes.
     */
    static byte[] encodeURLWithoutPadding(byte[] src) {
        int flags = Base64.URL_SAFE | Base64.NO_PADDING;

        return src == null ? null : Base64.encode(src, flags);
    }

    /**
     * Encodes a byte array to a Base64 string.
     *
     * @param src The byte array to encode.
     * @return The Base64 encoded bytes.
     */
    static String encodeToString(byte[] src) {
        return src == null ? null : Base64.encodeToString(src, Base64.DEFAULT);
    }

    /**
     * Decodes a Base64 encoded byte array.
     *
     * @param encoded The byte array to decode.
     * @return The decoded byte array.
     */
    static byte[] decode(byte[] encoded) {
        return encoded == null ? null : Base64.decode(encoded, Base64.DEFAULT);
    }

    /**
     * Decodes a byte array in Base64 URL format.
     *
     * @param src The byte array to decode.
     * @return The decoded byte array.
     */
    static byte[] decodeURL(byte[] src) {
        return src == null ? null : Base64.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a string in Base64 URL format.
     *
     * @param src The string to decode.
     * @return The decoded byte array.
     */
    static byte[] decodeURL(String src) {
        return src == null ? null : Base64.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a Base64 encoded string.
     *
     * @param encoded The string to decode.
     * @return The decoded byte array.
     */
    static byte[] decodeString(String encoded) {
        return encoded == null ? null : Base64.decode(encoded, Base64.DEFAULT);
    }
}
