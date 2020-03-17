// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import androidx.annotation.NonNull;

import com.azure.android.core.annotation.HeaderCollection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class TestHeaders {
    private String firstHeader;

    @HeaderCollection("secondHeader-")
    private Map<String, String> secondHeader;

    @JsonCreator
    public TestHeaders(@JsonProperty(value = "firstHeader") String firstHeader,
                       @JsonProperty(value = "secondHeader") @NonNull Map<String, String> secondHeader) {
        this.firstHeader = firstHeader;
        this.secondHeader = secondHeader;
    }

    public String getFirstHeader() {
        return firstHeader;
    }

    public void setFirstHeader(String firstHeader) {
        this.firstHeader = firstHeader;
    }

    public Map<String, String> getSecondHeader() {
        return secondHeader;
    }

    public void setSecondHeader(Map<String, String> secondHeader) {
        this.secondHeader = secondHeader;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TestHeaders testHeaders = (TestHeaders) obj;
        boolean mapsAreEqual = true;

        for (String key : testHeaders.secondHeader.keySet()) {
            if (!secondHeader.containsKey("secondHeader-" + key)) {
                mapsAreEqual = false;

                break;
            } else if (!secondHeader.get("secondHeader-" + key).equals(testHeaders.secondHeader.get(key))) {
                mapsAreEqual = false;

                break;
            }
        }

        return firstHeader.equals(testHeaders.firstHeader) && mapsAreEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstHeader, secondHeader);
    }
}
