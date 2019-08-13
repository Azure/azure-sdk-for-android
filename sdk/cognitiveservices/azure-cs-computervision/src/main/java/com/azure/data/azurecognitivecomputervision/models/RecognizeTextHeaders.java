package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecognizeTextHeaders {
    @JsonProperty("Operation-Location")
    private String operationLocation;

    public String operationLocation() {
        return operationLocation;
    }
}
