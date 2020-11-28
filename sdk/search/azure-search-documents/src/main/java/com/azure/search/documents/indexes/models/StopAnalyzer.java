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
 * Divides text at non-letters; Applies the lowercase and stopword token
 * filters. This analyzer is implemented using Apache Lucene.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.StopAnalyzer")
@Fluent
public final class StopAnalyzer extends LexicalAnalyzer {
    /*
     * A list of stopwords.
     */
    @SerdeProperty(value = "stopwords")
    private List<String> stopwords;

    /**
     * Constructor of {@link StopAnalyzer}.
     *
     * @param name The name of the analyzer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public StopAnalyzer(String name) {
        super(name);
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
     * @return the StopAnalyzer object itself.
     */
    public StopAnalyzer setStopwords(String... stopwords) {
        this.stopwords = (stopwords == null) ? null : Arrays.asList(stopwords);
        return this;
    }

    /**
     * Set the stopwords property: A list of stopwords.
     *
     * @param stopwords the stopwords value to set.
     * @return the StopAnalyzer object itself.
     */
    @SerdeSetter
    public StopAnalyzer setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
        return this;
    }
}
