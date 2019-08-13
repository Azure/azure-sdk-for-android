package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TextOperationStatusCodes {
    NOT_STARTED("NotStarted"),
    RUNNING("Running"),
    FAILED("Failed"),
    SUCCEEDED("Succeeded");

    private String value;

    TextOperationStatusCodes(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TextOperationStatusCodes fromString(String value) {
        TextOperationStatusCodes[] items = TextOperationStatusCodes.values();
        for (TextOperationStatusCodes item : items) {
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
