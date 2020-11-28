// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Defines a function that boosts scores based on the magnitude of a numeric
 * field.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "type")
@SerdeTypeName("magnitude")
@Fluent
public final class MagnitudeScoringFunction extends ScoringFunction {
    /*
     * Parameter values for the magnitude scoring function.
     */
    @SerdeProperty(value = "magnitude")
    private MagnitudeScoringParameters parameters;

    /**
     * Constructor of {@link MagnitudeScoringFunction}.
     *
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     * @param parameters Parameter values for the magnitude scoring function.
     */
    @SerdeToPojo
    public MagnitudeScoringFunction(
        @SerdeProperty(value = "fieldName") String fieldName,
        @SerdeProperty(value = "boost") double boost,
        @SerdeProperty(value = "magnitude") MagnitudeScoringParameters parameters) {
        super(fieldName, boost);
        this.parameters = parameters;
    }

    /**
     * Get the parameters property: Parameter values for the magnitude scoring
     * function.
     *
     * @return the parameters value.
     */
    public MagnitudeScoringParameters getParameters() {
        return this.parameters;
    }
}
