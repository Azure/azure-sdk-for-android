// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CoreUtilTest {
    @Test
    public void isNullOrEmpty_returnsTrue_withNullString() {
        assertTrue(CoreUtil.isNullOrEmpty(null));
    }

    @Test
    public void isNullOrEmpty_returnsTrue_withEmptyString() {
        assertTrue(CoreUtil.isNullOrEmpty(""));
    }

    @Test
    public void isNullOrEmpty_returnsTrue_withEmptyStringBuilder() {
        assertTrue(CoreUtil.isNullOrEmpty(new StringBuilder()));
    }

    @Test
    public void isNullOrEmpty_returnsFalse_withNonEmptyString() {
        assertFalse(CoreUtil.isNullOrEmpty("Test"));
    }

    @Test
    public void replaceWithSingleCharacters_inString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "A");
        pairs.put(' ', "_");
        String testString = "Ohana means family.";

        //noinspection ConstantConditions
        assertEquals("OhAnA_meAns_fAmily.", CoreUtil.replace(testString, pairs).toString());
    }

    @Test
    public void replaceWithMultipleCharacters_inString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "Aloha");
        pairs.put(' ', "_____");
        String testString = "Ohana means family.";

        //noinspection ConstantConditions
        assertEquals("OhAlohanAloha_____meAlohans_____fAlohamily.", CoreUtil.replace(testString, pairs).toString());
    }

    @Test
    public void replaceCharacters_inStringBuilder() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "A");
        pairs.put(' ', "_");
        StringBuilder stringBuilder = new StringBuilder("Ohana means family.");

        //noinspection ConstantConditions
        assertEquals("OhAnA_meAns_fAmily.", CoreUtil.replace(stringBuilder, pairs).toString());
    }

    @Test
    public void replaceCharacters_inNullString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put('a', "A");
        pairs.put(' ', "_");

        assertNull(CoreUtil.replace(null, pairs));
    }

    @Test
    public void replaceNull_inNullString() {
        Map<Character, CharSequence> pairs = new HashMap<>();
        pairs.put(null, "Ohana means family.");

        assertEquals("Ohana means family.", CoreUtil.replace(null, pairs));
    }

    @Test
    public void applyTitleCase_toString() {
        assertEquals("Test String With Multiple Words.", CoreUtil.toTitleCase("Test string with multiple words.").toString());
    }

    @Test
    public void applyTitleCase_toString_WithWordsSeparatedBySpecialCharacters() {
        assertEquals("Test_string_with_multiple_words.", CoreUtil.toTitleCase("Test_string_with_multiple_words.").toString());
    }

    @Test
    public void applyTitleCase_toStringBuilder() {
        assertEquals("Test String With Multiple Words.", CoreUtil.toTitleCase(new StringBuilder("Test string with multiple words.")).toString());
    }

    @Test
    public void applyTitleCase_toEmptyString() {
        assertEquals("", CoreUtil.toTitleCase("").toString());
    }

    @Test
    public void applyTitleCase_toNullString() {
        assertNull(CoreUtil.toTitleCase(null));
    }
}
