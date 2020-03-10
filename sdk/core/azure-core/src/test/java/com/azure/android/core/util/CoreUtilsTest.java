// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.android.core.util.CoreUtils.isNullOrEmpty;
import static com.azure.android.core.util.CoreUtils.replace;
import static com.azure.android.core.util.CoreUtils.toTitleCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CoreUtilsTest {
    @Test
    public void isNullOrEmpty_returnsTrue_withNullString() {
        assertTrue(isNullOrEmpty(null));
    }

    @Test
    public void isNullOrEmpty_returnsTrue_withEmptyString() {
        assertTrue(isNullOrEmpty(""));
    }

    @Test
    public void isNullOrEmpty_returnsTrue_withEmptyStringBuilder() {
        assertTrue(isNullOrEmpty(new StringBuilder()));
    }

    @Test
    public void isNullOrEmpty_returnsFalse_withNonEmptyString() {
        assertFalse(isNullOrEmpty("Test"));
    }

    @Test
    public void replaceWithSingleCharacters_inString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "A");
        pairs.put(' ', "_");
        String testString = "Ohana means family.";

        assertEquals("OhAnA_meAns_fAmily.", replace(testString, pairs).toString());
    }

    @Test
    public void replaceWithMultipleCharacters_inString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "Aloha");
        pairs.put(' ', "_____");
        String testString = "Ohana means family.";

        assertEquals("OhAlohanAloha_____meAlohans_____fAlohamily.", replace(testString, pairs).toString());
    }

    @Test
    public void replaceCharacters_inStringBuilder() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "A");
        pairs.put(' ', "_");
        StringBuilder stringBuilder = new StringBuilder("Ohana means family.");

        //noinspection ConstantConditions
        assertEquals("OhAnA_meAns_fAmily.", replace(stringBuilder, pairs).toString());
    }

    @Test
    public void replaceCharacters_inNullString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "A");
        pairs.put(' ', "_");

        assertNull(replace(null, pairs));
    }

    @Test
    public void replaceNull_inNullString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put(null, "Ohana means family.");

        assertEquals("Ohana means family.", replace(null, pairs));
    }

    @Test
    public void applyTitleCase_toString() {
        assertEquals("Test String With Multiple Words.", toTitleCase("Test string with multiple words.").toString());
    }

    @Test
    public void applyTitleCase_toString_WithWordsSeparatedBySpecialCharacters() {
        assertEquals("Test_string_with_multiple_words.", toTitleCase("Test_string_with_multiple_words.").toString());
    }

    @Test
    public void applyTitleCase_toStringBuilder() {
        assertEquals("Test String With Multiple Words.", toTitleCase(new StringBuilder("Test string with multiple words.")).toString());
    }

    @Test
    public void applyTitleCase_toEmptyString() {
        assertEquals("", toTitleCase("").toString());
    }

    @Test
    public void applyTitleCase_toNullString() {
        assertNull(toTitleCase(null));
    }
}
