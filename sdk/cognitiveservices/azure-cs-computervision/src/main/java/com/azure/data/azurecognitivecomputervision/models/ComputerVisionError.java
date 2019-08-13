package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComputerVisionError {
    @JsonProperty(value = "code")
    private Object code;

    @JsonProperty(value = "message")
    private String message;

    @JsonProperty(value = "requestId")
    private String requestId;

    public Object code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    public String requestId() {
        return this.requestId;
    }
}
