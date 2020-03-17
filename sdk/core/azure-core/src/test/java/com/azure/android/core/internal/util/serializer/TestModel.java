// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TestModel {
    private int x;
    private int y;

    @JsonCreator
    public TestModel(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @NonNull
    @Override
    public String toString() {
        return "TestModel{" +
            "x=" + x +
            ", y=" + y +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TestModel testModel = (TestModel) obj;

        return x == testModel.x && y == testModel.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
