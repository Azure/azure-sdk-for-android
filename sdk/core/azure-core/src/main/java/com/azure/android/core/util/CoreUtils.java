// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import java.util.Map;

/**
 * This interface contains static utility methods.
 */
public interface CoreUtils {
    /**
     * Checks if the given character sequence is {@code null} or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return {@code true} if the character sequence is {@code null} or empty, {@code false} otherwise.
     */
    static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Replaces the characters (keys) in a given map with a corresponding character sequence (values).
     *
     * @param charSequence Character sequence to replace characters in.
     * @param pairs        Map containing which characters to look for and their replacements.
     * @return Character sequence where all keys in the given map have been replaced by their corresponding values.
     */
    static CharSequence replace(CharSequence charSequence, Map<Character, CharSequence> pairs) {
        if (isNullOrEmpty(charSequence)) {
            if (pairs.containsKey(null)) {
                return pairs.get(null);
            } else {
                return null;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (char c : charSequence.toString().toCharArray()) {
            if (pairs.containsKey(c)) {
                stringBuilder.append(pairs.get(c));
            } else {
                stringBuilder.append(c);
            }
        }

        return stringBuilder;
    }
}
