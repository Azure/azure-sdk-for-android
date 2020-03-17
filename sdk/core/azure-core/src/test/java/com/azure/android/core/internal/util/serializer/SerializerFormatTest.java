// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import org.junit.Test;

import okhttp3.Headers;

import static com.azure.android.core.internal.util.serializer.SerializerFormat.fromHeaders;
import static org.junit.Assert.*;

public class SerializerFormatTest {
    @Test
    public void fromHeaders_withApplicationJsonContentType() {
        Headers headers = new Headers.Builder()
            .set("Content-Type", "application/json")
            .build();

        assertEquals(SerializerFormat.JSON, fromHeaders(headers));
    }

    @Test
    public void fromHeaders_withApplicationXmlContentType() {
        Headers headers = new Headers.Builder()
            .set("Content-Type", "application/xml")
            .build();

        assertEquals(SerializerFormat.XML, fromHeaders(headers));
    }

    @Test
    public void fromHeaders_withTextXmlContentType() {
        Headers headers = new Headers.Builder()
            .set("Content-Type", "text/xml")
            .build();

        assertEquals(SerializerFormat.XML, fromHeaders(headers));
    }

    @Test
    public void fromHeaders_withOtherContentType() {
        Headers headers = new Headers.Builder()
            .set("Content-Type", "application/octet-stream")
            .build();

        assertEquals(SerializerFormat.JSON, fromHeaders(headers));
    }
}
