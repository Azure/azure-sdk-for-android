// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;

/**
 * Provides parameter values to a magnitude scoring function.
 */
@Fluent
public final class MagnitudeScoringParameters {
    /*
     * The field value at which boosting starts.
     */
    @SerdeProperty(value = "boostingRangeStart")
    private double boostingRangeStart;

    /*
     * The field value at which boosting ends.
     */
    @SerdeProperty(value = "boostingRangeEnd")
    private double boostingRangeEnd;

    /*
     * A value indicating whether to apply a constant boost for field values
     * beyond the range end value; default is false.
     */
    @SerdeProperty(value = "constantBoostBeyondRange")
    private Boolean shouldBoostBeyondRangeByConstant;

    /**
     * Constructor of {@link MagnitudeScoringParameters}.
     *
     * @param boostingRangeStart The field value at which boosting starts.
     * @param boostingRangeEnd The field value at which boosting ends.
     */
    @SerdeToPojo
    public MagnitudeScoringParameters(
        @SerdeProperty(value = "boostingRangeStart") double boostingRangeStart,
        @SerdeProperty(value = "boostingRangeEnd") double boostingRangeEnd) {
        this.boostingRangeStart = boostingRangeStart;
        this.boostingRangeEnd = boostingRangeEnd;
    }

    /**
     * Get the boostingRangeStart property: The field value at which boosting
     * starts.
     *
     * @return the boostingRangeStart value.
     */
    public double getBoostingRangeStart() {
        return this.boostingRangeStart;
    }

    /**
     * Get the boostingRangeEnd property: The field value at which boosting
     * ends.
     *
     * @return the boostingRangeEnd value.
     */
    public double getBoostingRangeEnd() {
        return this.boostingRangeEnd;
    }

    /**
     * Get the shouldBoostBeyondRangeByConstant property: A value indicating
     * whether to apply a constant boost for field values beyond the range end
     * value; default is false.
     *
     * @return the shouldBoostBeyondRangeByConstant value.
     */
    public Boolean shouldBoostBeyondRangeByConstant() {
        return this.shouldBoostBeyondRangeByConstant;
    }

    /**
     * Set the shouldBoostBeyondRangeByConstant property: A value indicating
     * whether to apply a constant boost for field values beyond the range end
     * value; default is false.
     *
     * @param shouldBoostBeyondRangeByConstant the
     * shouldBoostBeyondRangeByConstant value to set.
     * @return the MagnitudeScoringParameters object itself.
     */
    public MagnitudeScoringParameters setShouldBoostBeyondRangeByConstant(Boolean shouldBoostBeyondRangeByConstant) {
        this.shouldBoostBeyondRangeByConstant = shouldBoostBeyondRangeByConstant;
        return this;
    }
}
