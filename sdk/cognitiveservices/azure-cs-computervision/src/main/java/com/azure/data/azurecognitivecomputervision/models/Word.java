package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Word {
    @JsonProperty(value = "boundingBox")
    private List<Double> boundingBox;

    @JsonProperty(value = "text")
    private String text;

    @JsonProperty(value = "confidence")
    private TextRecognitionResultConfidenceClass confidence;

    public List<Double> boundingBox() {
        return this.boundingBox;
    }

    public String text() {
        return this.text;
    }

    public TextRecognitionResultConfidenceClass confidence() {
        return this.confidence;
    }
}

