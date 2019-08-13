package com.azure.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Map;

@JacksonXmlRootElement(localName = "HttpBinJSON")
public class HttpBinJSON {
    @JsonProperty(value = "url")
    private String url;

    @JsonProperty(value = "headers")
    private Map<String, String> headers;

    @JsonProperty(value = "data")
    private Object data;

    public String url() {
        return url;
    }

    public void url(String url) {
        this.url = url;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void headers(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object data() {
        return data;
    }

    public void data(Object data) {
        this.data = data;
    }
}
