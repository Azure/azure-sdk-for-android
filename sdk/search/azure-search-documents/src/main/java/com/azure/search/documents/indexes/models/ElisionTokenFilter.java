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
 * Removes elisions. For example, "l'avion" (the plane) will be converted to
 * "avion" (plane). This token filter is implemented using Apache Lucene.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.ElisionTokenFilter")
@Fluent
public final class ElisionTokenFilter extends TokenFilter {
    /*
     * The set of articles to remove.
     */
    @SerdeProperty(value = "articles")
    private List<String> articles;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public ElisionTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the articles property: The set of articles to remove.
     *
     * @return the articles value.
     */
    public List<String> getArticles() {
        return this.articles;
    }

    /**
     * Set the articles property: The set of articles to remove.
     *
     * @param articles the articles value to set.
     * @return the ElisionTokenFilter object itself.
     */
    public ElisionTokenFilter setArticles(String... articles) {
        this.articles = (articles == null) ? null : Arrays.asList(articles);
        return this;
    }

    /**
     * Set the articles property: The set of articles to remove.
     *
     * @param articles the articles value to set.
     * @return the ElisionTokenFilter object itself.
     */
    @SerdeSetter
    public ElisionTokenFilter setArticles(List<String> articles) {
        this.articles = articles;
        return this;
    }
}
