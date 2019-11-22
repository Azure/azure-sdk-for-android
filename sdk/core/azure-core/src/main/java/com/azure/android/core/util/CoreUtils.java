// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

/**
 * This class contains utility methods useful for building client libraries.
 */
public class CoreUtils {
    private CoreUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Checks if the character sequence is null or empty.
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }
}
