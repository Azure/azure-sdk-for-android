package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Line {
    @JsonProperty(value = "boundingBox")
    private List<Double> boundingBox;

    @JsonProperty(value = "text")
    private String text;

    @JsonProperty(value = "words")
    private List<Word> words;

    public List<Double> boundingBox() {
        return this.boundingBox;
    }

    public String text() {
        return this.text;
    }

    public List<Word> words() {
        return this.words;
    }
}
