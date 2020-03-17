// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import java.io.OutputStream;

public class StringBuilderOutputStream extends OutputStream {
    private StringBuilder string = new StringBuilder();

    @Override
    public void write(int x) {
        this.string.append((char) x);
    }

    public String toString() {
        return this.string.toString();
    }
}
