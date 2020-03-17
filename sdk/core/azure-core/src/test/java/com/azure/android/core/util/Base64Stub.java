package com.azure.android.core.util;

class Base64Stub implements Base64Util.Base64Wrapper {
    static final byte[] DECODED_BYTES =
        new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 };
    static final byte[] ENCODED_BYTES =
        new byte[] { 81, 85, 70, 70, 81, 48, 70, 51, 85, 85, 90, 67, 90, 50, 78, 74, 81, 49, 69, 61 };
    static final String ENCODED_STRING = "QUFFQ0F3UUZCZ2NJQ1E=";

    @Override
    public byte[] encode(byte[] input, int flags) {
        if (input.length == 0) {
            return input;
        } else {
            return ENCODED_BYTES;
        }
    }

    @Override
    public String encodeToString(byte[] input, int flags) {
        if (input.length == 0) {
            return "";
        } else {
            return ENCODED_STRING;
        }
    }

    @Override
    public byte[] decode(byte[] input, int flags) {
        if (input.length == 0) {
            return input;
        } else {
            return DECODED_BYTES;
        }
    }

    @Override
    public byte[] decode(String input, int flags) {
        if (input.isEmpty()) {
            return new byte[0];
        } else {
            return DECODED_BYTES;
        }
    }
}
