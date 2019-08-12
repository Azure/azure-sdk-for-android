package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TextRecognitionResultConfidenceClass {
    HIGH("High"),
    LOW("Low");

    private String value;

    TextRecognitionResultConfidenceClass(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TextRecognitionResultConfidenceClass fromString(String value) {
        TextRecognitionResultConfidenceClass[] items = TextRecognitionResultConfidenceClass.values();
        for (TextRecognitionResultConfidenceClass item : items) {
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
