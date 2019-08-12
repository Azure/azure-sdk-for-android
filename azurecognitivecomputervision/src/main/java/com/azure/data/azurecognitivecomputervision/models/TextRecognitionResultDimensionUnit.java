package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TextRecognitionResultDimensionUnit {
    PIXEL("pixel"),
    INCH("inch");

    private String value;

    TextRecognitionResultDimensionUnit(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TextRecognitionResultDimensionUnit fromString(String value) {
        TextRecognitionResultDimensionUnit[] items = TextRecognitionResultDimensionUnit.values();
        for (TextRecognitionResultDimensionUnit item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}

