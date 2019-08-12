package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TextRecognitionResult {
    @JsonProperty(value = "page")
    private Integer page;

    @JsonProperty(value = "clockwiseOrientation")
    private Double clockwiseOrientation;

    @JsonProperty(value = "width")
    private Double width;

    @JsonProperty(value = "height")
    private Double height;

    @JsonProperty(value = "unit")
    private TextRecognitionResultDimensionUnit unit;

    @JsonProperty(value = "lines")
    private List<Line> lines;

    public Integer page() {
        return this.page;
    }

    public Double clockwiseOrientation() {
        return this.clockwiseOrientation;
    }

    public Double width() {
        return this.width;
    }

    public Double height() {
        return this.height;
    }

    public TextRecognitionResultDimensionUnit unit() {
        return this.unit;
    }

    public List<Line> lines() {
        return this.lines;
    }
}

