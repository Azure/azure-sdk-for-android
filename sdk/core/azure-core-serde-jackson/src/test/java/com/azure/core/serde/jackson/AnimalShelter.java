// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.JsonFlatten;
import com.azure.core.serde.SerdeProperty;

import java.util.List;

@JsonFlatten
public class AnimalShelter {
    @SerdeProperty(value = "properties.description")
    private String description;

    @SerdeProperty(value = "properties.animalsInfo")
    private List<FlattenableAnimalInfo> animalsInfo;

    public String description() {
        return this.description;
    }

    public AnimalShelter withDescription(String description) {
        this.description = description;
        return this;
    }

    public List<FlattenableAnimalInfo> animalsInfo() {
        return this.animalsInfo;
    }

    public AnimalShelter withAnimalsInfo(List<FlattenableAnimalInfo> animalsInfo) {
        this.animalsInfo = animalsInfo;
        return this;
    }
}
