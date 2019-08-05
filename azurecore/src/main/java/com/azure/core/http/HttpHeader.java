// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

public class HttpHeader {
    private final String name;
    private String value;

    public HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String[] values() {
        return value == null ? null : value.split(",");
    }

    public void addValue(String value) {
        this.value += "," + value;
    }

    @Override
    public String toString() {
        return name + ":" + value;
    }
}
