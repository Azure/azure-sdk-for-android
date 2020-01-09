// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import java.util.Map;

/**
 * This interface contains utility methods useful for building client libraries.
 */
public interface CoreUtils {
    /**
     * Checks if the character sequence is null or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return True if the character sequence is null or empty, false otherwise.
     */
    static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Replaces the given characters with a corresponding character sequence
     *
     * @param charSequence Character sequence to replace characters in.
     * @param pairs        Map containing which characters to look for and their replacements.
     * @return StringBuilder where all keys in the map have been replaced by their corresponding values.
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
