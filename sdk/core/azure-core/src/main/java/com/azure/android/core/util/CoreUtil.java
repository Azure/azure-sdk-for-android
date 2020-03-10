// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import androidx.annotation.Nullable;

import java.util.Map;

/**
 * This interface contains static utility methods.
 */
public final class CoreUtil {
    private CoreUtil() {
        // Empty constructor to prevent instantiation of this class.
    }

    /**
     * Checks if the given character sequence is {@code null} or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return {@code true} if the character sequence is {@code null} or empty, {@code false} otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Replaces the characters (keys) in a given map with a corresponding character sequence (values).
     *
     * @param charSequence Character sequence to replace characters in.
     * @param pairs        Map containing which characters to look for and their replacements.
     * @return Character sequence where all keys in the given map have been replaced by their corresponding values.
     */
    @Nullable
    public static CharSequence replace(CharSequence charSequence, Map<Character, CharSequence> pairs) {
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

    /**
     * Converts the first letter of each word in the provided character sequence to uppercase.
     *
     * @param charSequence Character sequence to convert.
     * @return String where the first letter of each word has been converted to uppercase
     */
    public static CharSequence toTitleCase(CharSequence charSequence) {
        if (isNullOrEmpty(charSequence)) {
            return charSequence;
        }

        StringBuilder stringBuilder = new StringBuilder();
        boolean capitalizeNext = true;

        for (int i = 0; i < charSequence.length(); i++) {
            char c = charSequence.charAt(i);
            if (capitalizeNext && Character.isLetter(c)) {
                stringBuilder.append(Character.toUpperCase(c));

                capitalizeNext = false;

                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }

            stringBuilder.append(c);
        }

        return stringBuilder;
    }
}
