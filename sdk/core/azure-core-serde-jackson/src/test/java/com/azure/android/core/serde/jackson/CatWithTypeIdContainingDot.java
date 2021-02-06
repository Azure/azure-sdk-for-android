// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.azure.android.core.serde.JsonFlatten;
import com.azure.android.core.serde.SerdeProperty;
import com.azure.android.core.serde.SerdeTypeInfo;
import com.azure.android.core.serde.SerdeTypeName;

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
