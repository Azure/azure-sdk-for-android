// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package android.util;

/**
 * Mock for android.util.Base64
 */
public class Base64 {
    public static final int DEFAULT = 0;

    public static String encodeToString(byte[] input, int flags) {
        return java.util.Base64.getEncoder().encodeToString(input);
    }

    public static byte[] decode(String input, int flags) {
        return java.util.Base64.getDecoder().decode(input);
    }
}