// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.JsonFlatten;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

@JsonFlatten
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME,
    include = SerdeTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = DogWithTypeIdContainingDot.class)
@SerdeTypeName("#Favourite.Pet.DogWithTypeIdContainingDot")
public class DogWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @SerdeProperty(value = "breed")
    private String breed;

    // Flattenable property
    @SerdeProperty(value = "properties.cuteLevel")
    private Integer cuteLevel;

    public String breed() {
        return this.breed;
    }

    public DogWithTypeIdContainingDot withBreed(String audioLanguage) {
        this.breed = audioLanguage;
        return this;
    }

    public Integer cuteLevel() {
        return this.cuteLevel;
    }

    public DogWithTypeIdContainingDot withCuteLevel(Integer cuteLevel) {
        this.cuteLevel = cuteLevel;
        return this;
    }
}
