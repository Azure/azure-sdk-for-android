package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TextRecognitionMode {
    HANDWRITTEN("Handwritten"),
    PRINTED("Printed");

    private String value;

    TextRecognitionMode(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TextRecognitionMode fromString(String value) {
        TextRecognitionMode[] items = TextRecognitionMode.values();
        for (TextRecognitionMode item : items) {
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
