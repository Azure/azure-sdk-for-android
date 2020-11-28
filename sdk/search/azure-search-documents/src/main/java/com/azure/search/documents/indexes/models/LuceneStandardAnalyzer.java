// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSetter;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.Arrays;
import java.util.List;

/**
 * Standard Apache Lucene analyzer; Composed of the standard tokenizer,
 * lowercase filter and stop filter.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.StandardAnalyzer")
@Fluent
public final class LuceneStandardAnalyzer extends LexicalAnalyzer {
    /*
     * The maximum token length. Default is 255. Tokens longer than the maximum
     * length are split. The maximum token length that can be used is 300
     * characters.
     */
    @SerdeProperty(value = "maxTokenLength")
    private Integer maxTokenLength;

    /*
     * A list of stopwords.
     */
    @SerdeProperty(value = "stopwords")
    private List<String> stopwords;

    /**
     * Constructor of {@link LuceneStandardAnalyzer}.
     *
     * @param name The name of the analyzer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @SerdeToPojo
    public LuceneStandardAnalyzer(@SerdeProperty(value = "name") String name) {
        super(name);
    }

    /**
     * Get the maxTokenLength property: The maximum token length. Default is
     * 255. Tokens longer than the maximum length are split. The maximum token
     * length that can be used is 300 characters.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return this.maxTokenLength;
    }

    /**
     * Set the maxTokenLength property: The maximum token length. Default is
     * 255. Tokens longer than the maximum length are split. The maximum token
     * length that can be used is 300 characters.
     *
     * @param maxTokenLength the maxTokenLength value to set.
     * @return the LuceneStandardAnalyzer object itself.
     */
    public LuceneStandardAnalyzer setMaxTokenLength(Integer maxTokenLength) {
        this.maxTokenLength = maxTokenLength;
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
     * @return the LuceneStandardAnalyzer object itself.
     */
    public LuceneStandardAnalyzer setStopwords(String... stopwords) {
        this.stopwords = (stopwords == null) ? null : Arrays.asList(stopwords);
        return this;
    }

    /**
     * Set the stopwords property: A list of stopwords.
     *
     * @param stopwords the stopwords value to set.
     * @return the LuceneStandardAnalyzer object itself.
     */
    @SerdeSetter
    public LuceneStandardAnalyzer setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
        return this;
    }
}
