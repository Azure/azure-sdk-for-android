// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.azure.android.core.serde.SerdeProperty;
import com.azure.android.core.serde.SerdeTypeInfo;
import com.azure.android.core.serde.SerdeTypeName;

@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME,
    include = SerdeTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = TurtleWithTypeIdContainingDot.class)
@SerdeTypeName("#Favourite.Pet.TurtleWithTypeIdContainingDot")
public class TurtleWithTypeIdContainingDot extends NonEmptyAnimalWithTypeIdContainingDot {
    @SerdeProperty(value = "size")
    private Integer size;

    public Integer size() {
        return this.size;
    }

    public TurtleWithTypeIdContainingDot withSize(Integer size) {
        this.size = size;
        return this;
    }
}

