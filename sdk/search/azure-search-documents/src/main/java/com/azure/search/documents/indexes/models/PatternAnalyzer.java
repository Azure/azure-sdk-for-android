// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSetter;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.Arrays;
import java.util.List;

/**
 * Flexibly separates text into terms via a regular expression pattern. This
 * analyzer is implemented using Apache Lucene.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.PatternAnalyzer")
@Fluent
public final class PatternAnalyzer extends LexicalAnalyzer {
    /*
     * A value indicating whether terms should be lower-cased. Default is true.
     */
    @SerdeProperty(value = "lowercase")
    private Boolean lowerCaseTerms;

    /*
     * A regular expression pattern to match token separators. Default is an
     * expression that matches one or more non-word characters.
     */
    @SerdeProperty(value = "pattern")
    private String pattern;

    /*
     * Regular expression flags.
     */
    @SerdeProperty(value = "flags")
    private List<RegexFlags> flags;

    /*
     * A list of stopwords.
     */
    @SerdeProperty(value = "stopwords")
    private List<String> stopwords;

    /**
     * Constructor of {@link PatternAnalyzer}.
     *
     * @param name The name of the analyzer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public PatternAnalyzer(String name) {
        super(name);
    }

    /**
     * Get the lowerCaseTerms property: A value indicating whether terms should
     * be lower-cased. Default is true.
     *
     * @return the lowerCaseTerms value.
     */
    public Boolean areLowerCaseTerms() {
        return this.lowerCaseTerms;
    }

    /**
     * Set the lowerCaseTerms property: A value indicating whether terms should
     * be lower-cased. Default is true.
     *
     * @param lowerCaseTerms the lowerCaseTerms value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setLowerCaseTerms(Boolean lowerCaseTerms) {
        this.lowerCaseTerms = lowerCaseTerms;
        return this;
    }

    /**
     * Get the pattern property: A regular expression pattern to match token
     * separators. Default is an expression that matches one or more non-word
     * characters.
     *
     * @return the pattern value.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Set the pattern property: A regular expression pattern to match token
     * separators. Default is an expression that matches one or more non-word
     * characters.
     *
     * @param pattern the pattern value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Get the flags property: Regular expression flags.
     *
     * @return the flags value.
     */
    public List<RegexFlags> getFlags() {
        return this.flags;
    }

    /**
     * Set the flags property: Regular expression flags.
     *
     * @param flags the flags value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setFlags(RegexFlags... flags) {
        this.flags = (flags == null) ? null : Arrays.asList(flags);
        return this;
    }

    /**
     * Set the flags property: Regular expression flags.
     *
     * @param flags the flags value to set.
     * @return the PatternAnalyzer object itself.
     */
    @SerdeSetter
    public PatternAnalyzer setFlags(List<RegexFlags> flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Get the stopwords property: A list of stopwords.
     *
     * @return the stopwords value.
     */
    public List<String> getStopwords() {
        return this.stopwords;
    }

    /**
     * Set the stopwords property: A list of stopwords.
     *
     * @param stopwords the stopwords value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setStopwords(String... stopwords) {
        this.stopwords = (stopwords == null) ? null : Arrays.asList(stopwords);
        return this;
    }

    /**
     * Set the stopwords property: A list of stopwords.
     *
     * @param stopwords the stopwords value to set.
     * @return the PatternAnalyzer object itself.
     */
    @SerdeSetter
    public PatternAnalyzer setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
        return this;
    }
}
