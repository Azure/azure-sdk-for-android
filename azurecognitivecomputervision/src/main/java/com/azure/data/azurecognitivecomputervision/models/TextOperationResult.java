package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextOperationResult {
    @JsonProperty(value = "status")
    private TextOperationStatusCodes status;

    @JsonProperty(value = "recognitionResult")
    private TextRecognitionResult recognitionResult;

    public TextOperationStatusCodes status() {
        return this.status;
    }

    public TextRecognitionResult recognitionResult() {
        return this.recognitionResult;
    }
}
