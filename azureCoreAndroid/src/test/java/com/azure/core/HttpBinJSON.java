package com.azure.core;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

@JacksonXmlRootElement(localName = "HttpBinJSON")
public class HttpBinJSON {
    @SerializedName(value = "url")
    private String url;

    @SerializedName(value = "headers")
    private Map<String, String> headers;

    @SerializedName(value = "data")
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
