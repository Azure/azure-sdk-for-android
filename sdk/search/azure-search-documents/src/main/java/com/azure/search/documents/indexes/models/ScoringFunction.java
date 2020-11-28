// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Base type for functions that can modify document scores during ranking.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "type",
    defaultImpl = ScoringFunction.class)
@SerdeTypeName("ScoringFunction")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "distance", value = DistanceScoringFunction.class),
    @SerdeSubTypes.Type(name = "freshness", value = FreshnessScoringFunction.class),
    @SerdeSubTypes.Type(name = "magnitude", value = MagnitudeScoringFunction.class),
    @SerdeSubTypes.Type(name = "tag", value = TagScoringFunction.class)
})
@Fluent
public abstract class ScoringFunction {
    /*
     * The name of the field used as input to the scoring function.
     */
    @SerdeProperty(value = "fieldName")
    private String fieldName;

    /*
     * A multiplier for the raw score. Must be a positive number not equal to
     * 1.0.
     */
    @SerdeProperty(value = "boost")
    private double boost;

    /*
     * A value indicating how boosting will be interpolated across document
     * scores; defaults to "Linear". Possible values include: 'Linear',
     * 'Constant', 'Quadratic', 'Logarithmic'
     */
    @SerdeProperty(value = "interpolation")
    private ScoringFunctionInterpolation interpolation;

    /**
     * Constructor of {@link ScoringFunction}.
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     */
    @SerdeToPojo
    public ScoringFunction(
        @SerdeProperty(value = "fieldName") String fieldName,
        @SerdeProperty(value = "boost") double boost) {
        this.fieldName = fieldName;
        this.boost = boost;
    }

    /**
     * Get the fieldName property: The name of the field used as input to the
     * scoring function.
     *
     * @return the fieldName value.
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * Get the boost property: A multiplier for the raw score. Must be a
     * positive number not equal to 1.0.
     *
     * @return the boost value.
     */
    public double getBoost() {
        return this.boost;
    }

    /**
     * Get the interpolation property: A value indicating how boosting will be
     * interpolated across document scores; defaults to "Linear". Possible
     * values include: 'Linear', 'Constant', 'Quadratic', 'Logarithmic'.
     *
     * @return the interpolation value.
     */
    public ScoringFunctionInterpolation getInterpolation() {
        return this.interpolation;
    }

    /**
     * Set the interpolation property: A value indicating how boosting will be
     * interpolated across document scores; defaults to "Linear". Possible
     * values include: 'Linear', 'Constant', 'Quadratic', 'Logarithmic'.
     *
     * @param interpolation the interpolation value to set.
     * @return the ScoringFunction object itself.
     */
    public ScoringFunction setInterpolation(ScoringFunctionInterpolation interpolation) {
        this.interpolation = interpolation;
        return this;
    }
}
