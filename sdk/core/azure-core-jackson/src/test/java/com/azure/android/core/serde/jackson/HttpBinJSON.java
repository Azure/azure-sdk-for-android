// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON {
    @JsonProperty("url")
    private String url;

    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("data")
    private Object data;

    /**
     * Gets the URL associated with this request.
     *
     * @return he URL associated with the request.
     */
    public String url() {
        return url;
    }

    /**
     * Sets the URL associated with this request.
     *
     * @param url The URL associated with the request.
     */
    public void url(String url) {
        this.url = url;
    }

    /**
     * Gets the response headers.
     *
     * @return The response headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Sets the response headers.
     *
     * @param headers The response headers.
     */
    public void headers(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Gets the response body.
     *
     * @return The response body.
     */
    public Object data() {
        return data;
    }

    /**
     * Sets the response body.
     *
     * @param data The response body.
     */
    public void data(Object data) {
        this.data = data;
    }
}
