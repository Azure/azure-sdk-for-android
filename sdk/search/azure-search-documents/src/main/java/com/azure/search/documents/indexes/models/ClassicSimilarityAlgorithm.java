// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Legacy similarity algorithm which uses the Lucene TFIDFSimilarity
 * implementation of TF-IDF. This variation of TF-IDF introduces static
 * document length normalization as well as coordinating factors that penalize
 * documents that only partially match the searched queries.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.ClassicSimilarity")
@Fluent
public final class ClassicSimilarityAlgorithm extends SimilarityAlgorithm {
}
