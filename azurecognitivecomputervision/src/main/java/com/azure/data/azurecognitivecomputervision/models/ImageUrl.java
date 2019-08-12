package com.azure.data.azurecognitivecomputervision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageUrl {
    @JsonProperty(value = "url")
    private String url;

    public String url() {
        return this.url;
    }

    public ImageUrl withUrl(String url) {
        this.url = url;
        return this;
    }
}
