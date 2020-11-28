// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Defines a function that boosts scores based on distance from a geographic
 * location.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "type")
@SerdeTypeName("distance")
@Fluent
public final class DistanceScoringFunction extends ScoringFunction {
    /*
     * Parameter values for the distance scoring function.
     */
    @SerdeProperty(value = "distance")
    private DistanceScoringParameters parameters;

    /**
     * Constructor of {@link ScoringFunction}.
     *
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     * @param parameters Parameter values for the distance scoring function.
     */
    @SerdeToPojo
    public DistanceScoringFunction(
        @SerdeProperty(value = "fieldName") String fieldName,
        @SerdeProperty(value = "boost") double boost,
        @SerdeProperty(value = "distance") DistanceScoringParameters parameters) {
        super(fieldName, boost);
        this.parameters = parameters;
    }

    /**
     * Get the parameters property: Parameter values for the distance scoring
     * function.
     *
     * @return the parameters value.
     */
    public DistanceScoringParameters getParameters() {
        return this.parameters;
    }
}
