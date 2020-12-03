// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.JsonFlatten;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

@JsonFlatten
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = NonEmptyAnimalWithTypeIdContainingDot.class)
@SerdeTypeName("NonEmptyAnimalWithTypeIdContainingDot")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "#Favourite.Pet.TurtleWithTypeIdContainingDot",
        value = TurtleWithTypeIdContainingDot.class)
})
public class NonEmptyAnimalWithTypeIdContainingDot {
    @SerdeProperty(value = "age")
    private Integer age;

    public Integer age() {
        return this.age;
    }

    public NonEmptyAnimalWithTypeIdContainingDot withAge(Integer age) {
        this.age = age;
        return this;
    }
}
