// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class HttpHeaders implements Iterable<HttpHeader> {
    private final Map<String, HttpHeader> headers = new HashMap<>();

    public HttpHeaders() {
    }

    public HttpHeaders(Map<String, String> headers) {
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            this.put(header.getKey(), header.getValue());
        }
    }

    public HttpHeaders(Iterable<HttpHeader> headers) {
        for (final HttpHeader header : headers) {
            this.put(header.name(), header.value());
        }
    }

    public HttpHeaders put(String name, String value) {
        headers.put(formatKey(name), new HttpHeader(name, value));
        return this;
    }

    public HttpHeader get(String name) {
        return headers.get(formatKey(name));
    }

    public String value(String name) {
        final HttpHeader header = get(name);
        return header == null ? null : header.value();
    }

    public String[] values(String name) {
        final HttpHeader header = get(name);
        return header == null ? null : header.values();
    }

    public int size() {
        return headers.size();
    }

    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers.values()) {
            result.put(header.name(), header.value());
        }
        return result;
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return headers.values().iterator();
    }

    private String formatKey(final String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
