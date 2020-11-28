// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.util;

public class UrlToken {
    private final String text;
    private final UrlTokenType type;

    // TODO (anuchan) back to package private once tests are moved
    public UrlToken(String text, UrlTokenType type) {
        this.text = text;
        this.type = type;
    }

    String text() {
        return text;
    }

    UrlTokenType type() {
        return type;
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof UrlToken && equals((UrlToken) rhs);
    }

    public boolean equals(UrlToken rhs) {
        return rhs != null && text.equals(rhs.text) && type == rhs.type;
    }

    @Override
    public String toString() {
        return "\"" + text + "\" (" + type + ")";
    }

    @Override
    public int hashCode() {
        return (text == null ? 0 : text.hashCode()) ^ type.hashCode();
    }

    // TODO (anuchan) back to package private once tests are moved
    public static UrlToken scheme(String text) {
        return new UrlToken(text, UrlTokenType.SCHEME);
    }

    // TODO (anuchan) back to package private once tests are moved
    public static UrlToken host(String text) {
        return new UrlToken(text, UrlTokenType.HOST);
    }

    // TODO (anuchan) back to package private once tests are moved
    public static UrlToken port(String text) {
        return new UrlToken(text, UrlTokenType.PORT);
    }

    // TODO (anuchan) back to package private once tests are moved
    public static UrlToken path(String text) {
        return new UrlToken(text, UrlTokenType.PATH);
    }

    // TODO (anuchan) back to package private once tests are moved
    public static UrlToken query(String text) {
        return new UrlToken(text, UrlTokenType.QUERY);
    }
}
