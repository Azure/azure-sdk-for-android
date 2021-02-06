// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.azure.android.core.serde.JsonFlatten;
import com.azure.android.core.serde.SerdeProperty;
import com.azure.android.core.serde.SerdeTypeInfo;
import com.azure.android.core.serde.SerdeTypeName;

import java.util.List;

@JsonFlatten
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME,
    include = SerdeTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = RabbitWithTypeIdContainingDot.class)
@SerdeTypeName("#Favourite.Pet.RabbitWithTypeIdContainingDot")
public class RabbitWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @SerdeProperty(value = "tailLength")
    private Integer tailLength;

    @SerdeProperty(value = "meals")
    private List<String> meals;

    public Integer filters() {
        return this.tailLength;
    }

    public RabbitWithTypeIdContainingDot withTailLength(Integer tailLength) {
        this.tailLength = tailLength;
        return this;
    }

    public List<String> meals() {
        return this.meals;
    }

    public RabbitWithTypeIdContainingDot withMeals(List<String> meals) {
        this.meals = meals;
        return this;
    }
}
