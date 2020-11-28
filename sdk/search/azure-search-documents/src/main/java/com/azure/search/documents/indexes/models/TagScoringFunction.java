// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Defines a function that boosts scores of documents with string values
 * matching a given list of tags.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "type")
@SerdeTypeName("tag")
@Fluent
public final class TagScoringFunction extends ScoringFunction {
    /*
     * Parameter values for the tag scoring function.
     */
    @SerdeProperty(value = "tag")
    private TagScoringParameters parameters;

    /**
     * Constructor of {@link TagScoringFunction}.
     *
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     * @param parameters Parameter values for the tag scoring function.
     */
    @SerdeToPojo
    public TagScoringFunction(
        @SerdeProperty(value = "fieldName") String fieldName,
        @SerdeProperty(value = "boost") double boost,
        @SerdeProperty(value = "tag") TagScoringParameters parameters) {
        super(fieldName, boost);
        this.parameters = parameters;
    }

    /**
     * Get the parameters property: Parameter values for the tag scoring
     * function.
     *
     * @return the parameters value.
     */
    public TagScoringParameters getParameters() {
        return this.parameters;
    }

}
