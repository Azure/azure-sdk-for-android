// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Defines a function that boosts scores based on the value of a date-time
 * field.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "type")
@SerdeTypeName("freshness")
@Fluent
public final class FreshnessScoringFunction extends ScoringFunction {
    /*
     * Parameter values for the freshness scoring function.
     */
    @SerdeProperty(value = "freshness")
    private FreshnessScoringParameters parameters;

    /**
     * Constructor of {@link ScoringFunction}.
     *
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     * @param parameters Parameter values for the freshness scoring function.
     */
    public FreshnessScoringFunction(String fieldName, double boost, FreshnessScoringParameters parameters) {
        super(fieldName, boost);
        this.parameters = parameters;
    }

    /**
     * Get the parameters property: Parameter values for the freshness scoring
     * function.
     *
     * @return the parameters value.
     */
    public FreshnessScoringParameters getParameters() {
        return this.parameters;
    }

}
