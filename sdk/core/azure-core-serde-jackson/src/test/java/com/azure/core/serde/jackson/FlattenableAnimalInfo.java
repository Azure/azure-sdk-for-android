// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.SerdeProperty;

public class FlattenableAnimalInfo {
    @SerdeProperty(value = "home")
    private String home;

    @SerdeProperty(value = "animal")
    private AnimalWithTypeIdContainingDot animal;

    public String home() {
        return this.home;
    }

    public FlattenableAnimalInfo withHome(String home) {
        this.home = home;
        return this;
    }

    public AnimalWithTypeIdContainingDot animal() {
        return this.animal;
    }

    public FlattenableAnimalInfo withAnimal(AnimalWithTypeIdContainingDot animal) {
        this.animal = animal;
        return this;
    }

}
