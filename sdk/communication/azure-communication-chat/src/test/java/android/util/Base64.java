// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package android.util;

public class Base64 {
    public static String encodeToString(byte[] input, int flags) {
        return java.util.Base64.getEncoder().encodeToString(input);
    }

    public static byte[] decode(String str, int flags) {
        return java.util.Base64.getDecoder().decode(str);
    }
}
