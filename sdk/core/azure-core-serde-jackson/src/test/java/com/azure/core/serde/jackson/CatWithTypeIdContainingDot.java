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
    defaultImpl = CatWithTypeIdContainingDot.class)
@SerdeTypeName("#Favourite.Pet.CatWithTypeIdContainingDot")
public class CatWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @SerdeProperty(value = "breed")
    private String breed;

    public String breed() {
        return this.breed;
    }

    public CatWithTypeIdContainingDot withBreed(String presetName) {
        this.breed = presetName;
        return this;
    }
}
