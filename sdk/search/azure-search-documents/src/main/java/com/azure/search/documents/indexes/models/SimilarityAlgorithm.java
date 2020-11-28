// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Base type for similarity algorithms. Similarity algorithms are used to
 * calculate scores that tie queries to documents. The higher the score, the
 * more relevant the document is to that specific query. Those scores are used
 * to rank the search results.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = SimilarityAlgorithm.class)
@SerdeTypeName("Similarity")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.ClassicSimilarity", value = ClassicSimilarityAlgorithm.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.BM25Similarity", value = BM25SimilarityAlgorithm.class)
})
@Fluent
public abstract class SimilarityAlgorithm {
}
