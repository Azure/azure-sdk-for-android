// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * A character filter that replaces characters in the input string. It uses a
 * regular expression to identify character sequences to preserve and a
 * replacement pattern to identify characters to replace. For example, given
 * the input text "aa bb aa bb", pattern "(aa)\s+(bb)", and replacement
 * "$1#$2", the result would be "aa#bb aa#bb". This character filter is
 * implemented using Apache Lucene.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.PatternReplaceCharFilter")
@Fluent
public final class PatternReplaceCharFilter extends CharFilter {
    /*
     * A regular expression pattern.
     */
    @SerdeProperty(value = "pattern")
    private String pattern;

    /*
     * The replacement text.
     */
    @SerdeProperty(value = "replacement")
    private String replacement;

    /**
     * Constructor of {@link CharFilter}.
     *
     * @param name The name of the char filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param pattern A regular expression pattern.
     * @param replacement The replacement text.
     */
    public PatternReplaceCharFilter(String name, String pattern, String replacement) {
        super(name);
        this.pattern = pattern;
        this.replacement = replacement;
    }

    /**
     * Get the pattern property: A regular expression pattern.
     *
     * @return the pattern value.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Get the replacement property: The replacement text.
     *
     * @return the replacement value.
     */
    public String getReplacement() {
        return this.replacement;
    }

}
