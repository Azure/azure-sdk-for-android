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
     * We cannot call Android framework's classes like {@link Base64} from unit tests and we cannot use Java's
     * Base64 class because that's only available at a later Android version. To get around this and not use
     * something like PowerMock to mock static methods like {@code Base64.encode()}, the {@link Base64Wrapper}
     * interface was created. Normally this value will always be set to {@link Base64Android} and for unit tests it will
     * be set to a stub that implements the aforementioned interface.
     */
    private static Base64Wrapper base64Wrapper = new Base64Android();

    static void setBase64Wrapper(Base64Wrapper newWrapper) {
        base64Wrapper = newWrapper;
    }

    /**
     * Encodes a byte array to Base64.
     *
     * @param src The byte array to encode.
     * @return The Base64 encoded bytes.
     */
    public static byte[] encode(byte[] src) {
        return src == null ? null : base64Wrapper.encode(src, Base64.DEFAULT);
    }

    /**
     * Encodes a byte array to Base64 URL format.
     *
     * @param src The byte array to encode.
     * @return The Base64 URL encoded bytes.
     */
    public static byte[] encodeUrlWithoutPadding(byte[] src) {
        int flags = Base64.URL_SAFE | Base64.NO_PADDING;

        return src == null ? null : base64Wrapper.encode(src, flags);
    }

    /**
     * Encodes a byte array to a Base64 string.
     *
     * @param src The byte array to encode.
     * @return The Base64 encoded bytes.
     */
    public static String encodeToString(byte[] src) {
        return src == null ? null : base64Wrapper.encodeToString(src, Base64.DEFAULT);
    }

    /**
     * Decodes a Base64 encoded byte array.
     *
     * @param encoded The byte array to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(byte[] encoded) {
        return encoded == null ? null : base64Wrapper.decode(encoded, Base64.DEFAULT);
    }

    /**
     * Decodes a byte array in Base64 URL format.
     *
     * @param src The byte array to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeUrl(byte[] src) {
        return src == null ? null : base64Wrapper.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a string in Base64 URL format.
     *
     * @param src The string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeUrl(String src) {
        return src == null ? null : base64Wrapper.decode(src, Base64.URL_SAFE);
    }

    /**
     * Decodes a Base64 encoded string.
     *
     * @param encoded The string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeString(String encoded) {
        return encoded == null ? null : base64Wrapper.decode(encoded, Base64.DEFAULT);
    }

    interface Base64Wrapper {
        byte[] encode(byte[] input, int flags);
        String encodeToString(byte[] input, int flags);
        byte[] decode(byte[] input, int flags);
        byte[] decode(String input, int flags);
    }

    static class Base64Android implements Base64Wrapper {
        @Override
        public byte[] encode(byte[] input, int flags) {
            return Base64.encode(input, flags);
        }

        @Override
        public String encodeToString(byte[] input, int flags) {
            return Base64.encodeToString(input, flags);
        }

        @Override
        public byte[] decode(byte[] input, int flags) {
            return Base64.decode(input, flags);
        }

        @Override
        public byte[] decode(String input, int flags) {
            return Base64.decode(input, flags);
        }
    }
}
